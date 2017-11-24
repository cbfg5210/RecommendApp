package com.ue.recommend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import com.ue.adapterdelegate.DelegationAdapter;
import com.ue.adapterdelegate.Item;
import com.ue.adapterdelegate.OnDelegateClickListener;
import com.ue.recommend.model.RecommendApp;

import java.util.List;

/**
 * Created by hawk on 2017/9/12.
 */
public class RecommendAppAdapter extends DelegationAdapter<Item> implements OnDelegateClickListener {
    private Activity activity;

    public RecommendAppAdapter(Activity activity, List<Item> items) {
        this.activity = activity;
        this.items = items;

        RecommendAppDelegate delegate = new RecommendAppDelegate(activity);
        delegate.setOnDelegateClickListener(this);
        this.addDelegate(delegate);
    }

    @Override
    public void onClick(View view, int position) {
        if (position < 0 || position >= getItemCount()) {
            return;
        }
        int viewId = view.getId();
        if (viewId == R.id.vgAppInfoPanel) {
            RecommendApp recommendApp = (RecommendApp) items.get(position);
            openBrowser(activity, recommendApp.appUrl);
            return;
        }
    }

    private void openBrowser(Context context, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception exp) {
            Toast.makeText(context, context.getString(R.string.error_open_browser), Toast.LENGTH_SHORT).show();
        }
    }
}