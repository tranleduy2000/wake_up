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

package com.duy.wakeup.settings;

import android.Manifest;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.duy.common.utils.DLog;
import com.duy.wakeup.R;
import com.duy.wakeup.manager.ProximitySensorManager;
import com.duy.wakeup.manager.WakeUpSettings;
import com.duy.wakeup.receivers.LockScreenAdminReceiver;
import com.duy.wakeup.root.Root;
import com.duy.wakeup.services.WaveUpService;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompatDividers;

public class SettingsFragment extends PreferenceFragmentCompatDividers implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "MainActivity";
    private static final int ADD_DEVICE_ADMIN_REQUEST_CODE = 1;
    private static final int READ_PHONE_STATE_PERMISSION_REQUEST_CODE = 300;
    private static final String[] READ_PHONE_STATE_PERMISSION = {"android.permission.READ_PHONE_STATE"};

    public static SettingsFragment newInstance() {

        Bundle args = new Bundle();

        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        adaptToNewMultipleWaveOption();
        createLayout();
        startService();
        registerPreferencesListener();
    }

    private void adaptToNewMultipleWaveOption() {
        if (!getSettings().isAdaptedToNewMultipleWaveOption()) {
            /*
               Yes, this is a dirty hack!
               If it is a new installation it will already be adapted and shouldn't be changed on the
               next upgrade (see NewMultipleWaveOptionAdapter) so we set it to true.
               It it were an old installation, this wouldn't exist and NewMultipleWaveOptionAdapter
               would be executed during the next upgrade
               Warning: This will only work if the app is opened at least once in between upgrades.
            */
            getSettings().setAdaptedToNewMultipleWaveOption(true);
        }
    }

    private WakeUpSettings getSettings() {
        return WakeUpSettings.getInstance(getContext());
    }

    private void createLayout() {
        addPreferencesFromResource(R.xml.preferences_settings);
        onSharedPreferenceChanged(getSettings().getPreferences(),
                WakeUpSettings.KEY_SENSOR_COVER_TIME_BEFORE_LOCKING_SCREEN); // Work-around to set the summary of the option every time the Main Activity is shown

        findPreference("prefs_general").setVisible(false);
        int preferences_settings2 = R.xml.preferences_settings2;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView listView = getListView();
        ViewCompat.setNestedScrollingEnabled(listView, false);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        ProximitySensorManager.getInstance(getContext()).startOrStopListeningDependingOnConditions();
        switch (key) {
            case WakeUpSettings.KEY_ENABLED:
                if (getSettings().isServiceEnabled()) {
                    requestReadPhoneStatePermission();
                }
                startService();
            case WakeUpSettings.KEY_LOCK_SCREEN:
                if (getSettings().isLockScreen() && !getSettings().isLockScreenAdmin()) {
                    openRequestAdminRightsYesNoDialog();
                }
                break;
            case WakeUpSettings.KEY_LOCK_SCREEN_WITH_POWER_BUTTON:
                if (getSettings().isLockScreenWithPowerButton()) {
                    if (!Root.requestSuPermission()) {
                        getSettings().setLockScreenWithPowerButton(false);
                        Toast.makeText(getContext(), R.string.root_access_failed, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case WakeUpSettings.KEY_SENSOR_COVER_TIME_BEFORE_LOCKING_SCREEN:
                Preference preference = findPreference(key);
                ListPreference listPreference = (ListPreference) preference;
                preference.setSummary(String.format(getResources().getString(R.string.pref_sensor_cover_time_before_locking_screen_summary), listPreference.getEntry()));
                break;
            case WakeUpSettings.KEY_NUMBER_OF_WAVES:
                Preference numberOfWavesPreference = findPreference(key);
                ListPreference numberOfWavesListPreference = (ListPreference) numberOfWavesPreference;
                numberOfWavesPreference.setSummary(String.format(getResources().getString(R.string.pref_number_of_waves_summary), numberOfWavesListPreference.getEntry()));
                break;
        }
    }

    @Override
    public Activity getContext() {
        return getActivity();
    }

    private void startService() {
        if (getSettings().isServiceEnabled()) {
            DLog.i(TAG, "Starting WaveUpService");
            getContext().startService(new Intent(getContext(), WaveUpService.class));
            if (getSettings().isShowStartedServiceToast()) {
                Toast.makeText(getContext(), R.string.wave_up_service_started, Toast.LENGTH_SHORT).show();
                getSettings().setShowStartedServiceToast(false);
            }
        } else {
            DLog.i(TAG, "Stopping WaveUpService");
            getContext().stopService(new Intent(getContext(), WaveUpService.class));
            if (!getSettings().isShowStartedServiceToast()) {
                Toast.makeText(getContext(), R.string.wave_up_service_stopped, Toast.LENGTH_SHORT).show();
                getSettings().setShowStartedServiceToast(true);
            }
        }
    }

    private void requestReadPhoneStatePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), READ_PHONE_STATE_PERMISSION,
                    READ_PHONE_STATE_PERMISSION_REQUEST_CODE);
        }
    }

    private void openRequestAdminRightsYesNoDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        requestLockScreenAdminRights();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        getSettings().setLockScreen(false);
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.lock_admin_rights_explanation).setPositiveButton(android.R.string.yes, dialogClickListener)
                .setNegativeButton(android.R.string.no, dialogClickListener).show();
    }

    private void requestLockScreenAdminRights() {
        ComponentName lockScreenAdminComponentName = new ComponentName(getContext(), LockScreenAdminReceiver.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, lockScreenAdminComponentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, R.string.lock_admin_rights_explanation);
        startActivityForResult(intent, ADD_DEVICE_ADMIN_REQUEST_CODE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ADD_DEVICE_ADMIN_REQUEST_CODE:
                if (!getSettings().isLockScreenAdmin()) {
                    // If the user does not activate lock admin switch off lock screen option
                    getSettings().setLockScreen(false);
                } else {
                    ProximitySensorManager.getInstance(getContext()).startOrStopListeningDependingOnConditions();
                }
                break;

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerPreferencesListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterPreferencesListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterPreferencesListener();
    }

    private void registerPreferencesListener() {
        getSettings().getPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    private void unregisterPreferencesListener() {
        getSettings().getPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
