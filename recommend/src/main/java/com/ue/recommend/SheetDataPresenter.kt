package com.ue.recommend

import android.content.Context
import android.preference.PreferenceManager
import com.ue.recommend.db.RecommendDatabase
import com.ue.recommend.model.RecommendApp
import com.ue.recommend.model.RecommendAppResult
import com.ue.recommend.util.BmobUtils
import com.ue.recommend.util.GsonHolder
import com.ue.recommend.util.KLog
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by hawk on 2017/11/27.
 */

class SheetDataPresenter(private val mContext: Context) {

    //从本地获取数据
    //更新数据
    //24*60*60*1000=86400000,缓存时间大于一天才重新获取数据
    val recommendApps: Observable<List<RecommendApp>>
        get() {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
            val mRecommendAppDao = RecommendDatabase.getInstance(mContext).recommendAppDao()

            return Observable
                    .create { e: ObservableEmitter<List<RecommendApp>> ->
                        val recommendApps = mRecommendAppDao.recommendApps
                        var hasRecommendApps = recommendApps != null && recommendApps.size > 0
                        if (hasRecommendApps) {
                            e.onNext(recommendApps)
                        }
                        val cacheTime = sharedPreferences.getLong(LAST_PULL_TIME, 0)
                        if (System.currentTimeMillis() - cacheTime > 86400000) {
                            val bql = String.format("select * from RecommendApp where packageName!='%s'", mContext.packageName)
                            val result = BmobUtils.getInstance().findBQL(bql)
                            KLog.e("RecommendSheetView", "getRecommendApps: server data=" + result)

                            if (result.contains("appName")) {
                                val recommendAppResult = GsonHolder.gson.fromJson(result, RecommendAppResult::class.java)
                                mRecommendAppDao.saveRecommendApps(recommendAppResult.results)

                                sharedPreferences.edit()
                                        .putLong(LAST_PULL_TIME, System.currentTimeMillis())
                                        .apply()

                                if (!hasRecommendApps) {
                                    hasRecommendApps = true
                                    e.onNext(recommendAppResult.results)
                                }
                            }
                        }
                        if (!hasRecommendApps) {
                            e.onNext(ArrayList())
                        }
                        e.onComplete()
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }

    companion object {
        private val LAST_PULL_TIME = "lastPullTime"
    }
}
