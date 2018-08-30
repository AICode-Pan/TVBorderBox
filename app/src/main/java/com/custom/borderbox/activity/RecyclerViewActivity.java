package com.custom.borderbox.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.custom.borderbox.BorderView;
import com.custom.borderbox.R;

/**
 * <pre>
 *     author : panbeixing
 *     time : 2018/8/29
 *     desc :
 *     version : 1.0
 * </pre>
 */

public class RecyclerViewActivity extends Activity {
    private RecyclerView recyclerView;
    private RAdapter mAdapter;
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recyclerView = new RecyclerView(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        setContentView(recyclerView);
        mAdapter = new RAdapter(this);
        recyclerView.setAdapter(mAdapter);
        BorderView borderView = new BorderView(this);
    }

    private class RAdapter extends RecyclerView.Adapter {
        private LayoutInflater mInflater;
        public RAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ItemHolder(mInflater.inflate(R.layout.item_view, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ItemHolder itemHolder = (ItemHolder) holder;
//            itemHolder.button.setText(position + "");
        }

        @Override
        public int getItemCount() {
            return 100;
        }

        private class ItemHolder extends RecyclerView.ViewHolder {
            private Button button;
            public ItemHolder(View itemView) {
                super(itemView);
                button = itemView.findViewById(R.id.button1);
            }
        }
    }
}
