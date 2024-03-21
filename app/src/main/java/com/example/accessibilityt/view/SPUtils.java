package com.example.accessibilityt.view;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtils {
    private static SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private SPUtils(Context context){
        sharedPreferences=context.getSharedPreferences("module",Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
    }

    private static SPUtils instance;
    public static SPUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (SPUtils.class) {
                if (instance == null) {
                    instance = new SPUtils(context);
                }
            }
        }
        return instance;
    }

    public void putInt(String spName, int value) {
        editor.putInt(spName, value);
        editor.commit();
    }

    public int getInt(String spName, int defaultvalue) {
        return sharedPreferences.getInt(spName, defaultvalue);
    }

    /**
     * ------- String ---------
     */
    public void putString(String spName, String value) {
        editor.putString(spName, value);
        editor.commit();
    }

    public String getString(String spName, String defaultvalue) {
        return sharedPreferences.getString(spName, defaultvalue);
    }

    public String getString(String spName) {
        return sharedPreferences.getString(spName, "");
    }


    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    public void putFloat(String key, float value) {
        editor.putFloat(key, value);
        editor.commit();
    }

    public float getFloat(String key, float defValue) {
        return sharedPreferences.getFloat(key, defValue);
    }

    public void putLong(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public long getLong(String key, long defValue) {
        return sharedPreferences.getLong(key, defValue);
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }

    public void remove(String key) {
        editor.remove(key);
        editor.commit();
    }

    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }
}
