package com.github.bluzwong.swipeback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.v4.util.LruCache;
import android.support.v4.widget.SlidingPaneLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangzhijie on 2015/10/9.
 */
public class SwipeBackActivityHelper {
    public static final String DEBUG_TAG = "SwipeBackActivityHelper";
    public static final String KEY_HASH = "^^hash$$";
    private Activity activity;
    private SwipeBackView swipeBackView;
    private int hashCode = 0;
    private String fileName = "";
    private static boolean debug = false;

    private ScreenShotAndShadowView leftView;
    public void init(final Activity activity) {
        this.activity = activity;
        final int screenShotHashCode = activity.getIntent().getIntExtra(KEY_HASH, 0);
        if (screenShotHashCode != 0) {
            hashCode = screenShotHashCode;
            fileName = getFileName(activity, screenShotHashCode);
            logD("got prev activity screen shot success: " + fileName);
        } else {
            logW("cannot got prev activity screen shot success, please use helper.startActivity() instead");
        }
        swipeBackView = new SwipeBackView(activity);
        setEdgeMode(isEdgeMode);
        try {
            Field field_overHandSize = SlidingPaneLayout.class.getDeclaredField("mOverhangSize");
            field_overHandSize.setAccessible(true);
            field_overHandSize.set(swipeBackView, 0);
            leftView = new ScreenShotAndShadowView(activity);
            leftView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            setNeedBackgroundShadow(needBackgroundShadow);
            swipeBackView.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(View view, float v) {
                    float x = view.getX() - leftView.getWidth();
                    logD(" set x = " + x);
                    leftView.shadowView.setX(x);
                    if (isParallax) {
                        leftView.imgView.setX(x / parallaxRatio);
                    }
                    if (needBackgroundShadow) {
                        float xPercent = (-x) / leftView.getWidth();
                        leftView.imgViewHover.setAlpha(xPercent);
                    }

                    if (!TextUtils.isEmpty(fileName) && leftView.getTag() == null) {
                        Bitmap bitmap = cachedScreenShot.get(fileName);
                        if (bitmap == null) {
                            logD("screen shot cache with name " + fileName + "is missed, try load it from file");
                            File file = new File(fileName);
                            if (file.exists()) {
                                bitmap = BitmapFactory.decodeFile(fileName);
                                if (bitmap != null) {
                                    logD("load " + fileName + " success, save to cache");
                                    cachedScreenShot.put(fileName, bitmap);
                                }
                            }
                        } else {
                            logD("load " + fileName + " from cache");
                        }
                        if (bitmap != null) {
                            leftView.imgView.setImageBitmap(bitmap);
                            leftView.setTag(1);
                        }
                    }
                }

                @Override
                public void onPanelOpened(View view) {
                    logD("swiped back, remove screen shot " + fileName);
                    removeScreenShot(activity, screenShotHashCode);
                    activity.finish();
                    activity.overridePendingTransition(0, R.anim.slide_out_right);
                    if (hashCode != 0) {
                        logD("after finish, start remove screen shot " + hashCode);
                        removeScreenShot(activity, hashCode);
                    }
                }

                @Override
                public void onPanelClosed(View view) {
                    logD("stop swipe, remove screen shot");
                    leftView.imgView.setImageBitmap(null);
                    leftView.setTag(null);
                }
            });
            swipeBackView.setSliderFadeColor(Color.TRANSPARENT);
            swipeBackView.addView(leftView, 0);
            ViewGroup decorView = getDecorView();
            LinearLayout decorChild = (LinearLayout) decorView.getChildAt(0);
            FrameLayout contentFrame = (FrameLayout) decorChild.getChildAt(1);
            View contentView = contentFrame.getChildAt(0);
            contentView.setBackgroundColor(Color.WHITE);
            swipeBackView.setLayoutParams(contentView.getLayoutParams());
            contentFrame.removeView(contentView);
            swipeBackView.addView(contentView, 1);
            contentFrame.addView(swipeBackView);
            logD("init ok");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public void finish() {
        swipeBackView.openPane();
    }

    public void disableSwipeBack() {
        logD("disableSwipeBack");
        swipeBackView.disallowIntercept = true;
    }

    public void enableSwipeBack() {
        logD("enableSwipeBack");
        swipeBackView.disallowIntercept = false;
    }

    private boolean isEdgeMode = false;

    public void setEdgeMode(boolean edgeMode) {
        isEdgeMode = edgeMode;
        if (swipeBackView != null) {
            swipeBackView.isEdgeMode = isEdgeMode;
        }
    }

    private boolean isParallax = false;

    public void setParallaxMode(boolean isParallax) {
        this.isParallax = isParallax;
    }

    private int parallaxRatio = 2;
    public void setParallaxRatio(int ratio) {
        parallaxRatio = Math.max(1, Math.min(ratio, Integer.MAX_VALUE));
    }

    private boolean needBackgroundShadow = false;
    public void setNeedBackgroundShadow(boolean ifNeed) {
        needBackgroundShadow = ifNeed;
        if (leftView != null) {
            leftView.imgViewHover.setVisibility(ifNeed? View.VISIBLE:View.GONE);
        }
    }

    private ViewGroup getDecorView() {
        return (ViewGroup) activity.getWindow().getDecorView();
    }

    public void setDebuggable(boolean needDebug) {
        debug = needDebug;
    }

    public static void startSwipeActivity(Activity activity, Class cls) {
        startSwipeActivity(activity, new Intent(activity, cls));
    }

    public static void startSwipeActivity(Activity activity, Intent intent) {
        saveScreenShot(activity);
        intent.putExtra(KEY_HASH, activity.hashCode());
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.keep);
        logD("start swipe activity for hashcode " + activity.hashCode());
    }

    private static final LruCache<String, Bitmap> cachedScreenShot
            = new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 8)) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }
    };

    private static final List<String> activityHashList = new ArrayList<String>();

    private static void removeScreenShot(Context context, int hashCode) {
        removeUnusedScreenShot(context);
        String fileName = getFileName(context, hashCode);
        if (TextUtils.isEmpty(fileName)) {
            logW("file name is null, cannot remove screen shot");
            return;
        }
        cachedScreenShot.remove(fileName);
        activityHashList.remove(String.valueOf(hashCode));
        logD(fileName + " removed from cache");
        File screenShotFile = new File(fileName);
        if (screenShotFile.exists()) {
            screenShotFile.delete();
            logD(fileName + " file removed");
        }
    }

    private static void removeUnusedScreenShot(Context context) {
        String cacheDir = getCacheDir(context);
        for (File file : new File(cacheDir).listFiles()) {
            boolean isUsed = false;
            for (String hash : activityHashList) {
                if (file.getName().contains(hash)) {
                    isUsed = true;
                    break;
                }
            }
            if (!isUsed) {
                logD(file.getName() + " is useless so remove it");
                file.delete();
            }
        }
    }

    private static void saveScreenShot(final Activity activity) {
        final View decorView = activity.getWindow().getDecorView();
        final View rootView = decorView.getRootView();
        rootView.setDrawingCacheEnabled(true);
        rootView.buildDrawingCache();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = rootView.getDrawingCache();
                if (bitmap == null) {
                    logW("get screen shot failed!");
                    rootView.setDrawingCacheEnabled(false);
                    return;
                }
                int hashCode = activity.hashCode();
                String fileName = getFileName(activity);
                try {
                    FileOutputStream out = new FileOutputStream(fileName);
                    Rect frame = new Rect();
                    decorView.getWindowVisibleDisplayFrame(frame);
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    int statusHeight = frame.top;
                    if (statusHeight > 0) {
                        logD("has status bar cut top for  " + statusHeight);
                        bitmap = Bitmap.createBitmap(bitmap, 0, statusHeight, width, height - statusHeight);
                    }
                    cachedScreenShot.put(fileName, bitmap);
                    activityHashList.add(String.valueOf(hashCode));
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    rootView.setDrawingCacheEnabled(false);
                    logD("get and save screen shot ok" + fileName);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static String getCacheDir(Context context) {
        return context.getApplicationContext().getFilesDir().getAbsolutePath();
    }

    private static String getFileName(Context context) {
        return getFileName(context, context.hashCode());
    }

    private static String getFileName(Context context, int hashCode) {
        return getCacheDir(context) + "/swipeback@@" + hashCode + "$$.png";
    }

    private static void logD(String msg) {
        if (debug) {
            Log.d(DEBUG_TAG, msg);
        }
    }

    private static void logW(String msg) {
        if (debug) {
            Log.w(DEBUG_TAG, msg);
        }
    }
}
