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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Locale;

public class ProximitySensorManager implements SensorEventListener {
    private static final String TAG = "ProximitySensorManager";
    private static final long WAVE_THRESHOLD = 2000;
    private static final long POCKET_THRESHOLD = 5000;
    private static final long MIN_TIME_BETWEEN_SCREEN_ON_AND_OFF = 1500;
    private static volatile ProximitySensorManager instance;

    private final SensorManager mSensorManager;
    private final Sensor mProximitySensor;
    private final Context mContext;
    private final WakeUpScreenHandler mScreenHandler;
    private final WakeUpSettings mSettings;
    private int mWaveCount = 0;
    private long mLastWaveTime = 0;
    private Distance mLastDistance = Distance.FAR;
    private long mLastTime = 0;
    private boolean mIsListening = false;

    private ProximitySensorManager(Context context) {
        this.mContext = context.getApplicationContext();
        this.mScreenHandler = WakeUpScreenHandler.getInstance(context);
        this.mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        this.mSettings = WakeUpSettings.getInstance(context);
        start();
    }

    public static ProximitySensorManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ProximitySensorManager.class) {
                if (instance == null) {
                    instance = new ProximitySensorManager(context);
                }
            }
        }

        return instance;
    }

    private void start() {
        if (!mIsListening) {
            Log.d(TAG, "Registering proximity sensor listener.");
            mSensorManager.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            mIsListening = true;
        } else {
            Log.d(TAG, "Proximity sensor listener is already registered. There is no need to register it again.");
        }
    }

    public final void startOrStopListeningDependingOnConditions() {
        WaveUpWorldState waveUpWorldState = new WaveUpWorldState(mContext);
        boolean startAllowedByWaveOrLockModes =
                (!waveUpWorldState.isScreenOn() && (mSettings.isPocketMode() || mSettings.isWaveMode())) ||
                        (waveUpWorldState.isScreenOn() && mSettings.isLockScreen() && mSettings.isLockScreenAdmin());
        boolean startAllowedByOrientation = mSettings.isLockScreenWhenLandscape() || waveUpWorldState.isPortrait()
                || !waveUpWorldState.isScreenOn();
        boolean startAllowedByNoOngoingCall = !waveUpWorldState.isOngoingCall();

        Log.v(TAG, String.format(
                "start because of wave or lock modes: %s\n" +
                        "start because of orientation: %s\n" +
                        "start because of no ongoing call: %s",
                startAllowedByWaveOrLockModes,
                startAllowedByOrientation,
                startAllowedByNoOngoingCall));

        boolean start = startAllowedByWaveOrLockModes && startAllowedByOrientation && startAllowedByNoOngoingCall;

        if (start) {
            Log.d(TAG, "Starting because an event happened and the world in combination with the settings say I should start listening");
            start();
        } else {
            Log.d(TAG, "Stopping because an event happened and the world in combination with the settings say I should stop listening");
            stop();
        }
    }

    public final void stop() {
        WakeUpScreenHandler.getInstance(mContext).cancelTurnOff();
        if (mIsListening) {
            Log.d(TAG, "Unregistering proximity sensor listener");
            mSensorManager.unregisterListener(this);
            mIsListening = false;
        } else {
            Log.d(TAG, "Proximity sensor listener is already unregistered. There is no need to unregister it again.");
        }
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        long currentTime = System.currentTimeMillis();
        Distance currentDistance = event.values[0] >= event.sensor.getMaximumRange() ? Distance.FAR : Distance.NEAR;
        Log.v(TAG, String.format(Locale.ENGLISH, "Proximity sensor changed: %s (current sensor value: %f - max. sensor value: %f)", currentDistance, event.values[0], event.sensor.getMaximumRange()));

        // If the sensor gets uncovered, there is possibly a thread waiting to turn off the screen. It needs to be interrupted.
        if (currentDistance == Distance.FAR) {
            mScreenHandler.cancelTurnOff();
        }

        boolean uncovered = mLastDistance == Distance.NEAR && currentDistance == Distance.FAR;
        boolean covered = mLastDistance == Distance.FAR && currentDistance == Distance.NEAR;

        long timeBetweenFarAndNear = currentTime - mLastTime;
        if (uncovered) {
            Log.v(TAG, "Just uncovered. Time it was covered: " + timeBetweenFarAndNear);
        } else {
            Log.v(TAG, "Just covered. Time it was uncovered: " + timeBetweenFarAndNear);
        }


        boolean waved = timeBetweenFarAndNear <= WAVE_THRESHOLD;
        boolean tookOutOfPocket = timeBetweenFarAndNear > POCKET_THRESHOLD;

        if (uncovered) {
            long timeSinceLastScreenOnOrOff = currentTime - mScreenHandler.getLastTimeScreenOnOrOff();
            if (timeSinceLastScreenOnOrOff > MIN_TIME_BETWEEN_SCREEN_ON_AND_OFF) { // Don't do anything if it turned on or off 1.5 seconds ago
                if (waved && mSettings.isWaveMode()) {
                    if (currentTime - mLastWaveTime > WAVE_THRESHOLD) {
                        mWaveCount = 0; // Reset wave count the last wave was a long time ago (in this case 2 seconds) - Will only switch on screen if waves happen within 2 seconds
                    }

                    mWaveCount++;
                    Log.v(TAG, "Waved. waveCount: " + mWaveCount + "");
                    Log.v(TAG, "Time between waves was: " + (currentTime - mLastWaveTime) + " (will only switch on screen if waves happen within 2 seconds)");
                    mLastWaveTime = System.currentTimeMillis();
                    long minWaves = mSettings.getNumberOfWavesToWaveUp() - 1;
                    if (mWaveCount > minWaves) {
                        mScreenHandler.turnOnScreen();
                        mWaveCount = 0;
                    }
                } else if (tookOutOfPocket && mSettings.isPocketMode()) {
                    mScreenHandler.turnOnScreen();
                }
            } else {
                Log.d(TAG, "Time since last screen off: " + timeSinceLastScreenOnOrOff + ". Not switching it on");
            }
        } else if (covered) {
            mScreenHandler.turnOffScreen();
        }

        mLastDistance = currentDistance;
        mLastTime = currentTime;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int i) {
    }


    private enum Distance {NEAR, FAR}
}