package com.ue.recommend.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import com.ue.adapterdelegate.Item

/**
 * Created by hawk on 2017/9/12.
 */
@Entity
class RecommendApp : Item {
    var appIcon: String? = null
    var appName: String? = null
    var appDescription: String? = null
    var appUrl: String? = null
    @PrimaryKey
    var packageName: String? = null
}