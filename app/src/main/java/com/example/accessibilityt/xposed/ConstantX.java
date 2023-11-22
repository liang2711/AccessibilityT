package com.example.accessibilityt.xposed;

import android.net.Uri;

public class ConstantX {
    public static final String SERVICER_IP="http://192.168.169.1:8088/dex2java";
    public static final String KEY_PACKAGENAME="packagename";
    public static final String KEY_CLASSNAME="classname";

    private static final String AUTOHORITY="com.example.infoprovider";
    private static final String KEY_NAME="xposed";
    public static final Uri CONTENT_URI=Uri.parse("content://"+AUTOHORITY+"/"+KEY_NAME);
}
