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
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private ViewGroup vgSheetContainer;

    private View vgSheetHeader;
    private TextView tvSheetTitle;
    private View ivSheetSwitch;

    private RecyclerView rvRecommendApps;

    private View vgSearchPanel;
    private SearchPanelView spvSearchPanel;
    private RecyclerView rvSearchApps;

    private ProgressBar pbPullProgress;
    private View vgNoApps;
    private TextView tvNoAppReason;

    private BottomSheetBehavior bottomSheetBehavior;

    private RecommendAppProxy mRecommendAppProxy;
    private Disposable showDisposable;
    private Disposable pullDisposable;
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
        RecommendAppAdapter recommendAdapter = new RecommendAppAdapter((Activity) getContext(), new ArrayList<>());
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

        setupData();
    }

    private void setupData() {
        switchSheetContent(true);

        mRecommendAppProxy = new RecommendAppProxy(getContext());
        //get recommended apps saved in local
        showDisposable = mRecommendAppProxy.getLocalRecommendApps()
                .subscribe(recommendApps -> {
                    if (getContext() != null) {
                        RecommendAppAdapter adapter = (RecommendAppAdapter) rvRecommendApps.getAdapter();
                        adapter.getItems().addAll(recommendApps);
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
                    RecommendAppAdapter adapter = (RecommendAppAdapter) rvRecommendApps.getAdapter();
                    adapter.getItems().addAll(0, recommendApps);
                    adapter.notifyDataSetChanged();
                }
            }, throwable -> {
                Toast.makeText(getContext(), "请求数据出错:" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void hideKeyBoard() {
        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(tvSheetTitle.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
            RecommendAppAdapter searchAdapter = new RecommendAppAdapter((Activity) getContext(), new ArrayList<>());
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

    private void searchApps(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            Toast.makeText(getContext(), "please input keyword", Toast.LENGTH_SHORT).show();
            return;
        }
        hideKeyBoard();
        switchProgress(true);
        dispose(searchDisposable);
        searchDisposable = mRecommendAppProxy.searchApps(keyword)
                .subscribe(searchAppDetails -> {
                    pbPullProgress.setVisibility(View.GONE);

                    RecommendAppAdapter adapter = (RecommendAppAdapter) rvSearchApps.getAdapter();
                    adapter.getItems().clear();
                    adapter.getItems().addAll(searchAppDetails);
                    adapter.notifyDataSetChanged();
                }, throwable -> {
                    switchProgress(false);
                    showNoApps();
                    tvNoAppReason.setText("请求数据出错:" + throwable.getMessage());
                });
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
