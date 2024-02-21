package com.example.accessibilityt.dao;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "appcodeinfo")
public class AppCodeInfo {
    @PrimaryKey(autoGenerate = true)
    public int _id;

    @ColumnInfo(name = "app_name")
    public String appName;

    @ColumnInfo(name = "class_name")
    public String className;

    @ColumnInfo(name = "code")
    public String code;

    @ColumnInfo(name = "res_1")
    public String resource1;

    @ColumnInfo(name = "res_2")
    public String resource2;
}
