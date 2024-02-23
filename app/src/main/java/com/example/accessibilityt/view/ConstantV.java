package com.example.accessibilityt.view;

import android.net.Uri;

public class ConstantV {
    public static final String APPLICATION_JSON="/data/user/0/com.example.accessibilityt/cache/application.json";
    public static final String SERVICER_IP="http://192.168.1.40:8088/dex2java";
    public static final String KEY_NAME="xposed";

    public static final String KET_APPNAME="appname";

    public static final int ITEM=1;

    public static final int ITEM_DEFAULT=2;

    public static final String KEY_CONTEXT="javacode";

    public static final String AUTOHORITY="com.example.infoprovider";

    public static final String KEY_PACKAGENAME="packagename";
    public static final String KEY_CLASSNAME="classname";

    public static final Uri CONTENT_URI=Uri.parse("content://"+AUTOHORITY+"/"+KEY_NAME);
}
