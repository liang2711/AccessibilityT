package com.example.accessibilityt.dao;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "appcodeinfo")
public class AppCodeInfo implements Parcelable {
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
    @Ignore
    protected AppCodeInfo(Parcel in) {
        _id = in.readInt();
        appName = in.readString();
        className = in.readString();
        code = in.readString();
        resource1 = in.readString();
        resource2 = in.readString();
    }
    public AppCodeInfo(){
        super();
    }
    @Ignore
    public static final Creator<AppCodeInfo> CREATOR = new Creator<AppCodeInfo>() {
        @Override
        public AppCodeInfo createFromParcel(Parcel in) {
            return new AppCodeInfo(in);
        }

        @Override
        public AppCodeInfo[] newArray(int size) {
            return new AppCodeInfo[size];
        }
    };

    @Ignore
    @Override
    public int describeContents() {
        return 0;
    }

    @Ignore
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeString(appName);
        dest.writeString(className);
        dest.writeString(code);
        dest.writeString(resource1);
        dest.writeString(resource2);
    }
}
