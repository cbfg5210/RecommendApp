package com.ue.recommend;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.ue.recommend.model.RecommendApp;
import com.ue.recommend.model.RecommendAppResult;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by hawk on 2017/11/24.
 */

public class RecommendAppProxy {
    private static final String LAST_PULL_TIME = "lastPullTime";

    private SharedPreferences sharedPreferences;
    private RecommendAppDao mRecommendAppDao;
    private boolean hasRecommendApps;

    public RecommendAppProxy(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mRecommendAppDao = AppDatabase.getInstance(context).recommendAppDao();
    }

    public Observable<List<RecommendApp>> getLocalRecommendApps() {
        return Observable
                .create((ObservableEmitter<List<RecommendApp>> e) -> {
                    List<RecommendApp> recommendApps = mRecommendAppDao.getRecommendApps();
                    if (recommendApps != null && recommendApps.size() > 0) {
                        hasRecommendApps = true;
                        e.onNext(recommendApps);
                    }
                    e.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<RecommendApp>> pullRecommendApps() {
        long cacheTime = sharedPreferences.getLong(LAST_PULL_TIME, 0);
        if (System.currentTimeMillis() - cacheTime < 86400000) {
            //24*60*60*1000=86400000,缓存时间小于一天不重新获取数据
            return null;
        }
        Log.e("MainFragment", "pullRecommendApps: **********");
        //刷新数据
        return Observable
                .create((@NonNull ObservableEmitter<List<RecommendApp>> e) -> {
                    String result = BmobUtils.findBQL("select * from RecommendApp");
                    Log.e("MainActivity", "---pullRecommendApps---: result=" + result);

                    if (result.contains("appName")) {
                        RecommendAppResult recommendAppResult = new Gson().fromJson(result, RecommendAppResult.class);
                        mRecommendAppDao.saveRecommendApps(recommendAppResult.results);

                        sharedPreferences.edit()
                                .putLong(LAST_PULL_TIME, System.currentTimeMillis())
                                .apply();

                        if (!hasRecommendApps) {
                            hasRecommendApps = true;
                            e.onNext(recommendAppResult.results);
                        }
                    }
                    e.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
