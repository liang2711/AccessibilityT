package com.example.accessibilityt.view;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.accessibilityt.R;

public class DisplayTextView implements View.OnClickListener{
    private ConstraintLayout layout;

    public View getView(Context context, WindowManager windowManager, WindowManager.LayoutParams params){
        TextView textView=null;
        LayoutInflater inflater = LayoutInflater.from(context);
        layout = (ConstraintLayout) inflater.inflate(R.layout.show_text, null);
        layout.findViewById(R.id.show_text_back).setOnClickListener(this);
        textView=layout.findViewById(R.id.show_text);
        return layout;
    }

    @Override
    public void onClick(View v) {

    }
}
