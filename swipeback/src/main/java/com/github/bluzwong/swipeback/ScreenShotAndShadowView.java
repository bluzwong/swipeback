package com.github.bluzwong.swipeback;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by wangzhijie on 2015/10/9.
 */
class ScreenShotAndShadowView extends ViewGroup {
    public ImageView imgView;
    public ShadowView shadowView;

    public ScreenShotAndShadowView(Context context) {
        super(context);
        imgView = new ImageView(context);
        shadowView = new ShadowView(context);
        addView(imgView);
        addView(shadowView);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        imgView.layout(i, i1, i2, i3);
        shadowView.layout(i, i1, i2, i3);
    }
}
