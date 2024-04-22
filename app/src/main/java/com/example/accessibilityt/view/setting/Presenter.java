package com.example.accessibilityt.view.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.accessibilityt.MainActivity;
import com.example.accessibilityt.view.VDataTools;
import com.example.accessibilityt.view.setting.model.ICallback;
import com.example.accessibilityt.view.setting.model.IModel;
import com.example.accessibilityt.view.setting.model.Model;

import java.util.ArrayList;
import java.util.List;

public class Presenter {
    Context context;
    List<String> listTemporary;
    private IModel iModel;
    private IView iView;
    private Handler handler=new Handler();

    public Presenter(IView iView,Context context){
        this.iView=iView;
        this.context=context;
        iModel=new Model();
    }

    public void refreshE(){
        iModel.getE(new ICallback() {
            @Override
            public void s(String str, List<String> list) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        iView.rE(str);
                    }
                });
            }
            @Override
            public void d() {
                Log.e(MainActivity.TAG,"Setting uri error! refreshE");
            }
        });
    }
    public void putUrl(String strTemporary){
        iModel.putE(strTemporary, new ICallback() {
            @Override
            public void s(String str, List<String> list) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        iView.rE(strTemporary);
                        Toast.makeText(context,"修改成功",Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void d() {
                Log.e(MainActivity.TAG,"Setting uri error! getE");
            }
        });
    }

    public void refreshR(){
        iModel.getR(new ICallback() {
            @Override
            public void s(String str, List<String> list) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listTemporary=list;
                        iView.rR(list);
                    }
                });
            }
            @Override
            public void d() {
                Log.e(MainActivity.TAG,"Setting packageName error! refreshR");
            }
        });
    }
    public void putPK(String str){
        if (listTemporary==null)
            listTemporary=new ArrayList<>();
        if(listTemporary.contains(str)){
            Toast.makeText(context,"这个包名已经存在",Toast.LENGTH_SHORT).show();
            return;
        }
        listTemporary.add(str);
        iModel.putR(listTemporary, new ICallback() {
            @Override
            public void s(String str, List<String> list) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        iView.rR(listTemporary);
                    }
                });
            }
            @Override
            public void d() {
                Log.e(MainActivity.TAG,"Setting packageName error! putPK ");
            }
        });
    }
    public void showDialog() {
       iView.showDialog();
    }

}
