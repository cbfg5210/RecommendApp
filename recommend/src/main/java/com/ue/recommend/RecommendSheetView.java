package com.ue.recommend;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.ue.adapterdelegate.Item;
import com.ue.recommend.adapter.RecommendAppAdapter;
import com.ue.recommend.model.RecommendApp;
import com.ue.recommend.util.RecommendAppProxy;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;

public class RecommendSheetView extends CoordinatorLayout implements View.OnClickListener {
    private ViewGroup vgMainBottomSheet;
    private RecyclerView rvRecommendApps;
    private ViewGroup vgSheetContentPanel;
    private View vgListHeader;
    private SearchPanelView spvSearchPanel;

    private BottomSheetBehavior bottomSheetBehavior;
    private RecommendAppAdapter adapter;
    private List<Item> mRecommendApps;

    private RecommendAppProxy mRecommendAppProxy;
    private Disposable showDisposable;
    private Disposable pullDisposable;
    private Disposable searchDisposable;

    private InputMethodManager inputManager;

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

        vgMainBottomSheet = findViewById(R.id.vgMainBottomSheet);
        rvRecommendApps = findViewById(R.id.rvRecommendApps);
        vgSheetContentPanel = findViewById(R.id.vgSheetContentPanel);
        vgListHeader = findViewById(R.id.vgListHeader);
        spvSearchPanel = findViewById(R.id.spvSearchPanel);
        spvSearchPanel.setSearchPanelListener(input -> {
            searchApps(input);
        });

        vgListHeader.setOnClickListener(this);

        bottomSheetBehavior = BottomSheetBehavior.from(vgMainBottomSheet);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == STATE_COLLAPSED) {
                    hideKeyBoard();
                    if (mRecommendApps != null && mRecommendApps.size() > 0) {
                        adapter.getItems().clear();
                        adapter.getItems().addAll(mRecommendApps);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
        //adapter初始化的时候传入new ArrayList，后续就不用判断items是否为null了
        adapter = new RecommendAppAdapter((Activity) getContext(), new ArrayList<>());
        rvRecommendApps.setAdapter(adapter);

        mRecommendAppProxy = new RecommendAppProxy(getContext());
        //get recommended apps saved in local
        showDisposable = mRecommendAppProxy.getLocalRecommendApps()
                .subscribe(recommendApps -> {
                    if (getContext() != null) {
                        mRecommendApps = new ArrayList<>(recommendApps);
                        adapter.getItems().addAll(0, mRecommendApps);
                        adapter.notifyDataSetChanged();
                    }
                }, throwable -> {
                    Toast.makeText(getContext(), "读取本地数据出错:" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });
        //pull recommended apps from server
        Observable<List<RecommendApp>> pullObservable = mRecommendAppProxy.pullRecommendApps();
        if (pullObservable != null) {
            pullDisposable = pullObservable.subscribe(recommendApps -> {
                if (getContext() != null) {
                    mRecommendApps = new ArrayList<>(recommendApps);
                    adapter.getItems().addAll(0, mRecommendApps);
                    adapter.notifyDataSetChanged();
                }
            }, throwable -> {
                Toast.makeText(getContext(), "请求数据出错:" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void hideKeyBoard() {
        if (inputManager == null) {
            inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        inputManager.hideSoftInputFromWindow(spvSearchPanel.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.vgListHeader) {
            int newState = bottomSheetBehavior.getState() == STATE_COLLAPSED ? STATE_EXPANDED : STATE_COLLAPSED;
            bottomSheetBehavior.setState(newState);
            return;
        }
    }

    private void searchApps(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            Toast.makeText(getContext(), "please input keyword", Toast.LENGTH_SHORT).show();
            return;
        }
        dispose(searchDisposable);
        searchDisposable = mRecommendAppProxy.searchApps(keyword)
                .subscribe(searchAppDetails -> {
                    adapter.getItems().clear();
                    adapter.getItems().addAll(searchAppDetails);
                    adapter.notifyDataSetChanged();
                }, throwable -> {
                    Toast.makeText(getContext(), "请求数据出错:" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDetachedFromWindow() {
        dispose(showDisposable);
        dispose(pullDisposable);
        dispose(searchDisposable);
        super.onDetachedFromWindow();
    }

    private void dispose(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
