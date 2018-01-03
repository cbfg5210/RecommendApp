package com.ue.recommend

import android.app.Activity
import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.view.inputmethod.InputMethodManager
import com.ue.adapterdelegate.Item
import com.ue.recommend.adapter.RecommendAppAdapter
import com.ue.recommend.widget.NBottomSheetBehavior
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.layout_recommend_sheet.view.*
import java.util.*

class RecommendSheetView : CoordinatorLayout, View.OnClickListener {
    private lateinit var recommendAdapter: RecommendAppAdapter
    private lateinit var bottomSheetBehavior: NBottomSheetBehavior<*>
    private lateinit var mDataPresenter: SheetDataPresenter
    private var recommendDisposable: Disposable? = null

    val bannerContainer: ViewGroup
        get() = findViewById<ViewStub>(R.id.vsBannerContainer).inflate() as ViewGroup

    val state: Int
        get() = bottomSheetBehavior.state

    private val isViewValid: Boolean
        get() {
            context ?: return false
            if ((context is Activity) && ((context as Activity).isFinishing)) return false
            return true
        }

    private var onItemTouchListener: RecyclerView.OnItemTouchListener = object : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            setScrollable(vgSheetContainer, rv)
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    }


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        View.inflate(context, R.layout.layout_recommend_sheet, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        tvSheetTitle.setOnClickListener(this)
        bottomSheetBehavior = NBottomSheetBehavior.from<ViewGroup>(vgSheetContainer)
        bottomSheetBehavior.setBottomSheetCallback(object : NBottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == NBottomSheetBehavior.STATE_COLLAPSED) {
                    hideKeyBoard()
                }
            }
        })

        rvRecommendApps.addOnItemTouchListener(onItemTouchListener)
        //adapter初始化的时候传入new ArrayList，后续就不用判断items是否为null了
        recommendAdapter = RecommendAppAdapter(context as Activity, ArrayList<Item>())
        rvRecommendApps.adapter = recommendAdapter

        mDataPresenter = SheetDataPresenter(context)
        setupData()
    }

    private fun setupData() {
        pbPullProgress.visibility = View.VISIBLE
        dispose(recommendDisposable)

        recommendDisposable = mDataPresenter.recommendApps
                .subscribe({ recommendApps ->
                    if (!isViewValid) {
                        return@subscribe
                    }
                    pbPullProgress.visibility = View.GONE
                    if (recommendApps.isEmpty()) {
                        return@subscribe
                    }
                    recommendAdapter.items.addAll(recommendApps)
                    recommendAdapter.notifyDataSetChanged()

                }) { _ ->
                    if (!isViewValid || recommendAdapter.items.size > 0) {
                        return@subscribe
                    }
                    pbPullProgress.visibility = View.GONE
                }
    }

    override fun onClick(view: View) {
        if (view.id == R.id.tvSheetTitle) {
            if (bottomSheetBehavior.state == NBottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = NBottomSheetBehavior.STATE_EXPANDED
            }
            return
        }
    }

    fun addBannerAd(bannerView: View) {
        vgSheetContainer.addView(bannerView)
    }

    fun hideBottomSheet() {
        bottomSheetBehavior.state = NBottomSheetBehavior.STATE_COLLAPSED
    }

    override fun onDetachedFromWindow() {
        dispose(recommendDisposable)
        super.onDetachedFromWindow()
    }

    private fun hideKeyBoard() {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(vgSheetContainer.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun dispose(disposable: Disposable?) {
        if (disposable != null && !disposable.isDisposed) {
            disposable.dispose()
        }
    }

    private fun setScrollable(bottomSheet: View, recyclerView: RecyclerView) {
        val params = bottomSheet.layoutParams
        if (params is CoordinatorLayout.LayoutParams) {
            val behavior = params.behavior
            if (behavior != null && behavior is NBottomSheetBehavior<*>) {
                behavior.setNestedScrollingChildRef(recyclerView)
            }
        }
    }
}
