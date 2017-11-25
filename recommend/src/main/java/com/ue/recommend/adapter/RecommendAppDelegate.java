package com.ue.recommend.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
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
    private Activity activity;

    public RecommendAppDelegate(Activity mActivity) {
        super(mActivity, R.layout.item_recommend_app);
        activity = mActivity;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(@NonNull View itemView) {
        final ViewHolder cHolder = new ViewHolder(itemView);
        View.OnClickListener onClickListener = view -> {
            if (onDelegateClickListener != null) {
                onDelegateClickListener.onClick(view, cHolder.getAdapterPosition());
            }
        };
        cHolder.vgAppInfoPanel.setOnClickListener(onClickListener);
        return cHolder;
    }

    @Override
    public boolean isForViewType(@NonNull Item item) {
        return (item instanceof RecommendApp);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @NonNull Item item, @NonNull List payloads) {
        ViewHolder cHolder = (ViewHolder) holder;
        RecommendApp recommendApp = (RecommendApp) item;

        Picasso.with(activity)
                .load(recommendApp.appIcon)
                .into(cHolder.ivAppIcon);
        cHolder.tvAppName.setText(recommendApp.appName);
        cHolder.tvAppDescription.setText(recommendApp.appDescription);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewGroup vgAppInfoPanel;
        ImageView ivAppIcon;
        TextView tvAppName;
        TextView tvAppDescription;

        ViewHolder(View itemView) {
            super(itemView);
            vgAppInfoPanel = itemView.findViewById(R.id.vgAppInfoPanel);
            ivAppIcon = itemView.findViewById(R.id.ivAppIcon);
            tvAppName = itemView.findViewById(R.id.tvAppName);
            tvAppDescription = itemView.findViewById(R.id.tvAppDescription);
        }
    }
}