package com.ue.recommendapp;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by hawk on 2017/11/24.
 */
@Entity
public class TestDb {
    @PrimaryKey
    public long id;
    public int abc=1;
}
