package com.example.accessibilityt.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.accessibilityt.MainActivity;
import com.example.accessibilityt.SideBarArrow;

public class SideBarHideReceiver extends BroadcastReceiver {
    private SideBarArrow mLeft = null;

    private static final String ACTION_HIDE = "com.example.accessibilityt.ACTION_HIDE";
    //刷新view 每调一次应用
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_HIDE)) {
            Log.d(MainActivity.TAG,"SideBarHideReceiver ----------------------------------------");
            if (null != mLeft ) {
                mLeft.launcherInvisibleSideBar();
            }
        }
        if (intent.getAction().equals(ACTION_HIDE+"ApkFileXposed")){
            Log.d("ApkFindXpose","SideBarHideReceiver ----------------------------------------");
        }
    }

    public void setSideBar(SideBarArrow left) {
        this.mLeft = left;
    }
}
