package com.example.accessibilityt;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.accessibilityt.view.CcodeShowView;

//代码显示
public class CNameDrawerFragment extends Fragment implements View.OnClickListener {

    public static TextView code_Context;
    public static TextView title;

    public static ImageView titleIcon;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.a_code,container,false);
        code_Context=view.findViewById(R.id.a_code_tv);
        title=view.findViewById(R.id.a_c_t_tv);
        titleIcon=view.findViewById(R.id.a_c_t_img);
        viewAction();
        return view;
    }

    private void viewAction(){
        code_Context.setText("");
        title.setText("请点击菜单显示代码");
        titleIcon.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (CcodeShowView.drawerLayout!=null)
            CcodeShowView.drawerLayout.openDrawer(CcodeShowView.listView);
    }
}
