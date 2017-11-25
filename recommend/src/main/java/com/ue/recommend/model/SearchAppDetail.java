package com.ue.recommend.model;

import com.ue.adapterdelegate.Item;
import com.ue.recommend.util.GsonHolder;

/**
 * Created by hawk on 2017/11/25.
 */
public class SearchAppDetail implements Item {
    public String name;
    public String iconUrl;
    public String editorIntro;
    public String pName;

    public String getAppUrl() {
        return "http://android.myapp.com/myapp/detail.htm?apkName=" + pName;
    }

    @Override
    public String toString() {
        return GsonHolder.getGson().toJson(this);
    }
}
