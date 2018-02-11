/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Gionee: 20120918 chenrui add for CR00696600 begin */

package com.android.mms.transaction;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony.Sms.Intents;
import android.os.PowerManager;
import android.util.Log;
import com.android.mms.MmsApp;
import com.aurora.featureoption.FeatureOption;
import android.os.SystemProperties;


/**
 * Handle incoming SMSes.  Just dispatches the work off to a Service.
 */
public class AlertMissMsgReceiver extends BroadcastReceiver {
    static final Object mStartingServiceSync = new Object();
    static PowerManager.WakeLock mStartingService;
    private static AlertMissMsgReceiver sInstance;

    private static final boolean gnAlertMissMsgSupport = SystemProperties.get("ro.gn.mms.alertMissMsg", "no").equals("yes");

    public static AlertMissMsgReceiver getInstance() {
        if (sInstance == null) {
            sInstance = new AlertMissMsgReceiver();
        }
        return sInstance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (gnAlertMissMsgSupport) {
            intent.setClass(context, AlertMissMsgReceiverService.class);
            beginStartingService(context, intent);
        }
    }

    // N.B.: <code>beginStartingService</code> and
    // <code>finishStartingService</code> were copied from
    // <code>com.android.calendar.AlertReceiver</code>.  We should
    // factor them out or, even better, improve the API for starting
    // services under wake locks.

    /**
     * Start the service to process the current event notifications, acquiring
     * the wake lock before returning to ensure that the service will run.
     */
    public static void beginStartingService(Context context, Intent intent) {
        synchronized (mStartingServiceSync) {
            if (mStartingService == null) {
                PowerManager pm =
                    (PowerManager)context.getSystemService(Context.POWER_SERVICE);
                mStartingService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "StartingAlertService");
                mStartingService.setReferenceCounted(false);
            }
            mStartingService.acquire();
            context.startService(intent);
        }
    }

    /**
     * Called back by the service when it has finished processing notifications,
     * releasing the wake lock if the service is now stopping.
     */
    public static void finishStartingService(Service service, int startId) {
        synchronized (mStartingServiceSync) {
            if (mStartingService != null) {
                if (service.stopSelfResult(startId)) {
                        mStartingService.release();
                }

            }
        }
    }

}

/* Gionee: 20120918 chenrui add for CR00696600 end */
