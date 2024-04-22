package com.example.accessibilityt.view.setting.model;

import com.example.accessibilityt.view.VDataTools;

import java.util.List;

public class Model implements IModel{

    @Override
    public void putE(String str,final ICallback callback) {
       boolean state=VDataTools.putURI(str);
       if (state)
           callback.s(null,null);
       else callback.d();
    }

    @Override
    public void getE(final ICallback callback) {
        String str=VDataTools.getURI();
        if (str!=null)
            callback.s(str,null);
        else callback.d();
    }

    @Override
    public void putR(List<String> list,final ICallback callback) {
        boolean state=VDataTools.putPK(list);
        if (state)
            callback.s(null,null);
        else callback.d();
    }

    @Override
    public void getR(final ICallback callback) {
        List<String> list=VDataTools.getPK();
        if (list!=null)
            callback.s(null,list);
        else callback.d();
    }
}
