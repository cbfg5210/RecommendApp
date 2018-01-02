package com.ue.recommend.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.ue.adapterdelegate.BaseAdapterDelegate;
import com.ue.adapterdelegate.Item;
import com.ue.recommend.R;
import com.ue.recommend.model.RecommendApp;

import java.util.List;

/**
 * Created by hawk on 2017/9/14.
 */
class RecommendAppDelegate extends BaseAdapterDelegate<Item> {
    private Activity mActivity;

    public RecommendAppDelegate(Activity activity) {
        super(activity, R.layout.item_recommend_app);
        this.mActivity = activity;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    public boolean isForViewType(@NonNull Item item) {
        return (item instanceof RecommendApp);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @NonNull Item item, @NonNull List payloads) {
        ViewHolder cHolder = (ViewHolder) holder;
        RecommendApp recommendApp = (RecommendApp) item;

        Picasso.with(mActivity)
                .load(recommendApp.appIcon)
                .into(cHolder.ivAppIcon);
        cHolder.tvAppName.setText(recommendApp.appName);
        cHolder.tvAppDescription.setText(recommendApp.appDescription);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAppIcon;
        TextView tvAppName;
        TextView tvAppDescription;

        ViewHolder(View itemView) {
            super(itemView);
            ivAppIcon = itemView.findViewById(R.id.ivAppIcon);
            tvAppName = itemView.findViewById(R.id.tvAppName);
            tvAppDescription = itemView.findViewById(R.id.tvAppDescription);
        }
    }
}