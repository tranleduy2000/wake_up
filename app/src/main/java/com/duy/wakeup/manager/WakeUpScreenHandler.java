/*
 * Copyright (c) 2016-2017 Juan García Basilio
 *
 * This file is part of WaveUp.
 *
 * WaveUp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WaveUp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WaveUp.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.duy.wakeup.manager;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

import com.duy.wakeup.root.Root;

public class WakeUpScreenHandler {
    private static final String TAG = "ScreenHandler";

    private static final long TIME_SCREEN_ON = 5000;
    private static volatile WakeUpScreenHandler instance;
    private final PowerManager powerManager;
    private final PowerManager.WakeLock wakeLock;
    private final DevicePolicyManager policyManager;
    private final WaveUpWorldState waveUpWorldState;
    private final WakeUpSettings settings;
    private final Context context;
    private long lastTimeScreenOnOrOff;
    private Thread turnOffScreenThread;
    private boolean turningOffScreen;

    private WakeUpScreenHandler(Context context) {
        this.context = context;
        this.powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        this.wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "WakeUpWakeLock");
        this.policyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        this.settings = WakeUpSettings.getInstance(context);
        this.waveUpWorldState = new WaveUpWorldState(context);
    }

    public static WakeUpScreenHandler getInstance(Context context) {
        if (instance == null) {
            synchronized (WakeUpScreenHandler.class) {
                if (instance == null) {
                    instance = new WakeUpScreenHandler(context);
                }
            }
        }

        return instance;
    }

    private Thread turnOffScreenThread(final long delay) {
        return new Thread() {
            @Override
            public void run() {
                if (waveUpWorldState.isScreenOn()) {
                    Log.d(TAG, "Creating a thread to turn off display if still covered in " + delay / 1000 + " seconds");
                    try {
                        Thread.sleep(delay);
                        doTurnOffScreen();
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Interrupted thread: Turning off screen cancelled.");
                    }
                }
            }
        };
    }

    private void doTurnOffScreen() {
        turningOffScreen = true; // Issue #68. Avoid interrupting the thread if screen is already being turned off.
        lastTimeScreenOnOrOff = System.currentTimeMillis();
        if (settings.isVibrateWhileLocking()) {
            vibrate();
        }
        Log.i(TAG, "Switched from 'far' to 'near'.");
        if (settings.isLockScreenWithPowerButton()) {
            Log.i(TAG, "Turning screen off simulating power button press.");
            Root.pressPowerButton();
        } else {
            Log.i(TAG, "Turning screen off.");
            try {
                policyManager.lockNow();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Failed to run lockNow() to turn off the screen. Probably due to an ongoing call. Exception: " + e);
            } catch (SecurityException e) {
                Log.e(TAG, "Failed to run lockNow() to turn off the screen. Probably due to missing device admin rights, which I don't really understand... Exception: " + e);
            }
        }
        turningOffScreen = false;
    }

    public void turnOffScreen() {
        if (waveUpWorldState.isScreenOn()) {
            if (settings.isVibrateWhileLocking()) {
                vibrate();
            }
            turnOffScreenThread = turnOffScreenThread(settings.getSensorCoverTimeBeforeLockingScreen());
            turnOffScreenThread.start();
        }
    }

    public void cancelTurnOff() {
        if (turnOffScreenThread != null && !turningOffScreen) {
            Log.d(TAG, "Cancelling turning off of display");
            turnOffScreenThread.interrupt();
            turnOffScreenThread = null;
        }
    }

    public long getLastTimeScreenOnOrOff() {
        return lastTimeScreenOnOrOff;
    }

    public void turnOnScreen() {
        if (!waveUpWorldState.isScreenOn()) {
            lastTimeScreenOnOrOff = System.currentTimeMillis();
            Log.i(TAG, "Switched from 'near' to 'far'. Turning screen on");
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
            wakeLock.acquire(TIME_SCREEN_ON);
        }
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(50);
    }
}