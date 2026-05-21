package com.xuanfeng.browser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TabsActivity extends Activity {
    
    private ListView listView;
    private String[] titles;
    private String[] urls;
    private int currentIndex;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 接收
        Intent intent = getIntent();
        titles = intent.getStringArrayExtra("titles");
        urls = intent.getStringArrayExtra("urls");
        currentIndex = intent.getIntExtra("current", 0);
        
        // if没有 创建新
        if (titles == null || titles.length == 0) {
            Intent result = new Intent();
            result.putExtra("action", "new_tab");
            setResult(RESULT_OK, result);
            finish();
            return;
        }
        
        // 布局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFFF5F5F5);
        
        // 标题
        LinearLayout titleBar = new LinearLayout(this);
        titleBar.setOrientation(LinearLayout.HORIZONTAL);
        titleBar.setPadding(20, 20, 20, 20);
        titleBar.setBackgroundColor(0xFFFFFFFF);
        
        TextView titleView = new TextView(this);
        titleView.setText("标签页列表");
        titleView.setTextSize(20);
        titleView.setTextColor(0xFF333333);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        
        Button newTabBtn = new Button(this);
        newTabBtn.setText("+");
        newTabBtn.setTextSize(24);
        newTabBtn.setTextColor(0xFF333333);
        newTabBtn.setBackgroundColor(0x00FFFFFF);
        newTabBtn.setOnClickListener(v -> {
            Intent result = new Intent();
            result.putExtra("action", "new_tab");
            setResult(RESULT_OK, result);
            finish();
        });
        
        titleBar.addView(titleView);
        titleBar.addView(newTabBtn);
        layout.addView(titleBar);
        
        // 列表
        listView = new ListView(this);
        listView.setAdapter(new TabAdapter());
        listView.setDivider(null);
        listView.setPadding(10, 10, 10, 10);
        layout.addView(listView);
        
        setContentView(layout);
    }
    
    private class TabAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return titles != null ? titles.length : 0;
        }
        
        @Override
        public Object getItem(int position) {
            return position;
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 每
            LinearLayout itemLayout = new LinearLayout(TabsActivity.this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(20, 15, 20, 15);
            itemLayout.setBackgroundColor(0xFFFFFFFF);
            itemLayout.setLayoutParams(new ListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
            
            // l边距
            itemLayout.setPadding(20, 15, 20, 15);
            
            // 文本
            LinearLayout textLayout = new LinearLayout(TabsActivity.this);
            textLayout.setOrientation(LinearLayout.VERTICAL);
            textLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            
            TextView titleText = new TextView(TabsActivity.this);
            String title = titles[position];
            titleText.setText(title != null && !title.isEmpty() ? title : "新标签页");
            titleText.setTextSize(16);
            titleText.setTextColor(position == currentIndex ? 0xFF2196F3 : 0xFF333333);
            textLayout.addView(titleText);
            
            TextView urlText = new TextView(TabsActivity.this);
            String url = urls[position];
            urlText.setText(url != null && !url.isEmpty() ? url : "about:blank");
            urlText.setTextSize(12);
            urlText.setTextColor(0xFF666666);
            textLayout.addView(urlText);
            
            itemLayout.addView(textLayout);
            
            // 删除按钮
            Button deleteBtn = new Button(TabsActivity.this);
            deleteBtn.setText("×");
            deleteBtn.setTextSize(20);
            deleteBtn.setTextColor(0xFF999999);
            deleteBtn.setBackgroundColor(0x00FFFFFF);
            deleteBtn.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
            
            final int pos = position;
            deleteBtn.setOnClickListener(v -> {
                // 2
                if (titles.length <= 1) {
                    Intent result = new Intent();
                    result.putExtra("action", "delete_and_new");
                    result.putExtra("position", pos);
                    setResult(RESULT_OK, result);
                    finish();
                } else {
                    Intent result = new Intent();
                    result.putExtra("action", "delete");
                    result.putExtra("position", pos);
                    setResult(RESULT_OK, result);
                    finish();
                }
            });
            
            itemLayout.addView(deleteBtn);
            
            // 切
            itemLayout.setOnClickListener(v -> {
                Intent result = new Intent();
                result.putExtra("action", "switch");
                result.putExtra("position", pos);
                setResult(RESULT_OK, result);
                finish();
            });
            
            return itemLayout;
        }
    }
}