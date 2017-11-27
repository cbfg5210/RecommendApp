package com.ue.recommend.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ue.recommend.R;

/**
 * Created by hawk on 2017/11/25.
 */

public class SearchPanelView extends FrameLayout {
    private ClearEditText etSearchInput;
    private ImageView ivSearchAction;
    private OnSearchPanelListener mSearchPanelListener;

    private int clearImageRes;
    private int searchImageRes;

    public void setSearchPanelListener(OnSearchPanelListener searchPanelListener) {
        mSearchPanelListener = searchPanelListener;
    }

    public SearchPanelView(@NonNull Context context) {
        this(context, null, 0);
    }

    public SearchPanelView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchPanelView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SearchPanelView);
        clearImageRes = ta.getResourceId(R.styleable.SearchPanelView_clearImageRes, R.drawable.svg_clear_input);
        searchImageRes = ta.getResourceId(R.styleable.SearchPanelView_searchImageRes, R.drawable.svg_clear_input);
        ta.recycle();

        View.inflate(context, R.layout.view_search_panel, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        etSearchInput = findViewById(R.id.etSearchInput);
        ivSearchAction = findViewById(R.id.ivSearchAction);

        etSearchInput.setClearImageRes(clearImageRes);
        ivSearchAction.setImageResource(searchImageRes);

        ivSearchAction.setOnClickListener(view -> {
            mSearchPanelListener.onSearchClicked(etSearchInput.getText().toString().trim());
        });
    }

    public interface OnSearchPanelListener {
        void onSearchClicked(String input);
    }
}
