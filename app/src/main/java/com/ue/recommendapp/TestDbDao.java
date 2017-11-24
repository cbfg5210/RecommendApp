package com.ue.recommendapp;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;

/**
 * Created by hawk on 2017/11/24.
 */
@Dao
public interface TestDbDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long saveTestDb(TestDb testDb);
}
