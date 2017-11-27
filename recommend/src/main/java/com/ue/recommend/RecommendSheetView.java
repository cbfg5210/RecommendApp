package com.ue.recommend;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ue.recommend.adapter.RecommendAppAdapter;
import com.ue.recommend.widget.NBottomSheetBehavior;
import com.ue.recommend.widget.SearchPanelView;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;

public class RecommendSheetView extends CoordinatorLayout implements View.OnClickListener {
    private ViewGroup vgSheetContainer;

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

    private NBottomSheetBehavior bottomSheetBehavior;

    private SheetDataPresenter mDataPresenter;
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

        tvSheetTitle = findViewById(R.id.tvSheetTitle);
        ivSheetSwitch = findViewById(R.id.ivSheetSwitch);
        vgSheetContainer = findViewById(R.id.vgSheetContainer);
        bottomSheetBehavior = NBottomSheetBehavior.from(vgSheetContainer);

        ivSheetSwitch.setOnClickListener(this);
        findViewById(R.id.vgSheetHeader).setOnClickListener(this);
        bottomSheetBehavior.setBottomSheetCallback(new NBottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == NBottomSheetBehavior.STATE_COLLAPSED) {
                    hideKeyBoard();
                }
            }
        });
        initSheetContent(true);
        switchSheetContent(true);

        mDataPresenter = new SheetDataPresenter(getContext());
        setupData();
    }

    private void initSheetContent(boolean isRecommended) {
        if (isRecommended) {
            rvRecommendApps = findViewById(R.id.rvRecommendApps);
            rvRecommendApps.addOnItemTouchListener(onItemTouchListener);
            //adapter初始化的时候传入new ArrayList，后续就不用判断items是否为null了
            recommendAdapter = new RecommendAppAdapter((Activity) getContext(), new ArrayList<>());
            rvRecommendApps.setAdapter(recommendAdapter);
            return;
        }
        //搜索
        vgSearchPanel = ((ViewStub) findViewById(R.id.vsSearchAppPanel)).inflate();
        spvSearchPanel = findViewById(R.id.spvSearchApp);
        rvSearchApps = findViewById(R.id.rvSearchApps);
        rvSearchApps.addOnItemTouchListener(onItemTouchListener);
        //adapter初始化的时候传入new ArrayList，后续就不用判断items是否为null了
        searchAdapter = new RecommendAppAdapter((Activity) getContext(), new ArrayList<>());
        rvSearchApps.setAdapter(searchAdapter);

        spvSearchPanel.setSearchPanelListener(input -> {
            searchApps(input);
        });
    }

    private void setupData() {
        switchProgress(true);
        dispose(recommendDisposable);

        recommendDisposable = mDataPresenter.getRecommendApps()
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

        searchDisposable = mDataPresenter.searchApps(keyword)
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
            if (bottomSheetBehavior.getState() == NBottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(NBottomSheetBehavior.STATE_EXPANDED);
            }
            return;
        }
        if (viewId == R.id.ivSheetSwitch) {
            if (bottomSheetBehavior.getState() == NBottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(NBottomSheetBehavior.STATE_EXPANDED);
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
            initSheetContent(false);
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
        bannerView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.col_sheet_list));
        vgSheetContainer.addView(bannerView);
    }

    public int getState() {
        return bottomSheetBehavior.getState();
    }

    public void hideBottomSheet() {
        bottomSheetBehavior.setState(NBottomSheetBehavior.STATE_COLLAPSED);
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
        Context context = getContext();
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing()) {
                return false;
            }
        }
        return true;
    }

    private void dispose(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    RecyclerView.OnItemTouchListener onItemTouchListener = new RecyclerView.OnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            setScrollable(vgSheetContainer, rv);
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    };

    private void setScrollable(View bottomSheet, RecyclerView recyclerView) {
        ViewGroup.LayoutParams params = bottomSheet.getLayoutParams();
        if (params instanceof CoordinatorLayout.LayoutParams) {
            CoordinatorLayout.LayoutParams coordinatorLayoutParams = (CoordinatorLayout.LayoutParams) params;
            CoordinatorLayout.Behavior behavior = coordinatorLayoutParams.getBehavior();
            if (behavior != null && behavior instanceof NBottomSheetBehavior) {
                ((NBottomSheetBehavior) behavior).setNestedScrollingChildRef(recyclerView);
            }
        }
    }
}
