package com.example.accessibilityt.view.setting;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accessibilityt.R;

import java.util.ArrayList;
import java.util.List;

public class SettingView extends AppCompatActivity implements IView{
    EditText mEditText;
    RecyclerView mRecyclerView;
    Presenter presenter;
    SettingAdapter settingAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        init();
        presenter.refreshE();
        presenter.refreshR();
    }
    private void init(){
        presenter=new Presenter(this,this);
        mRecyclerView=findViewById(R.id.st_r);
        mEditText=findViewById(R.id.st_e);
        settingAdapter=new SettingAdapter(this,new ArrayList<String>());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(settingAdapter);
        mRecyclerView.addOnItemTouchListener(new SettingTouchListener());
    }
    public void determine(View view){
        presenter.showDialog();
    }
    public void modify(View view){
        presenter.putUrl(String.valueOf(mEditText.getText()));
    }

    @Override
    public void rE(String str) {
        mEditText.setText(str);
    }

    @Override
    public void rR(List<String> list) {
        settingAdapter.setList(list);
        settingAdapter.notifyDataSetChanged();
    }

    @Override
    public void showDialog() {
        final EditText editText=new EditText(this);
        AlertDialog.Builder builder=
                new AlertDialog.Builder(this);
        builder.setTitle("请输入要hook的包名").setView(editText);
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        presenter.putPK(String.valueOf(editText.getText()));
                    }
                });
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
}
