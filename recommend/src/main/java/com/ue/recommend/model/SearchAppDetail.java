package com.ue.recommend.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.ue.adapterdelegate.Item;
import com.ue.recommend.util.GsonHolder;

/**
 * Created by hawk on 2017/11/25.
 */
@Entity
public class SearchAppDetail implements Item {
    public String name;
    public String iconUrl;
    public String editorIntro;
    @PrimaryKey
    private String pName;
    public transient String appUrl;

    public void setPName(String pName) {
        this.pName = pName;
        appUrl = "http://android.myapp.com/myapp/detail.htm?apkName=" + pName;
    }

    @Override
    public String toString() {
        return GsonHolder.getGson().toJson(this);
    }
}
