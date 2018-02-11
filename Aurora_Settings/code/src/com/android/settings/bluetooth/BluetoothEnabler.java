/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.widget.CompoundButton;
import aurora.widget.AuroraSwitch;
import android.widget.Toast;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.WirelessSettings;
 // Aurora liugj 2013-10-22 added for aurora's new feature start
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraSwitchPreference;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
 // Aurora liugj 2013-10-22 addied for aurora's new feature end

/**
 * BluetoothEnabler is a helper to manage the Bluetooth on/off checkbox
 * preference. It turns on/off Bluetooth and ensures the summary of the
 * preference reflects the current state.
 */
// Aurora liugj 2013-10-22 modified for aurora's new feature
public final class BluetoothEnabler /*implements CompoundButton.OnCheckedChangeListener*/implements AuroraPreference.OnPreferenceChangeListener {
	
	private static final String BLUETOOTH_SWITCH_PREFERENCE_KEY = "bluetooth_switch";
    private final Context mContext;
    private AuroraSwitchPreference mSwitch;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final IntentFilter mIntentFilter;
    ///M: indicate whether need to enable/disable BT or just update the preference
    private boolean mUpdateStatusOnly = false;
    
    private static final String TAG = "BluetoothEnabler";
    
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ///M: get the state from BT framework, because the broadcast may have some delay.
            //int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            int state = mLocalAdapter.getBluetoothState();
            Log.d(TAG, "BluetoothAdapter state changed to" + state);
            handleStateChanged(state);
        }
    };

    public BluetoothEnabler(Context context, AuroraSwitchPreference switch_) {
        mContext = context;
        mSwitch = switch_;

        LocalBluetoothManager manager = LocalBluetoothManager.getInstance(context);
        if (manager == null) {
            // Bluetooth is not supported
            mLocalAdapter = null;
            mSwitch.setEnabled(false);
        } else {
            mLocalAdapter = manager.getBluetoothAdapter();
        }
        mIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    public void resume() {
        if (mLocalAdapter == null) {
            mSwitch.setEnabled(false);
            return;
        }
        // qy test
        if(mSwitch != null)
        mSwitch.setOnPreferenceChangeListener(this);
        // Bluetooth state is not sticky, so set it manually
        handleStateChanged(mLocalAdapter.getBluetoothState());

        mContext.registerReceiver(mReceiver, mIntentFilter);
    }

    public void pause() {
        if (mLocalAdapter == null) {
            return;
        }

        mContext.unregisterReceiver(mReceiver);
        mSwitch.setOnPreferenceChangeListener(null);
    }

    public void setSwitch(AuroraSwitchPreference switch_) {
        if (mSwitch == switch_) return;
        mSwitch.setOnPreferenceChangeListener(null);
        mSwitch = switch_;
        mSwitch.setOnPreferenceChangeListener(this);

        int bluetoothState = BluetoothAdapter.STATE_OFF;
        if (mLocalAdapter != null) bluetoothState = mLocalAdapter.getBluetoothState();
        boolean isOn = bluetoothState == BluetoothAdapter.STATE_ON;
        boolean isOff = bluetoothState == BluetoothAdapter.STATE_OFF;
        mSwitch.setChecked(isOn);
        mSwitch.setEnabled(isOn || isOff);
    }

    /*public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Show toast message if Bluetooth is not allowed in airplane mode
        Log.d(TAG, "onCheckChanged "+isChecked);  
        if (isChecked &&
                !WirelessSettings.isRadioAllowed(mContext, Settings.Global.RADIO_BLUETOOTH)) {
            Toast.makeText(mContext, R.string.wifi_in_airplane_mode, Toast.LENGTH_SHORT).show();
            // Reset switch to off
            buttonView.setChecked(false);
            Log.e(TAG, "setChecked---3");  
        }

          Log.d(TAG, "mUpdateStatusOnly is" + mUpdateStatusOnly);
        /// M: if receive bt status changed broadcast, do not need enable/disable bt.
        if (mLocalAdapter != null && !mUpdateStatusOnly) {
            mLocalAdapter.setBluetoothEnabled(isChecked);
        }
        mSwitch.setEnabled(false);
    }*/

    void handleStateChanged(int state) {
        switch (state) {
            case BluetoothAdapter.STATE_TURNING_ON:
                mSwitch.setEnabled(false);
                break;
            case BluetoothAdapter.STATE_ON:
                  /// M: receive bt status changed broadcast, set mUpdateStatusOnly true @{
                  mUpdateStatusOnly = true;
                  Log.d(TAG, "Begin update status ON: set mUpdateStatusOnly to true");
                  /// @}
                  setSwitchChecked(true);
                mSwitch.setEnabled(true); 
                /// M: after set the switch checked status, set mUpdateStatusOnly false @{
                mUpdateStatusOnly = false;
                Log.d(TAG, "End update status ON: set mUpdateStatusOnly to false");
                /// @}
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                mSwitch.setEnabled(false);
                break;
            case BluetoothAdapter.STATE_OFF:
                  /// M: receive bt status changed broadcast, set mUpdateStatusOnly true @{
                  mUpdateStatusOnly = true;
                  Log.d(TAG, "Begin update status OFF: set mUpdateStatusOnly to true");
                  /// @}
                  setSwitchChecked(false);
                mSwitch.setEnabled(true); 
                /// M: after set the switch checked status, set mUpdateStatusOnly false @{
                mUpdateStatusOnly = false;
                Log.d(TAG, "End update status OFF: set mUpdateStatusOnly to false");
                /// @}
                break;
            default:
            	setSwitchChecked(false);
                mSwitch.setEnabled(true);
        }
    }

    private void setSwitchChecked(boolean isChecked) {
        if (isChecked != mSwitch.isChecked()) {
            // set listener to null, so onCheckedChanged won't be called
            // if the checked status on AuroraSwitch isn't changed by user click
            /*if (mValidListener) {
                mSwitch.setOnCheckedChangeListener(null);
            }*/
            mSwitch.setChecked(isChecked); 
            /*if (mValidListener) {
                mSwitch.setOnCheckedChangeListener(this);
            }*/
        }
    }
    
    @Override
    public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {  
    	boolean isChecked = (Boolean) newValue;
    	if (mSwitch.isChecked() == isChecked) {
			return true;
		}
        if (isChecked &&
                !WirelessSettings.isRadioAllowed(mContext, Settings.Global.RADIO_BLUETOOTH)) {
            Toast.makeText(mContext, R.string.wifi_in_airplane_mode, Toast.LENGTH_SHORT).show();
            // Reset switch to off
            ((AuroraSwitchPreference)preference).setChecked(false);            
        }

          Log.d(TAG, "mUpdateStatusOnly is " + mUpdateStatusOnly);
        /// M: if receive bt status changed broadcast, do not need enable/disable bt.
        if (mLocalAdapter != null && !mUpdateStatusOnly) {
            mLocalAdapter.setBluetoothEnabled(isChecked);
        }
        mSwitch.setEnabled(false);
    	return true;
    }
}