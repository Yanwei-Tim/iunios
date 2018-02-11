package com.mediatek.phone.ext;

import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public interface ICallFeaturesSettingExt {

    /**
     * called when init the preference (onCreate) single card
     * plugin can customize the activity, like add/remove preference screen
     * plugin should check the activiyt class name, to distinct the caller, avoid do wrong work.
     * if (TextUtils.equals(getClass().getSimpleName(), "CallFeaturesSetting") {}
     *
     * @param activity the PreferenceActivity instance
     */
    void initOtherCallFeaturesSetting(PreferenceActivity activity);

    /**
     * called when init the preference (onCreate)
     * plugin can customize the fragment, like add/remove preference screen
     * plugin should check the fragment class name, to distinct the caller, avoid do wrong work.
     * if (TextUtils.equals(getClass().getSimpleName(), "CallFeaturesSetting") {}
     *
     * @param fragment the PreferenceFragment instance
     */
    void initOtherCallFeaturesSetting(PreferenceFragment fragment);

    /**
     * called when init the preference Activity(onCreate)
     * plugin can customize the Activity, like add/remove preference screen
     * plugin should check the Activity class name before use it, like:
     *
     * @param activity the PreferenceActivity which call this API
     */
    void initCdmaCallForwardOptionsActivity(PreferenceActivity activity);

    void initCdmaCallForwardOptionsActivity(PreferenceActivity activity, int subId);
}
