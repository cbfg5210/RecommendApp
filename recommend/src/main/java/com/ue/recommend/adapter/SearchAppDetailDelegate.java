package com.ue.recommend.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.ue.adapterdelegate.BaseAdapterDelegate;
import com.ue.adapterdelegate.Item;
import com.ue.recommend.R;
import com.ue.recommend.model.SearchAppDetail;

import java.util.List;

/**
 * Created by hawk on 2017/11/25.
 */

public class SearchAppDetailDelegate extends BaseAdapterDelegate<Item> {
    private Activity mActivity;

    public SearchAppDetailDelegate(Activity activity) {
        super(activity, R.layout.item_recommend_app);
        mActivity = activity;
    }

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(@NonNull View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    public boolean isForViewType(@NonNull Item item) {
        return (item instanceof SearchAppDetail);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @NonNull Item item, @NonNull List payloads) {
        ViewHolder cHolder = (ViewHolder) holder;
        SearchAppDetail recommendApp = (SearchAppDetail) item;

        Picasso.with(mActivity)
                .load(recommendApp.iconUrl)
                .into(cHolder.ivAppIcon);
        cHolder.tvAppName.setText(recommendApp.name);
        cHolder.tvAppDescription.setText(TextUtils.isEmpty(recommendApp.editorIntro) ? recommendApp.name : recommendApp.editorIntro);
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
