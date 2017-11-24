package com.ue.recommend;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.ue.recommend.model.RecommendApp;

import java.util.List;

/**
 * Created by hawk on 2017/11/15.
 */
@Dao
public interface RecommendAppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveRecommendApps(List<RecommendApp> recommendApps);

    @Query("select * from RecommendApp")
    List<RecommendApp> getRecommendApps();

    @Query("select * from RecommendApp where packageName=:packageName limit 1")
    RecommendApp getRecommendApp(String packageName);
}
