package com.custom.borderbox.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.custom.borderbox.BorderView;
import com.custom.borderbox.R;

/**
 * <pre>
 *     author : panbeixing
 *     time : 2018/8/30
 *     desc :
 *     version : 1.0
 * </pre>
 */

public class GridViewActivity extends Activity {
    private GridView mGridView;
    private GAdapter mAdapter;
    private BorderView borderView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGridView = new GridView(this);
        mGridView.setFocusable(false);
//        mGridView.setHorizontalSpacing(5); // 设置列表项水平间距
//        mGridView.setStretchMode(GridView.NO_STRETCH);
        mGridView.setNumColumns(4);
        mGridView.setPadding(20, 20, 20, 20);
        setContentView(mGridView);
        mAdapter = new GAdapter(this);
        mGridView.setAdapter(mAdapter);
        borderView = new BorderView(this);
    }

    private class GAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        public GAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return 100;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_view, parent, false); //加载布局
                holder = new ViewHolder();

                holder.button = (Button) convertView.findViewById(R.id.item_button);
                holder.button.requestFocus();
                convertView.setTag(holder);
            } else {   //else里面说明，convertView已经被复用了，说明convertView中已经设置过tag了，即holder
                holder = (ViewHolder) convertView.getTag();
            }

            holder.button.setText(position + "");
            holder.button.setId(position);
            return convertView;
        }

        //这个ViewHolder只能服务于当前这个特定的adapter，因为ViewHolder里会指定item的控件，不同的ListView，item可能不同，所以ViewHolder写成一个私有的类
        private class ViewHolder {
            Button button;
        }
    }
}
