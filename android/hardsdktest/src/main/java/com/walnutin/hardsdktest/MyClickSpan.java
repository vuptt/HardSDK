package com.walnutin.hardsdktest;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class MyClickSpan extends ClickableSpan {
    private int mHighLightColor = 0xffFf1a0F;
    private boolean mUnderLine = false;
    private View.OnClickListener mClickListener;

    public MyClickSpan(View.OnClickListener listener){
        this.mClickListener = listener;

    }

    public MyClickSpan(int color, boolean underline, View.OnClickListener listener){
        this.mHighLightColor = color;
        this.mUnderLine = underline;
        this.mClickListener = listener;

    }

    @Override
    public void onClick(View widget){
        if (mClickListener != null)
            mClickListener.onClick(widget);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(mHighLightColor);
        ds.setUnderlineText(mUnderLine);
    }


}


