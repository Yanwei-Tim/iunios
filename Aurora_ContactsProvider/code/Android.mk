#Gionee:wangth 20120722 modify begin
#ifeq ($(strip $(GN_QC_MTK_APP_CONTACTS_SUPPORT)), yes)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

# Only compile source java files in this apk.
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += \
        src/com/android/providers/contacts/EventLogTags.logtags

LOCAL_STATIC_JAVA_LIBRARIES := \
        yulore_helper \

LOCAL_JAVA_LIBRARIES := ext

LOCAL_STATIC_JAVA_LIBRARIES += android-common com.android.vcard guava
LOCAL_JAVA_LIBRARIES += gnframework

# The Emma tool analyzes code coverage when running unit tests on the
# application. This configuration line selects which packages will be analyzed,
# leaving out code which is tested by other means (e.g. static libraries) that
# would dilute the coverage results. These options do not affect regular
# production builds.
LOCAL_EMMA_COVERAGE_FILTER := +com.android.providers.contacts.*

# The Emma tool analyzes code coverage when running unit tests on the
# application. This configuration line selects which packages will be analyzed,
# leaving out code which is tested by other means (e.g. static libraries) that
# would dilute the coverage results. These options do not affect regular
# production builds.
LOCAL_EMMA_COVERAGE_FILTER := +com.android.providers.contacts.*

LOCAL_PACKAGE_NAME := ContactsProvider
LOCAL_CERTIFICATE := shared

LOCAL_PROGUARD_FLAG_FILES := proguard.flags


include $(BUILD_PACKAGE)

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
#endif
#Gionee:wangth 20120722 modify end
