package com.github.bluzwong.swipeback.example;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.github.bluzwong.swipeback.SwipeBackActivityHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static boolean needParallax = false;
    private static boolean needShadow = false;
    private static boolean needEdge = false;
    int num = 0;
    /// 0. get helper instance
    SwipeBackActivityHelper helper = new SwipeBackActivityHelper();
    CheckBox parallaxBox, shadowBox, edgeBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        num = getIntent().getIntExtra("num", 0);
        initViews();
        /// 1. init with activity
        helper.setDebuggable(true)
                .setEdgeMode(needEdge)
                .setParallaxMode(needParallax)
                .setParallaxRatio(3)
                .setNeedBackgroundShadow(needShadow)
                .setPanelSlideListener(new SwipeBackActivityHelper.PanelSlideListener() {
                    @Override
                    public void onPanelSlide(View view, float v) {
                        Log.i("swipeback-log", "panel slide : " + v);
                    }
                })
                .init(this);
    }

    private void initViews() {
        initCheckbox();
        findViewById(R.id.background).setBackgroundColor(Color.parseColor(getRandColorCode()));
        ((TextView) findViewById(R.id.tv)).setText(String.valueOf(num));
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.putExtra("num", num + 1);
                /// 2. use static method to start the activity that needs swipe back feature
                SwipeBackActivityHelper.activityBuilder(MainActivity.this)
                        .intent(intent)
                        .needParallax(true)
                        .needBackgroundShadow(true)
                     // .fitSystemWindow(true) // status bar height
                     // .prepareView(swipeRefreshLayout)
                     // see http://stackoverflow.com/questions/29356607/android-swiperefreshlayout-cause-recyclerview-not-update-when-take-screenshot
                        .startActivity();

                /* or use this method to startActivityForResult
                .startActivityBy(new SwipeBackActivityHelper.IStartActivity() {
                    @Override
                    public void startYourActivityHere(Intent intent) {
                        startActivityForResult(intent, 0);
                    }
                });
                */
            }
        });

        // setup view pager and adapter
        List<View> views = new ArrayList<View>();
        LayoutInflater inflater = LayoutInflater.from(this);
        views.add(inflater.inflate(R.layout.layout1, null));
        views.add(inflater.inflate(R.layout.layout2, null));
        views.add(inflater.inflate(R.layout.layout3, null));
        VpAdapter adapter = new VpAdapter(views);
        ViewPager viewPager = (ViewPager) findViewById(R.id.vp);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                /// 4. when some views conflict with swipe back , you should do these, for example:
                if (position != 0) {
                    /// if the current view page is not the first, make 'viewPager' receive touch event.
                    // helper.disableSwipeBack();

                    /// or enable edge mode
                    helper.setEdgeMode(true);
                } else {
                    /// the current page return to the first one, make 'swipe back' receive touch event.
                    helper.enableSwipeBack();

                    /// or disable edge mode
                    helper.setEdgeMode(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        /// 3. use helper.finish() to make finish animation or finish it normally
        helper.finish();
    }

    private void initCheckbox() {
        parallaxBox = (CheckBox) findViewById(R.id.parallax);
        parallaxBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                needParallax = isChecked;
                helper.setParallaxMode(isChecked);
            }
        });
        shadowBox = (CheckBox) findViewById(R.id.shadow);
        shadowBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                needShadow = isChecked;
                helper.setNeedBackgroundShadow(isChecked);
            }
        });

        edgeBox = (CheckBox) findViewById(R.id.edge);
        edgeBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                needEdge = isChecked;
                helper.setEdgeMode(isChecked);
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        parallaxBox.setChecked(needParallax);
        shadowBox.setChecked(needShadow);
        edgeBox.setChecked(needEdge);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("swipeback", "onActivityResult => " + requestCode);
    }

    // just for random background color
    public static String getRandColorCode() {
        String r, g, b;
        Random random = new Random();
        r = Integer.toHexString(random.nextInt(256)).toUpperCase();
        g = Integer.toHexString(random.nextInt(256)).toUpperCase();
        b = Integer.toHexString(random.nextInt(256)).toUpperCase();

        r = r.length() == 1 ? "0" + r : r;
        g = g.length() == 1 ? "0" + g : g;
        b = b.length() == 1 ? "0" + b : b;

        return "#" + r + g + b;
    }
}
