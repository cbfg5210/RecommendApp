package com.ue.recommend.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.ue.adapterdelegate.Item;

/**
 * Created by hawk on 2017/9/12.
 */
@Entity
public class RecommendApp implements Item {
    public String appIcon;
    public String appName;
    public String appDescription;
    public String appUrl;
    @PrimaryKey
    public String packageName;
}