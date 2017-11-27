package com.ue.recommend.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;

/**
 * 两种模式：
 * 一、清除内容
 * 二、显示明文或密文
 */
public class ClearEditText extends AppCompatEditText implements OnFocusChangeListener, TextWatcher {
    private Drawable mClearDrawable;

    public ClearEditText(Context context) {
        this(context, null);
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        //这里构造方法也很重要，不加这个很多属性不能再XML里面定义
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    public void setClearImageRes(int imageRes) {
        setClearImageDrawable(imageRes <= 0 ? null : getResources().getDrawable(imageRes));
    }

    public void setClearImageDrawable(Drawable clearDrawable) {
        mClearDrawable = clearDrawable == null ?
                new ColorDrawable() :
                clearDrawable;

        mClearDrawable.setBounds(
                0,
                0,
                mClearDrawable.getIntrinsicWidth(),
                mClearDrawable.getIntrinsicHeight());
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        setClearImageDrawable(getCompoundDrawables()[2]);
        //默认设置隐藏图标
        setClearIconVisible(false);
        //设置焦点改变的监听
        setOnFocusChangeListener(this);
        //设置输入框里面内容发生改变的监听
        addTextChangedListener(this);
    }


    /**
     * 因为我们不能直接给EditText设置点击事件，所以我们用记住我们按下的位置来模拟点击事件
     * 当我们按下的位置 在  EditText的宽度 - 图标到控件右边的间距 - 图标的宽度  和
     * EditText的宽度 - 图标到控件右边的间距之间我们就算点击了图标，竖直方向就没有考虑
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getCompoundDrawables()[2] != null) {
                int textWidth = getWidth() - getTotalPaddingRight();
                int etWidth = getWidth() - getPaddingRight();
                boolean touchable = (event.getX() > textWidth) && (event.getX() < etWidth);

                if (touchable) {
                    setText("");
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 当ClearEditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        setClearIconVisible(hasFocus && (getText().length() > 0));
    }


    /**
     * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
     *
     * @param visible
     */
    protected void setClearIconVisible(boolean visible) {
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawables(
                getCompoundDrawables()[0],
                getCompoundDrawables()[1],
                right,
                getCompoundDrawables()[3]);
    }


    /**
     * 当输入框里面内容发生变化的时候回调的方法
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {
        setClearIconVisible((s.length() > 0));
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}