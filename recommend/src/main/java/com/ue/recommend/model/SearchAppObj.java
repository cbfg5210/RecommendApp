package com.ue.recommend.model;

import android.text.TextUtils;

import com.ue.recommend.util.GsonHolder;

import java.util.List;

/**
 * Created by hawk on 2017/11/25.
 */

public class SearchAppObj {
    public String pageNumberStack;
    public List<SearchAppItem> items;

    public boolean hasNext() {
        return !TextUtils.isEmpty(pageNumberStack);
    }

    @Override
    public String toString() {
        return GsonHolder.getGson().toJson(this);
    }
}
