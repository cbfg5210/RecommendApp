package com.ue.recommend.model;

import com.ue.recommend.util.GsonHolder;

/**
 * Created by hawk on 2017/11/25.
 */

public class SearchAppItem {
    public SearchAppDetail appDetail;

    @Override
    public String toString() {
        return GsonHolder.getGson().toJson(this);
    }
}
