package com.ue.recommend;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ue.recommend.db.RecommendAppDao;
import com.ue.recommend.db.RecommendDatabase;
import com.ue.recommend.model.RecommendApp;
import com.ue.recommend.model.RecommendAppResult;
import com.ue.recommend.util.BmobUtils;
import com.ue.recommend.util.GsonHolder;
import com.ue.recommend.util.KLog;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by hawk on 2017/11/27.
 */

public class SheetDataPresenter {
    private static final String LAST_PULL_TIME = "lastPullTime";
    private Context mContext;

    public SheetDataPresenter(Context context) {
        this.mContext = context;
    }

    public Observable<List<RecommendApp>> getRecommendApps() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        RecommendAppDao mRecommendAppDao = RecommendDatabase.getInstance(mContext).recommendAppDao();

        return Observable
                .create((ObservableEmitter<List<RecommendApp>> e) -> {
                    //从本地获取数据
                    List<RecommendApp> recommendApps = mRecommendAppDao.getRecommendApps();
                    boolean hasRecommendApps = (recommendApps != null && recommendApps.size() > 0);
                    if (hasRecommendApps) {
                        e.onNext(recommendApps);
                    }
                    //更新数据
                    long cacheTime = sharedPreferences.getLong(LAST_PULL_TIME, 0);
                    if (System.currentTimeMillis() - cacheTime > 86400000) {
                        //24*60*60*1000=86400000,缓存时间大于一天才重新获取数据
                        String bql = String.format("select * from RecommendApp where packageName!='%s'", mContext.getPackageName());
                        String result = BmobUtils.getInstance().findBQL(bql);
                        KLog.e("RecommendSheetView", "getRecommendApps: server data=" + result);

                        if (result.contains("appName")) {
                            RecommendAppResult recommendAppResult = GsonHolder.getGson().fromJson(result, RecommendAppResult.class);
                            mRecommendAppDao.saveRecommendApps(recommendAppResult.results);

                            sharedPreferences.edit()
                                    .putLong(LAST_PULL_TIME, System.currentTimeMillis())
                                    .apply();

                            if (!hasRecommendApps) {
                                hasRecommendApps = true;
                                e.onNext(recommendAppResult.results);
                            }
                        }
                    }
                    if (!hasRecommendApps) {
                        e.onNext(new ArrayList<>());
                    }
                    e.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /*public Observable<List<SearchAppDetail>> searchApps(String keyword) {
        return Observable
                .create((ObservableEmitter<List<SearchAppDetail>> e) -> {
                    String result = BmobUtils.getInstance().search(keyword).trim();
                    KLog.e("RecommendSheetView", "searchApps: result=" + result);

                    boolean hasResults = false;
                    if (result.contains("apps")) {
                        if (result.endsWith(";")) {
                            result = result.substring(0, result.length() - 1);
                        }
                        SearchAppResult searchAppResult = GsonHolder.getGson().fromJson(result, SearchAppResult.class);
                        if (searchAppResult != null && searchAppResult.apps != null) {
                            hasResults = true;
                            e.onNext(searchAppResult.apps);
                        }
                    }
                    if (!hasResults) {
                        e.onNext(new ArrayList<>());
                    }
                    e.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }*/
}
