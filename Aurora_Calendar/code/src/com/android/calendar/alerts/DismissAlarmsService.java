/*
 * Copyright (C) 2009 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.calendar.alerts;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.CalendarContract.CalendarAlerts;
import android.support.v4.app.TaskStackBuilder;

import com.android.calendar.EventInfoActivity;
import com.aurora.calendar.AuroraBirthdayInfoActivity;
import com.mediatek.calendar.LogUtil;
import com.mediatek.calendar.MTKUtils;

/**
 * Service for asynchronously marking fired alarms as dismissed.
 */
public class DismissAlarmsService extends IntentService {
    private static final String TAG = "DismissAlarmsService";
    private static final String[] PROJECTION = new String[] {
            CalendarAlerts.STATE,
    };
    private static final int COLUMN_INDEX_STATE = 0;

    public DismissAlarmsService() {
        super("DismissAlarmsService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onHandleIntent(Intent intent) {

        long eventId = intent.getLongExtra(AlertUtils.EVENT_ID_KEY, -1);
        long eventStart = intent.getLongExtra(AlertUtils.EVENT_START_KEY, -1);
        long eventEnd = intent.getLongExtra(AlertUtils.EVENT_END_KEY, -1);
        boolean showEvent = intent.getBooleanExtra(AlertUtils.SHOW_EVENT_KEY, false);
        long[] eventIds = intent.getLongArrayExtra(AlertUtils.EVENT_IDS_KEY);
        int notificationId = intent.getIntExtra(AlertUtils.NOTIFICATION_ID_KEY, -1);
        boolean showBirthdayInfo = intent.getBooleanExtra(AlertUtils.SHOW_BIRTHDAY_INFO_KEY, false);

        Uri uri = CalendarAlerts.CONTENT_URI;
        String selection;

        // Dismiss a specific fired alarm if id is present, otherwise, dismiss all alarms
        if (eventId != -1) {
            /// M: update event id to notification id map, which means remove one notification id @{
            AlertService.getEventIdToNotificationIdMap().remove(eventId);
            /// @}
            selection = CalendarAlerts.STATE + "=" + CalendarAlerts.STATE_FIRED + " AND " +
            CalendarAlerts.EVENT_ID + "=" + eventId;
        } else if (eventIds != null && eventIds.length > 0) {
            selection = buildMultipleEventsQuery(eventIds);
            /// M: update event id to notification id map, which means remove one notification id @{
            for (long id : eventIds) {
                AlertService.getEventIdToNotificationIdMap().remove(id);
            }
            /// @}
        } else {
            selection = CalendarAlerts.STATE + "=" + CalendarAlerts.STATE_FIRED;
            /// M: update event id to notification id map, which means remove one notification id @{
            AlertService.getEventIdToNotificationIdMap().clear();
            /// @}
        }

        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(PROJECTION[COLUMN_INDEX_STATE], CalendarAlerts.STATE_DISMISSED);
        /// M: #update message: dismiss# @{
        int updateNum  = resolver.update(uri, values, selection, null);
        LogUtil.d(TAG, "Dissmiss alarm count: " + updateNum);
        ///@}

        /** M: We don't need to cancel the notification here, just refresh it according to database
         * change
        */
//        // Remove from notification bar.
//        if (notificationId != -1) {
//            NotificationManager nm =
//                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            nm.cancel(notificationId);
//        }
        AlertUtils.scheduleNextNotificationRefresh(this, null, System.currentTimeMillis());

        if (showEvent) {
            // Show event on Calendar app by building an intent and task stack to start
            // EventInfoActivity with AllInOneActivity as the parent activity rooted to home.
            Intent i = AlertUtils.buildEventViewIntent(this, eventId, eventStart, eventEnd);

            /*TaskStackBuilder.create(this)
                    .addParentStack(EventInfoActivity.class).addNextIntent(i).startActivities();*/
            startActivity(i);
        }

        if (showBirthdayInfo) {
            Intent intent2 = AlertUtils.buildBirthdayInfoViewIntent(this, eventStart);

            /*TaskStackBuilder.create(this)
                    .addParentStack(AuroraBirthdayInfoActivity.class).addNextIntent(intent2).startActivities();*/
            startActivity(intent2);
        }

        /// M: #unread message# update unread msg.@{
        selection = CalendarAlerts.STATE + "=" + CalendarAlerts.STATE_FIRED;
        Cursor cursor = resolver.query(uri, null, selection, null, null);
        int unReadMsgNumber = 0;
        if (cursor != null) {
            try {
                unReadMsgNumber = cursor.getCount();
            } finally {
                cursor.close();
            }
        }
        LogUtil.d(TAG, "WriteUnreadReminders(unReadMsgNumber).");
        MTKUtils.writeUnreadReminders(getApplicationContext(), unReadMsgNumber);
        ///@}

        // Stop this service
        /** M: we don't need stop the service here, cause: 1, If we have more than one intent arrived
        * to this service, the rest intents would never be processed after the first one had been
        * handled; 2, The handler which process the intents will stop itself when the intent it's
        * responsible for had been processed
        //stopSelf();
        */
    }

    private String buildMultipleEventsQuery(long[] eventIds) {
        StringBuilder selection = new StringBuilder();
        selection.append(CalendarAlerts.STATE);
        selection.append("=");
        selection.append(CalendarAlerts.STATE_FIRED);
        if (eventIds.length > 0) {
            selection.append(" AND (");
            selection.append(CalendarAlerts.EVENT_ID);
            selection.append("=");
            selection.append(eventIds[0]);
            for (int i = 1; i < eventIds.length; i++) {
                selection.append(" OR ");
                selection.append(CalendarAlerts.EVENT_ID);
                selection.append("=");
                selection.append(eventIds[i]);
            }
            selection.append(")");
        }
        return selection.toString();
    }
}
