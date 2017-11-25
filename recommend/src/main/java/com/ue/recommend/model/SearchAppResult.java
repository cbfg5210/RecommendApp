package com.ue.recommend.model;

import com.ue.recommend.util.GsonHolder;

/**
 * Created by hawk on 2017/11/25.
 */

public class SearchAppResult {
    public boolean success;
    public String msg;
    public SearchAppObj obj;

    @Override
    public String toString() {
        return GsonHolder.getGson().toJson(this);
    }
}
