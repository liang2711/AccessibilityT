package com.example.accessibilityt;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;

import com.example.accessibilityt.HongBaoService;
import com.example.accessibilityt.SideBarContent;

public class SideBarArrow implements View.OnClickListener {
    private WindowManager.LayoutParams mParams;
    private LinearLayout mArrowView;
    private Context mContext;
    private WindowManager mWindowManager;
    private SideBarContent mContentBar;
    private LinearLayout mContentBarView;

    //自己画view
    public LinearLayout getView(Context context,WindowManager windowManager) {
        mContext = context;
        mWindowManager = windowManager;
        mParams = new WindowManager.LayoutParams();
        // compatible 让窗口在第一层
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        // set bg transparent 设置透明度
        mParams.format = PixelFormat.RGBA_8888;
        // can not focusable 关闭焦点 这样鼠标键盘不会聚焦
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mParams.x = 0;
        mParams.y = 0;
        // window size
        mParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        mParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        // get layout
        LayoutInflater inflater = LayoutInflater.from(context);
        mArrowView = (LinearLayout) inflater.inflate(R.layout.layout_arrow, null);
        AppCompatImageView arrow = mArrowView.findViewById(R.id.arrow);
        arrow.setOnClickListener(this);
        //旋转
        arrow.setRotation(180);
        mParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        //设置动漫退出效果
        mParams.windowAnimations = R.style.LeftSeekBarAnim;
        mWindowManager.addView(mArrowView,mParams);
        return mArrowView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.arrow){
            mArrowView.setVisibility(View.GONE);
            if(null == mContentBar || null == mContentBarView) {
                mContentBar = new SideBarContent();
                mContentBarView = mContentBar.getView(mContext,mWindowManager,mParams,mArrowView);
            }else {
                mContentBarView.setVisibility(View.VISIBLE);
            }
            mContentBar.removeOrSendMsg(false,true);
        }
    }


    public void launcherInvisibleSideBar() {
        mArrowView.setVisibility(View.VISIBLE);
        if(null != mContentBar || null != mContentBarView) {
            mContentBarView.setVisibility(View.GONE);
            mContentBar.removeOrSendMsg(true,false);
            mContentBar.clearSeekBar();
        }
    }

    /**
     * when AccessibilityService is forced closed
     */
    public void clearAll() {
        mWindowManager.removeView(mArrowView);
        if(null != mContentBar || null != mContentBarView) {
            mWindowManager.removeView(mContentBarView);
            mContentBar.clearSeekBar();
            mContentBar.clearCallbacks();
        }
    }
}
