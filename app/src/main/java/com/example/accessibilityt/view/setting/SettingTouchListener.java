package com.example.accessibilityt.view.setting;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SettingTouchListener implements RecyclerView.OnItemTouchListener {
    private float downX;
    private float downY;
    private float tmpX;
    private float orientation = 0;
    private boolean moving = false;
    private View currentChild;
    private SettingAdapter.AppViewHoder currentHolder;
    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        View child = rv.findChildViewUnder(e.getX(), e.getY());
        if(child == null){
            return false;
        }
        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                currentChild = rv.findChildViewUnder(e.getX(), e.getY());
                currentHolder = (SettingAdapter.AppViewHoder) rv.getChildViewHolder(currentChild);
                downX = e.getX();
                downY = e.getY();
                moving = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = e.getX() - downX;
                float moveY = e.getY() - downY;
                if(moveX == 0 || Math.abs(moveX) < Math.abs(moveY)){
                    orientation = 0;
                    break;
                }
                if(Math.abs(e.getX() - tmpX) > 10){
                    orientation = e.getX() - tmpX;
                }
                if(child != currentChild){
                    break;
                }
                if(moveX > 20) {
                    moving = true;
                }
                if(moveX < 0) {
                    if(-moveX <= currentHolder.menuLayout.getWidth() && -currentHolder.contentLayout.getTranslationX() < currentHolder.menuLayout.getWidth()) {
                        currentHolder.contentLayout.setTranslationX(moveX);
                    }
                    else{
                        currentHolder.contentLayout.setTranslationX(-currentHolder.menuLayout.getWidth());
                    }
                }
                else if(moveX > 0 && currentHolder.contentLayout.getTranslationX() < 0){
                    float m = -currentHolder.menuLayout.getWidth() + moveX;
                    if(m > 0){
                        currentHolder.contentLayout.setTranslationX(0);
                    }
                    else{
                        currentHolder.contentLayout.setTranslationX(m);
                    }
                }
                tmpX = e.getX();
                break;
            case MotionEvent.ACTION_UP:
                if(orientation >= 0){
                    currentHolder.contentLayout.setTranslationX(0);
                }
                else{
                    currentHolder.contentLayout.setTranslationX(-currentHolder.menuLayout.getWidth());
                }
                return moving;
            //break;
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
