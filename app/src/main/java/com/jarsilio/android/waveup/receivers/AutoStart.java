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

package com.jarsilio.android.waveup.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jarsilio.android.waveup.Settings;
import com.jarsilio.android.waveup.WaveUpService;

public class AutoStart extends BroadcastReceiver {
    private static final String TAG = "AutoStart";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Settings.getInstance(context).isServiceEnabled()) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                Log.d(TAG, "Received ACTION_BOOT_COMPLETED.");
                startWaveUpService(context);
            } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
                Log.d(TAG, "Received ACTION_PACKAGE_REPLACED.");
                String upgradedPackage = intent.getData().getSchemeSpecificPart().replace("package:", "");
                if (context.getPackageName().equals(upgradedPackage)) {
                    Log.d(TAG, "The upgraded app was WaveUp.");
                    startWaveUpService(context);
                }
            }
        }
    }

    private void startWaveUpService(Context context) {
        Log.i(TAG, "Starting WaveUp");
        context.startService(new Intent(context, WaveUpService.class));
    }
}
