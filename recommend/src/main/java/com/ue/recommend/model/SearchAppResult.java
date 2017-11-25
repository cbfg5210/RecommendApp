package com.ue.recommend.model;

import com.ue.recommend.util.GsonHolder;

import java.util.List;

/**
 * Created by hawk on 2017/11/25.
 */

public class SearchAppResult {
    public List<SearchAppDetail> apps;

    @Override
    public String toString() {
        return GsonHolder.getGson().toJson(this);
    }
}
