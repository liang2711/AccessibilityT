package com.example.accessibilityt.view;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.accessibilityt.MainActivity;
import com.example.accessibilityt.SideBarArrow;

public class FloadWindowService extends Service {
    public static Context mContext=null;
    private static final String ACTION_HIDE="com.example.accessibilityt.ACTION_HIDE";
    private SideBarHideReceiver mReceiver;
    private SideBarArrow mLeftArrowBar;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createToucher();
        mContext=getApplicationContext();
        VDataTools.daoInit(getApplicationContext());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLeftArrowBar.clearAll();
        unregisterReceiver(mReceiver);
    }

    @SuppressLint({"RtlHardcoded", "InflateParams"})
    private void createToucher() {
        Log.d(MainActivity.TAG,"AccessibilityService onCreate");
        Log.d("InitApp","AccessibilityService_create");
        // get window manager
        WindowManager windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        // left arrow
        mLeftArrowBar = new SideBarArrow();
        LinearLayout mArrowLeft = mLeftArrowBar.getView(this, windowManager, null);

        // register
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_HIDE);
        mReceiver = new SideBarHideReceiver();
        mReceiver.setSideBar(mLeftArrowBar);
        registerReceiver(mReceiver, filter);
    }
}
