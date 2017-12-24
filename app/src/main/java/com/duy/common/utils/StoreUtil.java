/*
 * Copyright (c) 2017 by Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.common.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

/**
 * Created by Duy on 10-Jul-17.
 */

public class StoreUtil {
    private static final String TAG = "StoreUtil";

    public static void gotoPlayStore(Activity context, String appId) {
        DLog.d(TAG, "gotoPlayStore() called with: context = [" + context + "], appId = [" + appId + "]");
        Uri uri = Uri.parse(String.format("market://details?id=%s", appId));
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |

                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Uri link = Uri.parse("http://play.google.com/store/apps/details?id=" + appId);
            Intent intent = new Intent(Intent.ACTION_VIEW, link);
            context.startActivity(intent);
        }
    }

    public static void gotoPlayStore(Activity context, String APP_ID, int request) {
        Uri uri = Uri.parse("market://details?id=" + APP_ID);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            String uriString = "http://play.google.com/store/apps/details?id=" + APP_ID;
            gotoToLink(context, uriString, request);
        }
    }

    public static void gotoToLink(Activity context, String uriString, int request) {
        Uri link = Uri.parse(uriString);
        Intent intent = new Intent(Intent.ACTION_VIEW, link);
        context.startActivityForResult(intent, request);
    }

    public static void shareApp(Activity context, String appId) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "http://play.google.com/store/apps/details?id=" + appId);
        intent.setType("text/plain");
        context.startActivity(intent);
    }

    public static void shareThisApp(Activity context) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "http://play.google.com/store/apps/details?id=" +
                context.getPackageName());
        intent.setType("text/plain");
        context.startActivity(intent);
    }

    public static void moreApp(Activity mainActivity) {
        String location = "https://play.google.com/store/apps/dev?id=7055567654109499514";
        Uri uri = Uri.parse(location);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            mainActivity.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            mainActivity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(location)));
        }
    }

    public static void emailToDevelop(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"tranleduy1233@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Suggestion for unit converter");
        try {
            activity.startActivity(Intent.createChooser(intent, "Send mail..."));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(activity, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
