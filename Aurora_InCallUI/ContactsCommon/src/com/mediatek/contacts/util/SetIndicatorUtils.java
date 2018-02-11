/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */
package com.mediatek.contacts.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.net.Uri;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.Log;

import com.mediatek.contacts.ExtensionManager;
import com.mediatek.telecom.TelecomManagerEx;
import com.mediatek.widget.CustomAccountRemoteViews.AccountInfo;
import com.mediatek.widget.DefaultAccountSelectionBar;

import java.util.ArrayList;
import java.util.List;

public class SetIndicatorUtils {
    private static final String TAG = "SetIndicatorUtils";

    private static final String PEOPLEACTIVITY = "com.android.contacts.activities.PeopleActivtiy";
    private static final String QUICKCONTACTACTIVITY = "com.android.contacts.quickcontact.QuickContactActivity";
    private static final String INDICATE_TYPE = "CONTACTS";
    private static final String ACTION_OUTGOING_CALL_PHONE_ACCOUNT_CHANGED = "com.android.contacts.ACTION_OUTGOING_CALL_PHONE_ACCOUNT_CHANGED";
    private static final String EXTRA_ACCOUNT = "extra_account";

    private static SetIndicatorUtils sInstance = null;
    private DefaultAccountSelectionBar mDefaultAccountSelectionBar = null;
    private boolean mShowSimIndicator = false;
    private BroadcastReceiver mReceiver = null;

    // In PeopleActivity, if quickContact is show, quickContactIsShow = true,
    // PeopleActivity.onPause cannot hide the Indicator.
    private boolean mQuickContactIsShow = false;
    private Activity mActivity = null;
    private boolean mIsRegister = false;

    public static SetIndicatorUtils getInstance() {
        if (sInstance == null) {
            sInstance = new SetIndicatorUtils();
        }
        return sInstance;
    }

    public void showIndicator(Activity activity, boolean visible) {
        if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
            //None user owner don't show notification bar.
            return;
        }

        LogUtils.i(TAG, "[showIndicator]visible : " + visible);
        mActivity = activity;
        mShowSimIndicator = visible;

        if (mDefaultAccountSelectionBar == null) {
            Context context = mActivity;
            mDefaultAccountSelectionBar = new DefaultAccountSelectionBar(context,
                        context.getPackageName(), null);
        }

        setSimIndicatorVisibility(visible);
    }

    public void registerReceiver(Activity activity) {
        LogUtils.d(TAG, "[register] activity : " + activity + ",register:" + mIsRegister);
        if (!mIsRegister) {
            if (mReceiver == null) {
                mReceiver = new MyBroadcastReceiver();
            }

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_OUTGOING_CALL_PHONE_ACCOUNT_CHANGED);
            intentFilter.addAction(TelecomManagerEx.ACTION_PHONE_ACCOUNT_CHANGED);
            activity.registerReceiver(mReceiver, intentFilter);
            mIsRegister = true;
        }
    }

    public void unregisterReceiver(Activity activity) {
        LogUtils.d(TAG, "[unregister] activity : " + activity + ",unregister:" + mIsRegister);
        if (mIsRegister) {
            activity.unregisterReceiver(mReceiver);
            mIsRegister = false;
        }
    }

    private SetIndicatorUtils() {

    }

    private void setSimIndicatorVisibility(boolean visible) {
        if (visible) {
            List<AccountInfo> accountList = getPhoneAccountInfos(mActivity);
            Log.d(TAG, "[setSimIndicatorVisibility] accountList size " + accountList.size());

            if (accountList.size() > 2) {
                mDefaultAccountSelectionBar.updateData(accountList);
                mDefaultAccountSelectionBar.show();
            } else {
                mDefaultAccountSelectionBar.hide();
                mShowSimIndicator = false;
            }
            registerReceiver(mActivity);

        } else {
            mDefaultAccountSelectionBar.hide();
            mShowSimIndicator = false;
            unregisterReceiver(mActivity);
        }
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_OUTGOING_CALL_PHONE_ACCOUNT_CHANGED.equals(action)) {

                if (mShowSimIndicator) {
                    updateSelectedAccount(intent);
                    setSimIndicatorVisibility(true);
                }
                hideNotification();
            } else if (TelecomManagerEx.ACTION_PHONE_ACCOUNT_CHANGED.equals(action)) {
                setSimIndicatorVisibility(true);
            }
        }
    }

    private void hideNotification() {
        Intent intent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        mActivity.sendBroadcast(intent);
    }

    private List<AccountInfo> getPhoneAccountInfos(Context context) {
        TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        List<PhoneAccountHandle> accountHandles = telecomManager.getCallCapablePhoneAccounts();

        Log.d(TAG, "[getPhoneAccountInfos] accountHandles.size" + accountHandles.size());
        List<AccountInfo> accountInfos = new ArrayList<AccountInfo>();
        PhoneAccountHandle selectedAccountHandle = telecomManager.getUserSelectedOutgoingPhoneAccount();
        // Add the always ask item
        AccountInfo alwaysAskInfo = createAlwaysAskAccountInfo(context, selectedAccountHandle == null);
        accountInfos.add(alwaysAskInfo);

        for (PhoneAccountHandle handle : accountHandles) {
            final PhoneAccount account = telecomManager.getPhoneAccount(handle);
            if (account == null) {
                continue;
            }

            String label = account.getLabel() != null ? account.getLabel().toString() : null;
            Uri sddress = account.getAddress();
            Uri subAddress = account.getSubscriptionAddress();
            String number = null;

            if (subAddress != null) {
                number = subAddress.getSchemeSpecificPart();
            } else if (sddress != null) {
                number = sddress.getSchemeSpecificPart();
            }

            Intent intent = new Intent(ACTION_OUTGOING_CALL_PHONE_ACCOUNT_CHANGED);
            intent.putExtra(EXTRA_ACCOUNT, handle);
            boolean isSelected = false;

            if (handle.equals(selectedAccountHandle)) {
                isSelected = true;
            }

            AccountInfo info = new AccountInfo(account.getIconResId(), drawableToBitmap(account
                    .createIconDrawable(context)), label, number, intent, isSelected);
            Log.d(TAG, "label =" + label + ", number =" + number);
            accountInfos.add(info);
        }
        return accountInfos;
    }

    private AccountInfo createAlwaysAskAccountInfo(Context context, boolean isSelected) {
        Intent intent = new Intent(ACTION_OUTGOING_CALL_PHONE_ACCOUNT_CHANGED);
        String label = context.getString(com.mediatek.R.string.account_always_ask_title);
        int iconResId = ExtensionManager.getInstance().getCtExtension()
                .showAlwaysAskIndicate(com.mediatek.R.drawable.account_always_ask_icon);
        Log.i(TAG, "[createAlwaysAskAccountInfo] iconResId : " + iconResId);
        return new AccountInfo(iconResId, null, label, null, intent, isSelected);
        }

    private void updateSelectedAccount(Intent intent) {
        PhoneAccountHandle handle = (PhoneAccountHandle) intent.getParcelableExtra(EXTRA_ACCOUNT);
        Context context = mActivity;
        TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

        telecomManager.setUserSelectedOutgoingPhoneAccount(handle);
    }

    /**
     * DefaultAccountSelectionBar only accept the bitmap,
     * so if drawable, need covert it to bitmap.
     * @param drawable the original drawable.
     * @return the converted bitmap.
     */
    private Bitmap drawableToBitmap(Drawable drawable) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        return bitmapDrawable.getBitmap();
    }
}
