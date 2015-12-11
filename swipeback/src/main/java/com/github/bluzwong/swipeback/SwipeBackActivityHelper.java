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
import java.util.Arrays;
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

    public interface PanelSlideListener {
        void onPanelSlide(View view, float v);
    }
    private PanelSlideListener panelSlideListener;

    public SwipeBackActivityHelper setPanelSlideListener(PanelSlideListener panelSlideListener) {
        this.panelSlideListener = panelSlideListener;
        return this;
    }


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
                    if (panelSlideListener != null) {
                        panelSlideListener.onPanelSlide(view, v);
                    }
                    float x = view.getX() - leftView.getWidth();
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

    public SwipeBackActivityHelper setEdgeMode(boolean edgeMode) {
        isEdgeMode = edgeMode;
        if (swipeBackView != null) {
            swipeBackView.isEdgeMode = isEdgeMode;
        }
        return this;
    }

    private boolean isParallax = false;

    public SwipeBackActivityHelper setParallaxMode(boolean isParallax) {
        this.isParallax = isParallax;
        return this;
    }

    private int parallaxRatio = 2;

    public SwipeBackActivityHelper setParallaxRatio(int ratio) {
        parallaxRatio = Math.max(1, Math.min(ratio, Integer.MAX_VALUE));
        return this;
    }

    private boolean needBackgroundShadow = false;

    public SwipeBackActivityHelper setNeedBackgroundShadow(boolean ifNeed) {
        needBackgroundShadow = ifNeed;
        if (leftView != null) {
            leftView.imgViewHover.setVisibility(ifNeed ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    private ViewGroup getDecorView() {
        return (ViewGroup) activity.getWindow().getDecorView();
    }

    public SwipeBackActivityHelper setDebuggable(boolean needDebug) {
        debug = needDebug;
        return this;
    }

    public static void startSwipeActivity(Activity activity, Class cls) {
        startSwipeActivity(activity, new Intent(activity, cls));
    }

    public static void startSwipeActivity(Activity activity, Intent intent) {
        startSwipeActivity(activity, intent, false, false, false);
    }

    public static void startSwipeActivity(Activity activity, Class cls, boolean needParallax, boolean needBackgroundShadow, boolean fitSystemWindow) {
        startSwipeActivity(activity, new Intent(activity, cls), needParallax, needBackgroundShadow, fitSystemWindow);
    }

    public static StartSwipeActivityBuilder activityBuilder(Activity activity) {
        return new StartSwipeActivityBuilder(activity);
    }

    public static class StartSwipeActivityBuilder {
        private Activity activity;
        private boolean needParallax = false;
        private boolean needBackgroundShadow = false;
        private boolean fitSystemWindow = false;
        private Intent intent;
        private Class cls;
        private List<View> fixViews = new ArrayList<>();

        public StartSwipeActivityBuilder(Activity activity) {
            this.activity = activity;
        }

        public StartSwipeActivityBuilder needParallax(boolean needParallax) {
            this.needParallax = needParallax;
            return this;
        }

        public StartSwipeActivityBuilder needBackgroundShadow(boolean needBackgroundShadow) {
            this.needBackgroundShadow = needBackgroundShadow;
            return this;
        }

        public StartSwipeActivityBuilder fitSystemWindow(boolean fitSystemWindow) {
            this.fitSystemWindow = fitSystemWindow;
            return this;
        }

        public StartSwipeActivityBuilder intent(Intent intent) {
            this.intent = intent;
            return this;
        }

        public StartSwipeActivityBuilder cls(Class cls) {
            this.cls = cls;
            return this;
        }

        public StartSwipeActivityBuilder prepareView(View view) {
            if (view != null) {
                this.fixViews.add(view);
            }
            return this;
        }

        public StartSwipeActivityBuilder prepareViews(View[] views) {
            if (views != null) {
                this.fixViews.addAll(Arrays.asList(views));
            }
            return this;
        }

        public void startActivity() {
            if (intent == null && cls == null) {
                logW("intent or activityClass must be settled");
                return;
            }
            for (final View fixView : fixViews) {
                fixView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fixView.layout(0, 0, fixView.getWidth(), fixView.getHeight());
                        if (fixView.isDrawingCacheEnabled()) {
                            fixView.destroyDrawingCache();
                        }
                    }
                }, 1);
            }
            if (intent != null) {
                startSwipeActivity(activity, intent, needParallax, needBackgroundShadow, fitSystemWindow);
            } else {
                startSwipeActivity(activity, cls, needParallax, needBackgroundShadow, fitSystemWindow);
            }
        }

        // support for other startActivity
        public Intent prepareStartActivity() {
            if (intent == null && cls == null) {
                logW("intent or activityClass must be settled");
                return null;
            }
            saveScreenShot(activity, fitSystemWindow);
            if (intent == null) {
                intent = new Intent(activity, cls);
            }
            intent.putExtra(KEY_HASH, activity.hashCode());
            return intent;
        }

        // support for other startActivity
        public void startActivityBy(IStartActivity iStartActivity) {
            iStartActivity.startYourActivityHere(prepareStartActivity());
            activity.overridePendingTransition(R.anim.slide_in_right, getAnim(needParallax, needBackgroundShadow));
        }
    }

    // support for other startActivity
    public interface IStartActivity {
        void startYourActivityHere(Intent intent);
    }

    private static int getAnim(boolean needParallax, boolean needBackgroundShadow) {
        if (needParallax && needBackgroundShadow) {
            logD("needParallax && needBackgroundShadow");
            return R.anim.slide_out_center_to_left_shadow_30;
        }
        if (needParallax) {
            logD("needParallax do not need needBackgroundShadow");
            return R.anim.slide_out_center_to_left_30;
        }
        if (needBackgroundShadow) {
            logD("do not needParallax and need needBackgroundShadow");
            return R.anim.keep_shadow;
        }

        logD("do not need any animation");
        return R.anim.keep;
    }

    public static void startSwipeActivity(Activity activity, Intent intent, boolean needParallax, boolean needBackgroundShadow, boolean fitSystemWindow) {
        startSwipeActivity(activity, intent, getAnim(needParallax, needBackgroundShadow), fitSystemWindow);
    }

    public static void startSwipeActivity(Activity activity, Intent intent, int animResId, boolean fitSystemWindow) {
        saveScreenShot(activity, fitSystemWindow);
        intent.putExtra(KEY_HASH, activity.hashCode());
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_right, animResId);
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

    private static void saveScreenShot(final Activity activity, final boolean fitSystemWindow) {
        final View decorView = activity.getWindow().getDecorView();
        final View rootView = decorView.getRootView();
        // zhege zhenshi yeluzi, bu zheyang recyclerview jiu hui cuo
        rootView.setDrawingCacheEnabled(true);
        final Bitmap bitmap = rootView.getDrawingCache();
        new Thread(new Runnable() {
            @Override
            public void run() {

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
                    int navHeight = height - frame.bottom;
                    Bitmap fixedBitmap;

                    logD("has status bar cut top for  " + statusHeight);
                    if (fitSystemWindow) {
                        statusHeight = 0;
                    }
                    fixedBitmap = Bitmap.createBitmap(bitmap, 0, statusHeight, width, height - statusHeight - navHeight);

                    cachedScreenShot.put(fileName, fixedBitmap);
                    activityHashList.add(String.valueOf(hashCode));
                    fixedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
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
        File folder = new File(getCacheDir(context) + "/swipeback_cache");
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder.getAbsolutePath() + "/swipeback@@" + hashCode + "$$.png";
        //return Environment.getExternalStorageDirectory() +"/swipebackcache" + "/swipeback@@" + hashCode + "$$.png";
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
