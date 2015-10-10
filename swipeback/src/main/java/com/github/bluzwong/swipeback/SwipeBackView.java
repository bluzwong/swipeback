package com.github.bluzwong.swipeback;

import android.content.Context;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.MotionEvent;

/**
 * Created by wangzhijie on 2015/10/9.
 */
class SwipeBackView extends SlidingPaneLayout {
    public SwipeBackView(Context context) {
        super(context);
    }
    public boolean disallowIntercept = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_MOVE) {
            requestDisallowInterceptTouchEvent(disallowIntercept);
        }
        return super.dispatchTouchEvent(ev);
    }
}
