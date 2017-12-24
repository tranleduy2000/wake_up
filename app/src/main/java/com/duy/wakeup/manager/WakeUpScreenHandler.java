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
    private static volatile WakeUpScreenHandler INSTANCE;

    private final PowerManager.WakeLock mWakeLock;
    private final DevicePolicyManager mPolicyManager;
    private final WaveUpWorldState mWaveUpWorldState;
    private final WakeUpSettings mSettings;
    private final Context mContext;
    private long mLastTimeScreenOnOrOff;
    private Thread mTurnOffScreenThread;
    private boolean mIsTurningOffScreen;

    private WakeUpScreenHandler(Context context) {
        this.mContext = context.getApplicationContext();

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "WakeUpWakeLock");

        this.mPolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        this.mSettings = WakeUpSettings.getInstance(context);
        this.mWaveUpWorldState = new WaveUpWorldState(context);
    }

    public static WakeUpScreenHandler getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (WakeUpScreenHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WakeUpScreenHandler(context);
                }
            }
        }

        return INSTANCE;
    }

    private Thread turnOffScreenThread(final long delay) {
        return new Thread() {
            @Override
            public void run() {
                if (mWaveUpWorldState.isScreenOn()) {
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
        mIsTurningOffScreen = true; // Issue #68. Avoid interrupting the thread if screen is already being turned off.
        mLastTimeScreenOnOrOff = System.currentTimeMillis();
        if (mSettings.isVibrateWhileLocking()) {
            vibrate();
        }
        Log.i(TAG, "Switched from 'far' to 'near'.");
        if (mSettings.isLockScreenWithPowerButton()) {
            Log.i(TAG, "Turning screen off simulating power button press.");
            Root.pressPowerButton();
        } else {
            Log.i(TAG, "Turning screen off.");
            try {
                mPolicyManager.lockNow();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Failed to run lockNow() to turn off the screen. Probably due to an ongoing call. Exception: " + e);
            } catch (SecurityException e) {
                Log.e(TAG, "Failed to run lockNow() to turn off the screen. Probably due to missing device admin rights, which I don't really understand... Exception: " + e);
            }
        }
        mIsTurningOffScreen = false;
    }

    public void turnOffScreen() {
        if (mWaveUpWorldState.isScreenOn()) {
            if (mSettings.isVibrateWhileLocking()) {
                vibrate();
            }
            mTurnOffScreenThread = turnOffScreenThread(mSettings.getSensorCoverTimeBeforeLockingScreen());
            mTurnOffScreenThread.start();
        }
    }

    public void cancelTurnOff() {
        if (mTurnOffScreenThread != null && !mIsTurningOffScreen) {
            Log.d(TAG, "Cancelling turning off of display");
            mTurnOffScreenThread.interrupt();
            mTurnOffScreenThread = null;
        }
    }

    public long getLastTimeScreenOnOrOff() {
        return mLastTimeScreenOnOrOff;
    }

    public void turnOnScreen() {
        if (!mWaveUpWorldState.isScreenOn()) {
            mLastTimeScreenOnOrOff = System.currentTimeMillis();
            Log.i(TAG, "Switched from 'near' to 'far'. Turning screen on");
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            mWakeLock.acquire(TIME_SCREEN_ON);
        }
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) this.mContext.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(50);
    }
}