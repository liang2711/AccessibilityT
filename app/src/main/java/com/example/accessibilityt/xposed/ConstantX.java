package com.example.accessibilityt.xposed;

import android.net.Uri;

public class ConstantX {
    public static final String APPLICATION_ICON_DEFAULT="/data/user/0/com.example.accessibilityt/cache/youki.png";
    public static final String APPLICATION_JSON="/data/user/0/com.example.accessibilityt/cache/application.json";
    public static final String SERVICER_IP="http://10.43.105.137:8088/dex2java";
    public static final String MODULE_JSON="/data/user/0/com.example.accessibilityt/cache/module.json";
    public static final String KEY_PACKAGENAME="packagename";
    public static final String KEY_CLASSNAME="classname";
    public static final String KEY_ICON="icon";
    public static final String KEY_RUN="isrun";
    public static final String KET_APPNAME="appname";
    public static final String KET_MODULE="module";
    public static final String KET_URI="uri";
    private static final String AUTOHORITY="com.example.infoprovider";
    private static final String KEY_NAME="xposed";
    public static final Uri CONTENT_URI=Uri.parse("content://"+AUTOHORITY+"/"+KEY_NAME);
}
