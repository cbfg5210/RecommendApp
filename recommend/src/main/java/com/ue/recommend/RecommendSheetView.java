package com.ue.recommend;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ue.recommend.adapter.RecommendAppAdapter;
import com.ue.recommend.db.RecommendAppDao;
import com.ue.recommend.db.RecommendDatabase;
import com.ue.recommend.model.RecommendApp;
import com.ue.recommend.model.RecommendAppResult;
import com.ue.recommend.model.SearchAppDetail;
import com.ue.recommend.model.SearchAppResult;
import com.ue.recommend.util.BmobUtils;
import com.ue.recommend.util.GsonHolder;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;

public class RecommendSheetView extends CoordinatorLayout implements View.OnClickListener {
    private static final String LAST_PULL_TIME = "lastPullTime";

    private ViewGroup vgSheetContainer;

    private View vgSheetHeader;
    private TextView tvSheetTitle;
    private View ivSheetSwitch;

    private RecyclerView rvRecommendApps;
    private RecommendAppAdapter recommendAdapter;

    private View vgSearchPanel;
    private SearchPanelView spvSearchPanel;
    private RecyclerView rvSearchApps;
    private RecommendAppAdapter searchAdapter;

    private ProgressBar pbPullProgress;
    private View vgNoApps;
    private TextView tvNoAppReason;

    private BottomSheetBehavior bottomSheetBehavior;

    private Disposable recommendDisposable;
    private Disposable searchDisposable;

    public RecommendSheetView(Context context) {
        this(context, null, 0);
    }

    public RecommendSheetView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecommendSheetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.layout_recommend_sheet, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        vgSheetContainer = findViewById(R.id.vgSheetContainer);
        vgSheetHeader = findViewById(R.id.vgSheetHeader);
        tvSheetTitle = findViewById(R.id.tvSheetTitle);
        ivSheetSwitch = findViewById(R.id.ivSheetSwitch);

        rvRecommendApps = findViewById(R.id.rvRecommendApps);
        //adapter初始化的时候传入new ArrayList，后续就不用判断items是否为null了
        recommendAdapter = new RecommendAppAdapter((Activity) getContext(), new ArrayList<>());
        rvRecommendApps.setAdapter(recommendAdapter);

        ivSheetSwitch.setOnClickListener(this);
        vgSheetHeader.setOnClickListener(this);

        bottomSheetBehavior = BottomSheetBehavior.from(vgSheetContainer);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == STATE_COLLAPSED) {
                    hideKeyBoard();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        switchSheetContent(true);
        setupData();
    }

    private void setupData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        RecommendAppDao mRecommendAppDao = RecommendDatabase.getInstance(getContext()).recommendAppDao();

        switchProgress(true);
        dispose(recommendDisposable);
        recommendDisposable = Observable
                .create((ObservableEmitter<List<RecommendApp>> e) -> {
                    //从本地获取数据
                    List<RecommendApp> recommendApps = mRecommendAppDao.getRecommendApps();
                    Log.e("RecommendSheetView", "setupData: local data=" + recommendApps);
                    boolean hasRecommendApps = (recommendApps != null && recommendApps.size() > 0);
                    if (hasRecommendApps) {
                        e.onNext(recommendApps);
                    }
                    //更新数据
                    long cacheTime = sharedPreferences.getLong(LAST_PULL_TIME, 0);
                    if (System.currentTimeMillis() - cacheTime > 86400000) {
                        //24*60*60*1000=86400000,缓存时间大于一天才重新获取数据
                        String bql = String.format("select * from RecommendApp where packageName!='%s'", getContext().getPackageName());
                        String result = BmobUtils.getInstance().findBQL(bql);
                        Log.e("RecommendSheetView", "setupData: server data=" + result);

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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(recommendApps -> {
                    if (!isViewValid()) {
                        return;
                    }
                    switchProgress(false);
                    if (recommendApps.size() == 0) {
                        showNoApps();
                        tvNoAppReason.setText(getContext().getString(R.string.no_recommend_app));
                        return;
                    }
                    recommendAdapter.getItems().addAll(recommendApps);
                    recommendAdapter.notifyDataSetChanged();

                }, throwable -> {
                    if (!isViewValid() || recommendAdapter.getItems().size() > 0) {
                        return;
                    }
                    switchProgress(false);
                    showNoApps();
                    tvNoAppReason.setText(getContext().getString(R.string.error_search) + throwable.getMessage());
                });
    }

    private void searchApps(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            Toast.makeText(getContext(), R.string.input_keyword, Toast.LENGTH_SHORT).show();
            return;
        }
        hideKeyBoard();
        switchProgress(true);
        dispose(searchDisposable);

        searchDisposable = Observable
                .create((ObservableEmitter<List<SearchAppDetail>> e) -> {
                    String result = BmobUtils.getInstance().search(keyword).trim();
                    Log.e("RecommendAppProxy", "searchApps: result=" + result);

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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(searchAppDetails -> {
                    if (!isViewValid()) {
                        return;
                    }
                    switchProgress(false);
                    if (searchAppDetails.size() == 0) {
                        showNoApps();
                        return;
                    }
                    searchAdapter.getItems().clear();
                    searchAdapter.getItems().addAll(searchAppDetails);
                    searchAdapter.notifyDataSetChanged();
                }, throwable -> {
                    if (!isViewValid()) {
                        return;
                    }
                    switchProgress(false);
                    showNoApps();
                    tvNoAppReason.setText(getContext().getString(R.string.error_search) + throwable.getMessage());
                });
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.vgSheetHeader) {
            if (bottomSheetBehavior.getState() == STATE_COLLAPSED) {
                bottomSheetBehavior.setState(STATE_EXPANDED);
            }
            return;
        }
        if (viewId == R.id.ivSheetSwitch) {
            if (bottomSheetBehavior.getState() == STATE_COLLAPSED) {
                bottomSheetBehavior.setState(STATE_EXPANDED);
            }
            switchSheetContent(!ivSheetSwitch.isSelected());
            return;
        }
    }

    private void switchSheetContent(boolean switchRecommend) {
        ivSheetSwitch.setSelected(switchRecommend);
        tvSheetTitle.setSelected(switchRecommend);

        if (switchRecommend) {
            tvSheetTitle.setText(R.string.recommend_app);
            rvRecommendApps.setVisibility(View.VISIBLE);
            if (vgSearchPanel != null) {
                vgSearchPanel.setVisibility(View.GONE);
            }
            return;
        }
        if (vgSearchPanel == null) {
            /*init search part*/
            vgSearchPanel = ((ViewStub) findViewById(R.id.vsSearchAppPanel)).inflate();
            spvSearchPanel = findViewById(R.id.spvSearchApp);
            rvSearchApps = findViewById(R.id.rvSearchApps);
            //adapter初始化的时候传入new ArrayList，后续就不用判断items是否为null了
            searchAdapter = new RecommendAppAdapter((Activity) getContext(), new ArrayList<>());
            rvSearchApps.setAdapter(searchAdapter);

            spvSearchPanel.setSearchPanelListener(input -> {
                searchApps(input);
            });
        }
        /*switch search part*/
        tvSheetTitle.setText(R.string.search_app);
        vgSearchPanel.setVisibility(View.VISIBLE);
        rvRecommendApps.setVisibility(View.GONE);
    }

    private void switchProgress(boolean isShow) {
        //hide progress
        if (!isShow) {
            pbPullProgress.setVisibility(View.GONE);
            return;
        }
        //show progress and hide vgNoApps
        if (vgNoApps != null) {
            vgNoApps.setVisibility(View.GONE);
        }
        if (pbPullProgress == null) {
            pbPullProgress = (ProgressBar) ((ViewStub) findViewById(R.id.vsProgressBar)).inflate();
        } else {
            pbPullProgress.setVisibility(View.VISIBLE);
        }
    }

    private void showNoApps() {
        if (vgNoApps == null) {
            vgNoApps = ((ViewStub) findViewById(R.id.vsNoApps)).inflate();
            tvNoAppReason = findViewById(R.id.tvNoAppReason);
            return;
        }
        vgNoApps.setVisibility(View.VISIBLE);
    }

    public void addBannerAd(View bannerView) {
        bannerView.setBackgroundColor(Color.WHITE);
        vgSheetContainer.addView(bannerView);
    }

    public int getState() {
        return bottomSheetBehavior.getState();
    }

    public void hideBottomSheet() {
        bottomSheetBehavior.setState(STATE_COLLAPSED);
    }

    @Override
    public void onDetachedFromWindow() {
        dispose(recommendDisposable);
        dispose(searchDisposable);
        super.onDetachedFromWindow();
    }

    private void hideKeyBoard() {
        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(tvSheetTitle.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private boolean isViewValid() {
        return (getContext() != null && ViewCompat.isAttachedToWindow(this));
    }

    private void dispose(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
