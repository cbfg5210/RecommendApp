package com.ue.recommend.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.widget.Toast

import com.ue.adapterdelegate.DelegationAdapter
import com.ue.adapterdelegate.Item
import com.ue.adapterdelegate.OnDelegateClickListener
import com.ue.recommend.R
import com.ue.recommend.model.RecommendApp

/**
 * Created by hawk on 2017/9/12.
 */
class RecommendAppAdapter(private val activity: Activity, items: List<Item>?) : DelegationAdapter<Item>(), OnDelegateClickListener {

    init {
        if (items != null) this.items.addAll(items)

        val delegate = RecommendAppDelegate(activity)
        delegate.onDelegateClickListener = this
        addDelegate(delegate)
    }

    override fun onClick(view: View, position: Int) {
        if (position < 0 || position >= itemCount) {
            return
        }
        val item = items[position]
        if (item is RecommendApp) {
            openBrowser(activity, item.appUrl)
            return
        }
    }

    private fun openBrowser(context: Context, url: String?) {
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(context, context.getString(R.string.error_open_browser), Toast.LENGTH_SHORT).show()
            return
        }
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            context.startActivity(intent)
        } catch (exp: Exception) {
            Toast.makeText(context, context.getString(R.string.error_open_browser), Toast.LENGTH_SHORT).show()
        }
    }
}