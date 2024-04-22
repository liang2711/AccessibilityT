package com.example.accessibilityt.view.setting.model;

import java.util.List;

public interface IModel {
    public void putE(String str,final ICallback callback);
    public void getE(final ICallback callback);
    public void putR(List<String> list,final ICallback callback);
    public void getR(final ICallback callback);
}
