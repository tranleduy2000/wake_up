package com.duy.wakeup.activities;


import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.duy.common.utils.DLog;
import com.duy.wakeup.R;
import com.duy.wakeup.manager.WakeUpSettings;
import com.duy.wakeup.receivers.LockScreenAdminReceiver;
import com.duy.wakeup.settings.SettingsFragment;

/**
 * Created by Duy on 24-Dec-17.
 */

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final int UNINSTALL_REQUEST_CODE = 200;
    private static final int UNINSTALL_CANCELED_MSG_SHOW_TIME = 5000;
    private static final int UNINSTALL_CANCELED_MSG_SHOW_INTERVAL = 1000;
    private static boolean removeAdminRights = false;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

//        FragmentManager fragmentManager = getFragmentManager();
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        transaction.replace(R.id.content, SettingsFragment.newInstance()).commitAllowingStateLoss();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, SettingsFragment.newInstance()).commitAllowingStateLoss();


        findViewById(R.id.fab).setOnClickListener(this);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
//        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        int primaryDark = getResources().getColor(R.color.colorPrimaryDark);
        int primary = getResources().getColor(R.color.colorPrimary);
        int accent = getResources().getColor(R.color.colorAccent);

        collapsingToolbarLayout.setContentScrimColor((primary));
        collapsingToolbarLayout.setStatusBarScrimColor((primary));

        showInitialDialog();
    }


    private void showInitialDialog() {
        if (!getSettings().isInitialDialogShown()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.alert_dialog_title);
            builder.setMessage(R.string.alert_dialog_message);
            builder.setPositiveButton(R.string.alert_dialog_ok_button, null);
            builder.show();

            getSettings().setInitialDialogShown(true);
        }
    }

    private void uninstallApp() {
        if (getSettings().isLockScreenAdmin()) {
            removeDeviceAdminPermission();
            removeAdminRights = true;
        }

        DLog.i(TAG, "Uninstalling app");
        Uri packageURI = Uri.parse("package:" + "com.duy.wakeup");
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        startActivityForResult(uninstallIntent, UNINSTALL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case UNINSTALL_REQUEST_CODE:
                if (resultCode == RESULT_CANCELED && removeAdminRights) {
                    final Toast canceledMsg = Toast.makeText(this,
                            R.string.removed_device_admin_rights, Toast.LENGTH_SHORT);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.revoke_device_admin_menu_item:
                removeDeviceAdminPermission();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void removeDeviceAdminPermission() {
        DLog.i(TAG, "Removing lock screen admin rights");
        ComponentName devAdminReceiver = new ComponentName(this, LockScreenAdminReceiver.class);
        DevicePolicyManager dpm = (DevicePolicyManager) this.getSystemService(Context.DEVICE_POLICY_SERVICE);
        dpm.removeActiveAdmin(devAdminReceiver);

        // If the user cancels the uninstall he/she will have to switch it back on (to request the admin rights again)
        getSettings().setLockScreen(false);
    }

    private WakeUpSettings getSettings() {
        return WakeUpSettings.getInstance(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {
            uninstallApp();
        }
    }
}
