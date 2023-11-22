package com.example.accessibilityt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.accessibilityt.view.PermissionUtil;


public class SideBarContent implements View.OnClickListener {
    private Context mContext;
    private LinearLayout mContentView;
    private WindowManager mWindowManager;
    private LinearLayout mArrowView;
    private HongBaoService mSideBarService;
    private ControlBar mControlBar;
    private LinearLayout mSeekBarView;
    private int mTagTemp = -1;

    private static final int COUNT_DOWN_TAG = 1;
    private static final int COUNT_DWON_TIME = 5000;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case COUNT_DOWN_TAG:
                    goNormal();
                    break;
            }
        }
    };

    LinearLayout getView(Context context,
                         WindowManager windowManager,
                         WindowManager.LayoutParams params,
                         LinearLayout arrowView,
                         HongBaoService sideBarService) {
        mContext = context;
        mWindowManager = windowManager;
        mArrowView = arrowView;
        mSideBarService = sideBarService;
        // get layout
        LayoutInflater inflater = LayoutInflater.from(context);
        mContentView = (LinearLayout) inflater.inflate(R.layout.layout_content, null);
        // init click
        mContentView.findViewById(R.id.tv_brightness).setOnClickListener(this);
        mContentView.findViewById(R.id.tv_back).setOnClickListener(this);
        mContentView.findViewById(R.id.tv_home).setOnClickListener(this);
        mContentView.findViewById(R.id.tv_annotation).setOnClickListener(this);
        mContentView.findViewById(R.id.tv_volume).setOnClickListener(this);
        mContentView.findViewById(R.id.tv_backstage).setOnClickListener(this);
        LinearLayout root = mContentView.findViewById(R.id.root);
        root.setPadding(15,0,0,0);
        mWindowManager.addView(mContentView,params);
        return mContentView;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int state=v.getId();
        if (state==R.id.tv_brightness){
            removeOrSendMsg(true,true);
            brightnessPermissionCheck();
        }else if (state==R.id.tv_back){
            removeOrSendMsg(true,true);
            clearSeekBar();
            //mSideBarService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
        }else if (state==R.id.tv_home){
            removeOrSendMsg(true,false);
            goNormal();
           // mSideBarService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
        }else if (state==R.id.tv_annotation){
            removeOrSendMsg(true,false);
            goNormal();
            annotationGo();
        }else if (state==R.id.tv_volume){
            removeOrSendMsg(true,true);
            brightnessOrVolume(1);
        }else if (state==R.id.tv_backstage){
            removeOrSendMsg(true,false);
            goNormal();
            //mSideBarService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
        }
    }

    //设置音量和亮度
    private void brightnessOrVolume(int tag) {
        //判断点击两次同一个按钮会被取消view
        if(mTagTemp == tag) {
            if(null != mSeekBarView) {
                removeSeekBarView();
            }else {
                addSeekBarView(tag);
            }
            return;
        }
        mTagTemp = tag;
        //第一次点击的时候
        if(null == mControlBar) {
            mControlBar = new ControlBar();
        }

        if(null == mSeekBarView) {
            addSeekBarView(tag);
        }else {
            //点击不是同一个按钮取消当前的view调用另一个
            removeSeekBarView();
            addSeekBarView(tag);
        }
    }

    private void addSeekBarView(int tag) {
        mSeekBarView = mControlBar.getView(mContext,tag,this);
        mWindowManager.addView(mSeekBarView, mControlBar.mParams);
    }

    private void removeSeekBarView() {
        if(null != mSeekBarView) {
            mWindowManager.removeView(mSeekBarView);
            mSeekBarView = null;
        }
    }

    private void arrowsShow() {
        mContentView.setVisibility(View.GONE);
        mArrowView.setVisibility(View.VISIBLE);
    }

    void clearSeekBar() {
        if(null != mSeekBarView) {
            mWindowManager.removeView(mSeekBarView);
            mSeekBarView = null;
        }
    }
    //退出列表和显示箭头
    private void goNormal() {
        arrowsShow();
        clearSeekBar();
    }

    //我要设置的功能
    private void annotationGo() {
        Toast.makeText(mContext,"waawdawdawd",Toast.LENGTH_SHORT).show();
    }

    void removeOrSendMsg(boolean remove, boolean send) {
        if(remove) {
            //当在规定时间内接触到不会退出列表
            mHandler.removeMessages(COUNT_DOWN_TAG);
        }
        if(send) {
            //延迟发送通知只发送一次 应该是对于不同列表退回
            mHandler.sendEmptyMessageDelayed(COUNT_DOWN_TAG,COUNT_DWON_TIME);
        }
    }

    /**
     * when AccessibilityService is forced closed 清空消息队列
     */
    void clearCallbacks() {
        if(null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    //调节亮度
    private void brightnessPermissionCheck() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionUtil.isSettingsCanWrite(mContext)) {
                goNormal();
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + mContext.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                Toast.makeText(mContext,"权限调用成功",Toast.LENGTH_LONG).show();
            }else {
                brightnessOrVolume(0);
            }
        }else {
            brightnessOrVolume(0);
        }
    }
}
