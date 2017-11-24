package com.ue.recommend;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.ue.recommend.model.RecommendApp;
import com.ue.recommend.model.RecommendAppResult;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;

public class MainBottomSheetView extends CoordinatorLayout implements View.OnClickListener {
    private static final String LAST_PULL_TIME = "lastPullTime";

    private ViewGroup vgMainBottomSheet;
    private RecyclerView rvRecommendApps;
    private ViewGroup vgSheetContentPanel;
    private View vgListHeader;

    private BottomSheetBehavior bottomSheetBehavior;
    private RecommendAppAdapter adapter;
    private Context mContext;

    private SharedPreferences sharedPreferences;
    private RecommendAppDao mRecommendAppDao;
    private boolean hasRecommendApps;

    private Disposable showDisposable;
    private Disposable pullDisposable;

    public MainBottomSheetView(Context context) {
        this(context, null, 0);
    }

    public MainBottomSheetView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainBottomSheetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        View.inflate(context, R.layout.layout_main_bottom_sheet, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        vgMainBottomSheet = findViewById(R.id.vgMainBottomSheet);
        rvRecommendApps = findViewById(R.id.rvRecommendApps);
        vgSheetContentPanel = findViewById(R.id.vgSheetContentPanel);
        vgListHeader = findViewById(R.id.vgListHeader);
        vgListHeader.setOnClickListener(this);

        bottomSheetBehavior = BottomSheetBehavior.from(vgMainBottomSheet);

        adapter = new RecommendAppAdapter((Activity) mContext, null);
        rvRecommendApps.setAdapter(adapter);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mRecommendAppDao = AppDatabase.getInstance(getContext()).recommendAppDao();

        showRecommendApps();
        pullRecommendApps();
    }

    public void addBannerAd(View bannerView) {
        bannerView.setBackgroundColor(Color.WHITE);
        vgSheetContentPanel.addView(bannerView);
    }

    public int getState() {
        return bottomSheetBehavior.getState();
    }

    public void hideBottomSheet() {
        bottomSheetBehavior.setState(STATE_COLLAPSED);
    }

    private void showRecommendApps() {
        showDisposable = Observable
                .create((ObservableEmitter<List<RecommendApp>> e) -> {
                    List<RecommendApp> recommendApps = mRecommendAppDao.getRecommendApps();
                    if (recommendApps != null && recommendApps.size() > 0) {
                        hasRecommendApps = true;
                        e.onNext(recommendApps);
                    }
                    e.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(recommendApps -> {
                    if (getContext() != null) {
                        adapter.setItems(new ArrayList<>(recommendApps));
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void pullRecommendApps() {
        long cacheTime = sharedPreferences.getLong(LAST_PULL_TIME, 0);
        if (System.currentTimeMillis() - cacheTime < 86400000) {
            //24*60*60*1000=86400000,缓存时间小于一天不重新获取数据
            return;
        }
        Log.e("MainFragment", "pullRecommendApps: **********");
        //刷新数据
        pullDisposable = Observable
                .create((@NonNull ObservableEmitter<List<RecommendApp>> e) -> {
                    String result = BmobUtils.findBQL("select * from RecommendApp");
                    Log.e("MainActivity", "---pullRecommendApps---: result=" + result);
                    //result={"results":[{"appDescription":"这是一款好玩的棋类游戏合集哦，有单机、双人、在线、 邀请四种模式，更可以添加好友以及邀请ta一起对战，","appIcon":"http://pp.myapp.com/ma_icon/0/icon_42342916_1498996465/96","appName":"人生如棋","appUrl":"http://android.myapp.com/myapp/detail.htm?apkName=com.ue.chess_life","createdAt":"2017-09-17 22:38:48","objectId":"hSovPPPg","updatedAt":"2017-09-17 22:40:35"}]}
                    //result=Not Found:(findBQL)https://api.bmob.cn/1/cloudQuery?bql=select+*+from+RecommendAppa&values=[]
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(recommendApps -> {
                    if (getContext() != null) {
                        adapter.setItems(new ArrayList<>(recommendApps));
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.vgListHeader) {
            int newState = bottomSheetBehavior.getState() == STATE_COLLAPSED ? STATE_EXPANDED : STATE_COLLAPSED;
            bottomSheetBehavior.setState(newState);
            return;
        }
    }

    @Override
    public void onDetachedFromWindow() {
        dispose(showDisposable);
        dispose(pullDisposable);
        super.onDetachedFromWindow();
    }

    private void dispose(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
