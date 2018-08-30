package com.custom.borderbox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.custom.borderbox.activity.GridViewActivity;
import com.custom.borderbox.activity.ListViewActivity;
import com.custom.borderbox.activity.RecyclerGViewActivity;
import com.custom.borderbox.activity.RecyclerLViewActivity;

/**
 * <pre>
 *     author : panbeixing
 *     time : 2018/8/18
 *     desc :
 *     version : 1.0
 * </pre>
 */

public class MainActivity extends Activity implements View.OnClickListener {
    private BorderView mBorderView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBorderView = new BorderView(this);

        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.button1:
                intent.setClass(this, ListViewActivity.class);
                startActivity(intent);
                break;
            case R.id.button2:
                intent.setClass(this, RecyclerLViewActivity.class);
                startActivity(intent);
                break;
            case R.id.button3:
                intent.setClass(this, RecyclerGViewActivity.class);
                startActivity(intent);
                break;
            case R.id.button4:
                intent.setClass(this, GridViewActivity.class);
                startActivity(intent);
                break;
        }

    }
}
