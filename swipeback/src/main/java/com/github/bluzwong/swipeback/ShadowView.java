package com.github.bluzwong.swipeback;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Created by wangzhijie on 2015/10/9.
 */
class ShadowView extends View {
    public ShadowView(Context context) {
        super(context);
    }

    private final static int SHADOW_WIDTH = 50;

    private Drawable leftShadow;
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.save();
        int right = getWidth();
        int left = right - SHADOW_WIDTH;
        int top = 0;
        int bot = getHeight();
        getLeftShadow().setBounds(left, top, right, bot);
        leftShadow.draw(canvas);
        canvas.restore();
    }

    private Drawable getLeftShadow() {
        if (leftShadow == null) {
            leftShadow = getResources().getDrawable(R.drawable.shadow_left_code);
        }
        return leftShadow;
    }
}
