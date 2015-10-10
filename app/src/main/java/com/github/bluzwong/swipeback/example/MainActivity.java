package com.github.bluzwong.swipeback.example;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.github.bluzwong.swipeback.SwipeBackActivityHelper;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    int num = 0;
    SwipeBackActivityHelper helper = new SwipeBackActivityHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        num = getIntent().getIntExtra("num", 0);
        initViews();
        helper.setDebuggable(true);
        helper.init(this);
    }

    private void initViews() {
        findViewById(R.id.background).setBackgroundColor(Color.parseColor(getRandColorCode()));
        ((TextView) findViewById(R.id.tv)).setText(String.valueOf(num));
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.putExtra("num", num + 1);
                SwipeBackActivityHelper.startSwipeActivity(MainActivity.this, intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        helper.finish();
    }

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
