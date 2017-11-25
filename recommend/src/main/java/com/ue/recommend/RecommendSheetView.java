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
import android.widget.Button;
import android.widget.EditText;
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
    private ViewGroup vgMainBottomSheet;
    private RecyclerView rvRecommendApps;
    private ViewGroup vgSheetContentPanel;
    private View vgListHeader;
    private EditText etKeyword;
    private Button btnSearch;

    private BottomSheetBehavior bottomSheetBehavior;
    private RecommendAppAdapter adapter;

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
        etKeyword = findViewById(R.id.etKeyword);
        btnSearch = findViewById(R.id.btnSearch);

        vgListHeader.setOnClickListener(this);
        btnSearch.setOnClickListener(this);

        bottomSheetBehavior = BottomSheetBehavior.from(vgMainBottomSheet);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == STATE_COLLAPSED) {
                    if (inputManager == null) {
                        inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    }
                    inputManager.hideSoftInputFromWindow(etKeyword.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

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
                }, throwable -> {
                    Toast.makeText(getContext(), "读取本地数据出错:" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });
        //pull recommended apps from server
        Observable<List<RecommendApp>> pullObservable = mRecommendAppProxy.pullRecommendApps();
        if (pullObservable != null) {
            pullDisposable = pullObservable.subscribe(recommendApps -> {
                if (getContext() != null) {
                    adapter.setItems(new ArrayList<>(recommendApps));
                    adapter.notifyDataSetChanged();
                }
            }, throwable -> {
                Toast.makeText(getContext(), "请求数据出错:" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
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
        if (viewId == R.id.btnSearch) {
            searchApps();
            return;
        }
    }

    private void searchApps() {
        String keyword = etKeyword.getText().toString().trim();
        if (TextUtils.isEmpty(keyword)) {
            Toast.makeText(getContext(), "please input keyword", Toast.LENGTH_SHORT).show();
            return;
        }
        searchDisposable = mRecommendAppProxy.searchApps(keyword)
                .subscribe(searchAppDetails -> {
                    if (adapter.getItems() != null) {
                        adapter.getItems().addAll(searchAppDetails);
                        adapter.notifyDataSetChanged();
                    }
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
