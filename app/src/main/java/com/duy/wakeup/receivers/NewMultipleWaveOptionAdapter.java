/*
 * Copyright (c) 2017 Juan García Basilio
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

import com.duy.common.utils.DLog;
import com.duy.wakeup.manager.WakeUpSettings;

public class NewMultipleWaveOptionAdapter extends BroadcastReceiver {
    private static final String TAG = "NewMultipleWaveOption";

    private void setDefaultValueForDoubleWave(Context context, WakeUpSettings settings) {
        DLog.d(TAG, "Probably just replaced a pre-number-of-waves version of WaveUp. Adapting to new option accordingly: " +
                "Setting NUMBER_OF_WAVES to 1 if 'wave mode' was on and to 2 if it wasn't...");
        if (WakeUpSettings.getInstance(context).isWaveMode()) {
            DLog.d(TAG, "'Wave mode' is on. Setting NUMBER_OF_WAVES to 1 so that it works as before");
            WakeUpSettings.getInstance(context).setNumberOfWaves(1);
        } else {
            DLog.d(TAG, "'Wave mode' is off. Setting NUMBER_OF_WAVES to 2, the new standard to avoid accidental waving up");
            WakeUpSettings.getInstance(context).setNumberOfWaves(2);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
            String upgradedPackage = intent.getData().getSchemeSpecificPart().replace("package:", "");
            if (context.getPackageName().equals(upgradedPackage)) {
                WakeUpSettings settings = WakeUpSettings.getInstance(context);

                if (!settings.isAdaptedToNewMultipleWaveOption()) {
                    setDefaultValueForDoubleWave(context, settings);
                    settings.setAdaptedToNewMultipleWaveOption(true);
                }
            }
        }
    }
}
