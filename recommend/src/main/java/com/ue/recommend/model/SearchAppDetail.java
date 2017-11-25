package com.ue.recommend.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.ue.adapterdelegate.Item;
import com.ue.recommend.util.GsonHolder;

/**
 * Created by hawk on 2017/11/25.
 */
@Entity
public class SearchAppDetail implements Item{
    public int categoryId;
    public String appName;
    public String iconUrl;
    public String description;
    @PrimaryKey
    private String pkgName;
    public transient String appUrl;

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
        appUrl="http://android.myapp.com/myapp/detail.htm?apkName="+pkgName;
    }

    public String getPkgName() {
        return pkgName;
    }

    @Override
    public String toString() {
        return GsonHolder.getGson().toJson(this);
    }
}
