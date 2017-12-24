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

package com.duy.wakeup.activities;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.duy.wakeup.BuildConfig;
import com.duy.wakeup.manager.ProximitySensorManager;
import com.duy.wakeup.manager.WakeUpSettings;
import com.duy.wakeup.receivers.LockScreenAdminReceiver;
import com.duy.wakeup.root.Root;
import com.duy.wakeup.services.WaveUpService;

public class MainActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "MainActivity";

    private static final int ADD_DEVICE_ADMIN_REQUEST_CODE = 1;
    private static final int UNINSTALL_REQUEST_CODE = 200;
    private static final int READ_PHONE_STATE_PERMISSION_REQUEST_CODE = 300;
    private static final int UNINSTALL_CANCELED_MSG_SHOW_TIME = 5000;
    private static final int UNINSTALL_CANCELED_MSG_SHOW_INTERVAL = 1000;
    private static final String[] READ_PHONE_STATE_PERMISSION = {"android.permission.READ_PHONE_STATE"};
    private static boolean removeAdminRights = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Starting WaveUp MainActivity (GUI)");
        showInitialDialog();
        adaptToNewMultipleWaveOption();
        getSettings().setPreferenceActivity(this);
        super.onCreate(savedInstanceState);
        createLayout();
        startService();
        registerPreferencesListener();
    }

    private void showInitialDialog() {
        if (!getSettings().isInitialDialogShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(com.duy.wakeup.R.string.alert_dialog_title);
            builder.setMessage(com.duy.wakeup.R.string.alert_dialog_message);
            builder.setPositiveButton(com.duy.wakeup.R.string.alert_dialog_ok_button, null);
            builder.show();

            getSettings().setInitialDialogShown(true);
        }
    }

    private void showPrivacyPolicyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(com.duy.wakeup.R.string.privacy_policy_dialog_title);

        if (BuildConfig.BUILD_TYPE.equals("releaseGoogle")) {
            builder.setMessage(com.duy.wakeup.R.string.privacy_policy_google_play_store_dialog_text);
        } else {
            builder.setMessage(com.duy.wakeup.R.string.privacy_policy_f_droid_dialog_text);
        }

        builder.setPositiveButton(android.R.string.ok, null);

        AlertDialog dialog = builder.show();

        // Work-around to make links clickable (don't ask me why this works):
        // See: https://stackoverflow.com/questions/1997328/how-can-i-get-clickable-hyperlinks-in-alertdialog-from-a-string-resource
        if (Build.VERSION.SDK_INT > 8) {
            ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        }
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
        return WakeUpSettings.getInstance(getApplicationContext());
    }

    private void createLayout() {
        addPreferencesFromResource(com.duy.wakeup.R.xml.settings);

        Button uninstallButton = new Button(getApplicationContext());
        uninstallButton.setText(com.duy.wakeup.R.string.uninstall_button);
        uninstallButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                uninstallApp();
            }
        });

        ListView listView = getListView();
        listView.addFooterView(uninstallButton);
        onSharedPreferenceChanged(getSettings().getPreferences(), WakeUpSettings.SENSOR_COVER_TIME_BEFORE_LOCKING_SCREEN); // Work-around to set the summary of the option every time the Main Activity is shown
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.duy.wakeup.R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case com.duy.wakeup.R.id.privacy_policy_menu_item:
                showPrivacyPolicyDialog();
                break;
            case com.duy.wakeup.R.id.revoke_device_admin_menu_item:
                removeDeviceAdminPermission();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    @Override
    protected boolean isValidFragment(String fragmentName) {
        boolean isValidFragment = true;

        if (Build.VERSION.SDK_INT >= 11) {
            isValidFragment = PreferenceFragment.class.getName().equals(fragmentName);
        }

        return isValidFragment;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        ProximitySensorManager.getInstance(getApplicationContext()).startOrStopListeningDependingOnConditions();
        switch (key) {
            case WakeUpSettings.ENABLED:
                if (getSettings().isServiceEnabled()) {
                    requestReadPhoneStatePermission();
                }
                startService();
            case WakeUpSettings.LOCK_SCREEN:
                if (getSettings().isLockScreen() && !getSettings().isLockScreenAdmin()) {
                    openRequestAdminRightsYesNoDialog();
                }
                break;
            case WakeUpSettings.LOCK_SCREEN_WITH_POWER_BUTTON:
                if (getSettings().isLockScreenWithPowerButton()) {
                    if (!Root.requestSuPermission()) {
                        getSettings().setLockScreenWithPowerButton(false);
                        Toast.makeText(this, com.duy.wakeup.R.string.root_access_failed, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case WakeUpSettings.SENSOR_COVER_TIME_BEFORE_LOCKING_SCREEN:
                Preference preference = findPreference(key);
                ListPreference listPreference = (ListPreference) preference;
                preference.setSummary(String.format(getResources().getString(com.duy.wakeup.R.string.pref_sensor_cover_time_before_locking_screen_summary), listPreference.getEntry()));
                break;
            case WakeUpSettings.NUMBER_OF_WAVES:
                Preference numberOfWavesPreference = findPreference(key);
                ListPreference numberOfWavesListPreference = (ListPreference) numberOfWavesPreference;
                numberOfWavesPreference.setSummary(String.format(getResources().getString(com.duy.wakeup.R.string.pref_number_of_waves_summary), numberOfWavesListPreference.getEntry()));
                break;
        }
    }

    private void startService() {
        if (getSettings().isServiceEnabled()) {
            Log.i(TAG, "Starting WaveUpService");
            startService(new Intent(this, WaveUpService.class));
            if (getSettings().isShowStartedServiceToast()) {
                Toast.makeText(this, com.duy.wakeup.R.string.wave_up_service_started, Toast.LENGTH_SHORT).show();
                getSettings().setShowStartedServiceToast(false);
            }
        } else {
            Log.i(TAG, "Stopping WaveUpService");
            stopService(new Intent(this, WaveUpService.class));
            if (!getSettings().isShowStartedServiceToast()) {
                Toast.makeText(this, com.duy.wakeup.R.string.wave_up_service_stopped, Toast.LENGTH_SHORT).show();
                getSettings().setShowStartedServiceToast(true);
            }
        }
    }

    private void requestReadPhoneStatePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, READ_PHONE_STATE_PERMISSION, READ_PHONE_STATE_PERMISSION_REQUEST_CODE);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(com.duy.wakeup.R.string.lock_admin_rights_explanation).setPositiveButton(android.R.string.yes, dialogClickListener)
                .setNegativeButton(android.R.string.no, dialogClickListener).show();
    }

    private void requestLockScreenAdminRights() {
        ComponentName lockScreenAdminComponentName = new ComponentName(getApplicationContext(), LockScreenAdminReceiver.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, lockScreenAdminComponentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, com.duy.wakeup.R.string.lock_admin_rights_explanation);
        startActivityForResult(intent, ADD_DEVICE_ADMIN_REQUEST_CODE);
    }

    private void removeDeviceAdminPermission() {
        Log.i(TAG, "Removing lock screen admin rights");
        ComponentName devAdminReceiver = new ComponentName(getApplicationContext(), LockScreenAdminReceiver.class);
        DevicePolicyManager dpm = (DevicePolicyManager) getApplicationContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        dpm.removeActiveAdmin(devAdminReceiver);

        // If the user cancels the uninstall he/she will have to switch it back on (to request the admin rights again)
        getSettings().setLockScreen(false);
    }

    private void uninstallApp() {
        if (getSettings().isLockScreenAdmin()) {
            removeDeviceAdminPermission();
            removeAdminRights = true;
        }

        Log.i(TAG, "Uninstalling app");
        Uri packageURI = Uri.parse("package:" + "com.duy.wakeup");
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        startActivityForResult(uninstallIntent, UNINSTALL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ADD_DEVICE_ADMIN_REQUEST_CODE:
                if (!getSettings().isLockScreenAdmin()) {
                    // If the user does not activate lock admin switch off lock screen option
                    getSettings().setLockScreen(false);
                } else {
                    ProximitySensorManager.getInstance(getApplicationContext()).startOrStopListeningDependingOnConditions();
                }
                break;
            case UNINSTALL_REQUEST_CODE:
                if (resultCode == RESULT_CANCELED && removeAdminRights) {
                    final Toast canceledMsg = Toast.makeText(this, com.duy.wakeup.R.string.removed_device_admin_rights, Toast.LENGTH_SHORT);
                    canceledMsg.show();
                    /* Show message UNINSTALL_CANCELED_MSG_SHOW_TIME second */
                    new CountDownTimer(UNINSTALL_CANCELED_MSG_SHOW_TIME, UNINSTALL_CANCELED_MSG_SHOW_INTERVAL) {
                        public void onTick(long millisUntilFinished) {
                            canceledMsg.show();
                        }

                        public void onFinish() {
                            canceledMsg.cancel();
                        }
                    }.start();
                    removeAdminRights = false;
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerPreferencesListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterPreferencesListener();
    }

    @Override
    protected void onDestroy() {
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
