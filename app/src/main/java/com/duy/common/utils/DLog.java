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

import android.support.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.duy.wakeup.BuildConfig;

/**
 * Setup Crashlytics https://firebase.google.com/docs/crashlytics/get-started?authuser=0
 * <p>
 * Created by Duy on 27-Mar-17.
 * Lasted update 03/12/2017
 */
public class DLog {
    private static final String TAG = "DLog";
    public static boolean ANDROID = true;
    private static boolean DEBUG = BuildConfig.DEBUG;

    /**
     * Debug log
     * <p>
     * -
     */
    public static void d(Object msg) {
        if (DEBUG) {
            if (ANDROID) {
                Log.d(TAG, msg.toString());
            } else {
                System.out.println(TAG + ": " + msg.toString());
            }
        }
    }

    /**
     * debug log
     */
    public static void d(String TAG, Object msg) {
        if (DEBUG) {
            if (ANDROID) {
                Log.d(TAG, msg.toString());
            } else {
                System.out.println(TAG + ": " + msg.toString());
            }
        }
    }

    /**
     * warning log
     */
    public static void w(Object msg) {
        if (DEBUG) {
            if (ANDROID) {
                Log.w(TAG, msg.toString());
            } else {
                System.out.println(TAG + ": " + msg.toString());
            }
        }
    }

    /**
     * warning log
     */
    public static void w(String TAG, Object msg) {
        if (DEBUG) {
            if (ANDROID) {
                Log.w(TAG, msg.toString());
            } else {
                System.out.println(TAG + ": " + msg.toString());
            }
        }
    }

    /**
     * Error log
     */
    public static void e(Exception exception) {
        if (DEBUG) {
            if (ANDROID) {
                Log.e(TAG, "Error ", exception);
            } else {
                System.err.println(TAG + ": " + exception.toString());
            }
        }
    }

    /**
     * Error log
     */
    @Deprecated
    public static void e(String TAG, Exception exception) {
        if (DEBUG) {
            if (ANDROID) {
                Log.e(TAG, "Error ", exception);
            } else {
                System.err.println(TAG + ": " + exception.toString());
            }
        }
    }

    /**
     * error log
     */
    public static void e(@NonNull String TAG, @NonNull String exception) {
        if (DEBUG) {
            if (ANDROID) {
                Log.e(TAG, exception);
            } else {
                System.err.println(TAG + ": " + exception);
            }
        }
    }

    /**
     * Error log
     */
    public static void e(@NonNull String TAG, @NonNull String msg, @NonNull Exception e) {
        if (DEBUG) {
            if (ANDROID) {
                Log.e(TAG, msg, e);
            } else {
                System.err.println(TAG + ": " + msg);
                e.printStackTrace();
            }
        }
    }

    /**
     * info log
     */
    public static void i(@NonNull Object msg) {
        if (DEBUG) {
            if (ANDROID) {
                Log.i(TAG, msg.toString());
            } else {
                System.out.println(TAG + ": " + msg.toString());
            }
        }
    }

    /**
     * info log
     *
     * @param tag
     */
    public static void i(String tag, @NonNull Object msg) {
        if (DEBUG) {
            if (ANDROID) {
                Log.i(tag, msg.toString());
            } else {
                System.err.println(tag + ": " + msg);
            }
        }
    }
    /**
     * info log
     *
     * @param tag
     */
    public static void v(String tag, @NonNull Object msg) {
        if (DEBUG) {
            if (ANDROID) {
                Log.v(tag, msg.toString());
            } else {
                System.err.println(tag + ": " + msg);
            }
        }
    }
    @Deprecated
    public static void reportException(Throwable e) {
       /* FirebaseCrash.report(e);*/
        reportServer(e);
    }

    /**
     * Report an error to firebase server
     *
     * @param throwable - any error
     */
    public static void reportServer(Throwable throwable) {
        if (ANDROID) {
            Crashlytics.logException(throwable);
        } else {
            System.err.println("Fatal exception : ");
            throwable.printStackTrace();
        }
    }


}
