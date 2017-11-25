package com.ue.recommend;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.ue.recommend.model.RecommendApp;

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

    private BottomSheetBehavior bottomSheetBehavior;
    private RecommendAppAdapter adapter;

    private RecommendAppProxy mRecommendAppProxy;
    private Disposable showDisposable;
    private Disposable pullDisposable;

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
        vgListHeader.setOnClickListener(this);

        bottomSheetBehavior = BottomSheetBehavior.from(vgMainBottomSheet);

        adapter = new RecommendAppAdapter((Activity) getContext(), null);
        rvRecommendApps.setAdapter(adapter);

        mRecommendAppProxy = new RecommendAppProxy(getContext());
        //get recommended apps saved in local
        showDisposable = mRecommendAppProxy.getLocalRecommendApps()
                .subscribe(recommendApps -> {
                    if (getContext() != null) {
                        adapter.setItems(new ArrayList<>(recommendApps));
                        adapter.notifyDataSetChanged();
                    }
                });
        //pull recommended apps from server
        Observable<List<RecommendApp>> pullObservable = mRecommendAppProxy.pullRecommendApps();
        if (pullObservable != null) {
            pullDisposable = pullObservable.subscribe(recommendApps -> {
                if (getContext() != null) {
                    adapter.setItems(new ArrayList<>(recommendApps));
                    adapter.notifyDataSetChanged();
                }
            });
        }
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
