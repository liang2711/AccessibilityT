package com.example.accessibilityt.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AppCodeInfoDao {
    @Query("select * from appcodeinfo where app_name like :appName")
    List<AppCodeInfo> getAppNameAll(String appName);

    @Query("select * from appcodeinfo where app_name = :appName")
    List<AppCodeInfo> getAppNameAllTest(String appName);

    @Insert
    void insertAll(AppCodeInfo... infos);

    @Insert
    void insert(AppCodeInfo info);

    @Delete
    void delete(AppCodeInfo info);

    @Query("delete from appcodeinfo where app_name like :appName")
    void delete(String appName);

    @Query("delete from appcodeinfo where app_name = :appName")
    void deleteTest(String appName);
}
