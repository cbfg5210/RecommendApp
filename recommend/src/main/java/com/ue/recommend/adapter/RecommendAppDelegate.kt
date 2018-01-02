package com.ue.recommend.adapter

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.squareup.picasso.Picasso
import com.ue.adapterdelegate.BaseAdapterDelegate
import com.ue.adapterdelegate.Item
import com.ue.recommend.R
import com.ue.recommend.model.RecommendApp

/**
 * Created by hawk on 2017/9/14.
 */
class RecommendAppDelegate(private val mActivity: Activity) : BaseAdapterDelegate<Item>(mActivity, R.layout.item_recommend_app) {

    override fun onCreateViewHolder(itemView: View): RecyclerView.ViewHolder {
        return ViewHolder(itemView)
    }

    override fun isForViewType(item: Item): Boolean {
        return item is RecommendApp
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item, payloads: List<Any>) {
        val cHolder = holder as ViewHolder
        val recommendApp = item as RecommendApp

        Picasso.with(mActivity)
                .load(recommendApp.appIcon)
                .into(cHolder.ivAppIcon)
        cHolder.tvAppName.text = recommendApp.appName
        cHolder.tvAppDescription.text = recommendApp.appDescription
    }

    internal class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivAppIcon: ImageView
        var tvAppName: TextView
        var tvAppDescription: TextView

        init {
            ivAppIcon = itemView.findViewById(R.id.ivAppIcon)
            tvAppName = itemView.findViewById(R.id.tvAppName)
            tvAppDescription = itemView.findViewById(R.id.tvAppDescription)
        }
    }
}