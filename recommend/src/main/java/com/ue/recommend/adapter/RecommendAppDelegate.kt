package com.ue.recommend.adapter

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.View
import com.squareup.picasso.Picasso
import com.ue.adapterdelegate.BaseAdapterDelegate
import com.ue.adapterdelegate.Item
import com.ue.recommend.R
import com.ue.recommend.model.RecommendApp
import kotlinx.android.synthetic.main.item_recommend_app.view.*

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
        holder as ViewHolder
        item as RecommendApp

        Picasso.with(mActivity)
                .load(item.appIcon)
                .into(holder.ivAppIcon)
        holder.tvAppName.text = item.appName
        holder.tvAppDescription.text = item.appDescription
    }

    internal class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAppIcon = itemView.ivAppIcon!!
        val tvAppName = itemView.tvAppName!!
        val tvAppDescription = itemView.tvAppDescription!!
    }
}