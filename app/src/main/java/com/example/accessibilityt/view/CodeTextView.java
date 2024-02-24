package com.example.accessibilityt.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

@SuppressLint("AppCompatCustomView")
public class CodeTextView extends TextView {

    public CodeTextView(Context context) {
        super(context);
    }

    public CodeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CodeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CodeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        char[] decision_points={';','{','}'};
        String code=text.toString();
        String newString="";
        int nowindex=0;
        for (int i=0;i<code.length();i++){
            for (int j=0;j<decision_points.length;j++){
                if (code.charAt(i)!=decision_points[j])
                    continue;
                try {
                    newString+=code.substring(nowindex,i+1);
                    newString+="\n";
                    nowindex=i+1;
                }catch (Exception e){
                    Log.e("CodeTesxView","string index error!");
                }
            }
        }
        super.setText(newString, BufferType.NORMAL);
    }
}
