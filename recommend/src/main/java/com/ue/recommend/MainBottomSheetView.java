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

import com.ue.adapterdelegate.Item;
import com.ue.recommend.model.RecommendApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

//        pullRecommendApps();
        showTestRecommendApps();
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

    public void showTestRecommendApps() {
        List<Item> recommendApps = new ArrayList<>();

        RecommendApp recommendApp = new RecommendApp();
        recommendApp.appIcon = "https://github.com/bumptech/glide/raw/master/static/glide_logo.png";
        recommendApp.appName = "appNameappNameappNameappNameappName";
        recommendApp.appDescription = "appDescriptionappDescriptionappDescriptionappDescriptionappDescriptionappDescriptionappDescriptionappDescription";
        recommendApp.appUrl = "https://github.com/bumptech/glide";

        for (int i = 0; i < 20; i++) {
            recommendApps.add(recommendApp);
        }
        adapter.setItems(recommendApps);
        adapter.notifyDataSetChanged();
    }

    /*private void pullRecommendApps() {
        String cacheRecommendAppsStr = sharedPreferences.getString(SPKeys.CACHE_RECOMMEND_APPS, "");
        if (!TextUtils.isEmpty(cacheRecommendAppsStr)) {
            showRecommendApps(cacheRecommendAppsStr);
        }

        long cacheTime = sharedPreferences.getLong(LAST_PULL_TIME, 0);
        if (System.currentTimeMillis() - cacheTime < 24 * 3600000) {
            return;
        }
        //刷新数据
        Observable
                .create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                        String result = BmobUtils.findBQL("select * from RecommendApp");
                        Log.e("MainActivity", "---pullRecommendApps---: result=" + result);
                        //result={"results":[{"appDescription":"这是一款好玩的棋类游戏合集哦，有单机、双人、在线、 邀请四种模式，更可以添加好友以及邀请ta一起对战，","appIcon":"http://pp.myapp.com/ma_icon/0/icon_42342916_1498996465/96","appName":"人生如棋","appUrl":"http://android.myapp.com/myapp/detail.htm?apkName=com.ue.chess_life","createdAt":"2017-09-17 22:38:48","objectId":"hSovPPPg","updatedAt":"2017-09-17 22:40:35"}]}
                        //result=Not Found:(findBQL)https://api.bmob.cn/1/cloudQuery?bql=select+*+from+RecommendAppa&values=[]
                        if (result.contains("appName")) {
                            sharedPreferences.putString(SPKeys.CACHE_RECOMMEND_APPS, result);
                            sharedPreferences.putLong(LAST_PULL_TIME, System.currentTimeMillis());

                            e.onNext(result);
                        }
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String result) throws Exception {
                        showRecommendApps(result);
                    }
                });
    }*/

    private void showRecommendApps(String result) {
        Log.e("MainActivity", "---showRecommendApps---: result=" + result);
        if (getContext() == null) {
            return;
        }
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            jsonObject = new JSONObject();
        }

        JSONArray resultsArray = jsonObject.optJSONArray("results");
        if (resultsArray.length() == 0) {
            return;
        }

        List<Item> recommendApps = new ArrayList<>();
        String packageName;
        String thisPackageName = getContext().getPackageName();
        boolean isZh = Locale.getDefault().getLanguage().equals("zh");
        for (int i = 0, len = resultsArray.length(); i < len; i++) {
            JSONObject itemObj = resultsArray.optJSONObject(i);
            //如果是本应用则不显示
            packageName = itemObj.optString("packageName", "");
            if (packageName.equals(thisPackageName)) {
                continue;
            }
            RecommendApp recommendApp = new RecommendApp();
            if (isZh) {
                recommendApp.appName = itemObj.optString("appName", "");
                recommendApp.appDescription = itemObj.optString("appDescription", "");
            } else {
                recommendApp.appName = itemObj.optString("appNameEn", "");
                recommendApp.appDescription = itemObj.optString("appDescriptionEn", "");
            }
            recommendApp.appIcon = itemObj.optString("appIcon", "");
            recommendApp.appUrl = itemObj.optString("appUrl", "");
            recommendApp.packageName = packageName;
            recommendApps.add(recommendApp);
        }
        adapter.setItems(recommendApps);
        adapter.notifyDataSetChanged();
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
}
