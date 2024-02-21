package com.example.accessibilityt.dao;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {AppCodeInfo.class},version = 1)
public abstract class RDatabase extends RoomDatabase {
    public abstract AppCodeInfoDao getDao();

    private static RDatabase mInstance;

    private static final Object slock=new Object();

    public static RDatabase getInstance(Context context){
        synchronized (slock){
            if (mInstance==null){
                mInstance= Room.databaseBuilder
                        (context.getApplicationContext(),RDatabase.class,
                                "a_code.db").build();
            }
            return mInstance;
        }
    }
}
