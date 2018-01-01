package com.ue.recommend;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import com.ue.recommend.adapter.RecommendAppAdapter;
import com.ue.recommend.widget.NBottomSheetBehavior;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;

public class RecommendSheetView extends CoordinatorLayout implements View.OnClickListener {
    private ViewGroup vgSheetContainer;

    private RecyclerView rvRecommendApps;
    private RecommendAppAdapter recommendAdapter;

    private ProgressBar pbPullProgress;

    private NBottomSheetBehavior bottomSheetBehavior;

    private SheetDataPresenter mDataPresenter;
    private Disposable recommendDisposable;

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
        bottomSheetBehavior = NBottomSheetBehavior.from(vgSheetContainer);

        bottomSheetBehavior.setBottomSheetCallback(new NBottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == NBottomSheetBehavior.STATE_COLLAPSED) {
                    hideKeyBoard();
                }
            }
        });

        rvRecommendApps = findViewById(R.id.rvRecommendApps);
        rvRecommendApps.addOnItemTouchListener(onItemTouchListener);
        //adapter初始化的时候传入new ArrayList，后续就不用判断items是否为null了
        recommendAdapter = new RecommendAppAdapter((Activity) getContext(), new ArrayList<>());
        rvRecommendApps.setAdapter(recommendAdapter);

        mDataPresenter = new SheetDataPresenter(getContext());
        setupData();
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
                        return;
                    }
                    recommendAdapter.getItems().addAll(recommendApps);
                    recommendAdapter.notifyDataSetChanged();

                }, throwable -> {
                    if (!isViewValid() || recommendAdapter.getItems().size() > 0) {
                        return;
                    }
                    switchProgress(false);
                });
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.tvSheetTitle) {
            if (bottomSheetBehavior.getState() == NBottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(NBottomSheetBehavior.STATE_EXPANDED);
            }
            return;
        }
    }

    private void switchProgress(boolean isShow) {
        if (pbPullProgress == null) {
            if (isShow) {
                pbPullProgress = (ProgressBar) ((ViewStub) findViewById(R.id.vsProgressBar)).inflate();
            }
            return;
        }
        pbPullProgress.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void addBannerAd(View bannerView) {
        vgSheetContainer.addView(bannerView);
    }

    public ViewGroup getBannerContainer() {
        return (ViewGroup) ((ViewStub) findViewById(R.id.vsBannerContainer)).inflate();
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
        super.onDetachedFromWindow();
    }

    private void hideKeyBoard() {
        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(vgSheetContainer.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
