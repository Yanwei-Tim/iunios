LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := SogouInput_iuni
LOCAL_SRC_FILES := $(LOCAL_MODULE).apk
LOCAL_MODULE_CLASS := APPS
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)


LOCAL_MULTILIB := 32

LOCAL_PREBUILT_JNI_LIBS := \
    lib/arm/libkpencore_v71.so \
    lib/arm/libNinepatch.so \
    lib/arm/libwebp.so
include $(BUILD_PREBUILT)

