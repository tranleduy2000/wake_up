/*
 * Copyright (c) 2016 Juan García Basilio
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
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;

import com.duy.wakeup.receivers.CallStateReceiver;
import com.google.firebase.analytics.FirebaseAnalytics;

public class WaveUpWorldState {
    private static final String TAG = "WaveUpWorldState";
    private final Context mContext;

    public WaveUpWorldState(Context context) {
        this.mContext = context;
    }

    public boolean isScreenOn() {
        boolean screenOn = false;
        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) { // isScreenOn method is deprecated from API Level 20
                screenOn = powerManager.isInteractive();
            } else {
                screenOn = powerManager.isScreenOn();
            }
        } else {
            FirebaseAnalytics.getInstance(mContext).logEvent("PowerManager_not_found", new Bundle());
        }
        return screenOn;
    }

    public boolean isPortrait() {
        int orientation = mContext.getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_PORTRAIT || orientation == Configuration.ORIENTATION_UNDEFINED;
    }

    public boolean isOngoingCall() {
        return CallStateReceiver.isOngoingCall();
    }
}
