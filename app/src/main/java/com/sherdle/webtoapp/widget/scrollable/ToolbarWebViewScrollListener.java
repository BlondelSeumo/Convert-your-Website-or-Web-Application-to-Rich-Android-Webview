package com.sherdle.webtoapp.widget.scrollable;

import com.sherdle.webtoapp.widget.AdvancedWebView;

public abstract class ToolbarWebViewScrollListener implements AdvancedWebView.ScrollInterface {

    private static final int HIDE_THRESHOLD = 150;

    private int mScrolledDistance = 0;
    private boolean mControlsVisible = true;

    @Override
    public void onScrollChanged(AdvancedWebView toolbarWebView,int l, int t, int oldl, int oldt){
        if (toolbarWebView.getScrollY() == 0) {
            if(!mControlsVisible) {
                onShow();
                mControlsVisible = true;
            }
        } else {
            if (mScrolledDistance > HIDE_THRESHOLD && mControlsVisible) {
                onHide();
                mControlsVisible = false;
                mScrolledDistance = 0;
            } else if (mScrolledDistance < -HIDE_THRESHOLD && !mControlsVisible) {
                onShow();
                mControlsVisible = true;
                mScrolledDistance = 0;
            }
        }

        if((mControlsVisible && t-oldt>0) || (!mControlsVisible && t-oldt<0)) {
            mScrolledDistance += (t-oldt);
        }
    }

    public abstract void onHide();
    public abstract void onShow();
}