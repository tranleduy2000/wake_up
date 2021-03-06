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

package com.duy.wakeup.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import com.duy.common.utils.DLog;
import com.duy.wakeup.manager.ProximitySensorManager;

public class CallStateReceiver extends BroadcastReceiver {
    private static final String TAG = "CallStateReceiver";
    private static Boolean ongoingCall = false; // Unfortunately I cannot read this live, so I'll just assume there is no ongoing call the first time
    private static String lastState = "";

    public static boolean isOngoingCall() {
        return ongoingCall;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if (!intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                DLog.w(TAG, "Avoid intent spoofing (intent '" + intent.getAction() + "')");
                return;
            }
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String state = extras.getString(TelephonyManager.EXTRA_STATE);
                if (state != null) {
                    DLog.v(TAG, "Call state: " + state);
                    if (!state.equals(lastState)) { // States seem to show twice (once with number and once without number) I can ignore consecutive states that are equal.
                        lastState = state;
                        if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                            ongoingCall = false;
                            DLog.d(TAG, "Finished call.");
                        } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) ||
                                state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                            ongoingCall = true;
                            DLog.d(TAG, "Ongoing call.");
                        }
                    }
                    ProximitySensorManager.getInstance(context).startOrStopListeningDependingOnConditions();
                }
            }
        } else {
            return;
        }
    }
}