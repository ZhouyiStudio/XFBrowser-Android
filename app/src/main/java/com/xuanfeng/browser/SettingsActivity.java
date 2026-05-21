package com.xuanfeng.browser;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.*;
import android.content.SharedPreferences;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import java.util.List;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import java.util.List;
import android.view.View;

public class SettingsActivity extends Activity {
    private SharedPreferences prefs;
    private AlertDialog historyDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);
        
        // 标题栏
        LinearLayout titleBar = new LinearLayout(this);
        titleBar.setOrientation(LinearLayout.HORIZONTAL);
        titleBar.setPadding(0, 0, 0, 30);

        TextView backBtn = new TextView(this);
        backBtn.setText("<");
        backBtn.setTextSize(28);
        backBtn.setTextColor(0xFF2196F3);
        backBtn.setPadding(0, 0, 20, 0);
        backBtn.setOnClickListener(v -> finish());
        titleBar.addView(backBtn);

        TextView title = new TextView(this);
        title.setText("设置");
        title.setTextSize(24);
        titleBar.addView(title);
        layout.addView(titleBar);
        
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        
        // 搜索引擎
        String[] engines = {"百度", "Google", "Bing", "DuckDuckGo", "Ahmia", "搜狗", "360", "Yandex", "Qwant", "Ecosia", "Startpage", "Brave", "Yahoo", "Naver"};
        String[] engineValues = {"baidu", "google", "bing", "duckduckgo", "ahmia", "sogou", "360", "yandex", "qwant", "ecosia", "startpage", "brave", "yahoo", "naver"};
        addPickerItem(layout, "搜索引擎", "", "search_engine", "baidu", engines, engineValues);
        
        // 网址栏模式
        String[] urlModes = {"始终显示网址", "选中时显示网址"};
        String[] urlModeValues = {"always", "on_focus"};
        addPickerItem(layout, "网址栏模式", "需重启生效", "url_mode", "always", urlModes, urlModeValues);
        
        // 隐藏标题
        addSwitchItem(layout, "隐藏标题", "隐藏主界面顶部的标题", "hide_title", false);
        
        // 选中网址栏时全选
        addSwitchItem(layout, "选中网址栏时全选", "点击网址栏自动全选文字", "select_all_on_focus", true);
        
        // 历史记录管理入口
TextView historyManagerBtn = new TextView(this);
historyManagerBtn.setText("历史记录管理=>");
historyManagerBtn.setTextSize(16);
historyManagerBtn.setTextColor(0xFF2196F3);
historyManagerBtn.setPadding(0, 15, 0, 15);
historyManagerBtn.setOnClickListener(v -> showHistoryDialog());
layout.addView(historyManagerBtn);
        
        // 版本信息
        TextView version = new TextView(this);
        version.setText("XFbrowser v4.9.～");
        version.setTextSize(16);
        version.setPadding(0, 20, 0, 20);
        layout.addView(version);
        
        TextView extraText = new TextView(this);
        extraText.setText("Solo dev: xuanfeng0316");
        extraText.setTextSize(14);
        extraText.setTextColor(0xFF666666);
        extraText.setPadding(0, 10, 0, 10);
        layout.addView(extraText);
        
        setContentView(layout);
    }
    
    
    
    private void showHistoryDialog() {
    List<String> historyList = HistoryManager.getInstance(this).getHistoryList();
    
    if (historyList == null || historyList.isEmpty()) {
        new AlertDialog.Builder(this)
            .setTitle("历史记录")
            .setMessage("暂无历史记录")
            .setPositiveButton("确定", null)
            .show();
        return;
    }
    
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(20, 20, 20, 20);
    
    final ListView listView = new ListView(this);
    listView.setBackgroundColor(0xFFFFFFFF);
    
    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, historyList) {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view;
            textView.setTextColor(0xFF000000);
            textView.setBackgroundColor(0xFFFFFFFF);
            textView.setPadding(50, 20, 20, 20);
            return view;
        }
    };
    listView.setAdapter(adapter);
    
    listView.setOnItemClickListener((parent, view, position, id) -> {
        String url = historyList.get(position);
        new AlertDialog.Builder(this)
            .setTitle("删除记录")
            .setMessage("删除 " + url + " ?")
            .setPositiveButton("删除", (dialog, which) -> {
                HistoryManager.getInstance(this).removeHistory(position);
                historyList.remove(position);
                adapter.notifyDataSetChanged();
                if (adapter.getCount() == 0 && historyDialog != null) {
                    historyDialog.dismiss();
                    Toast.makeText(this, "历史记录已清空", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("取消", null)
            .show();
    });
    
    layout.addView(listView, new LinearLayout.LayoutParams(-1, 0, 1));
    
    LinearLayout buttonBar = new LinearLayout(this);
    buttonBar.setOrientation(LinearLayout.HORIZONTAL);
    buttonBar.setPadding(0, 20, 0, 0);
    
    Button clearAllBtn = new Button(this);
    clearAllBtn.setText("全部删除");
    clearAllBtn.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
    clearAllBtn.setOnClickListener(v -> {
        new AlertDialog.Builder(this)
            .setTitle("全部删除")
            .setMessage("确定删除所有历史记录吗？")
            .setPositiveButton("删除", (dialog, which) -> {
                HistoryManager.getInstance(this).clearHistory();
                adapter.clear();
                adapter.notifyDataSetChanged();
                if (historyDialog != null) {
                    historyDialog.dismiss();
                }
                Toast.makeText(this, "历史记录已清空", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    });
    buttonBar.addView(clearAllBtn);
    
    Button closeBtn = new Button(this);
    closeBtn.setText("关闭");
    closeBtn.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
    closeBtn.setOnClickListener(v -> {
        if (historyDialog != null) {
            historyDialog.dismiss();
        }
    });
    buttonBar.addView(closeBtn);
    
    layout.addView(buttonBar);
    
    final AlertDialog historyDialog = new AlertDialog.Builder(this)
        .setTitle("历史记录")
        .setView(layout)
        .create();
    
    historyDialog.show();
}


    
    
    
    
    
    // 开关类设置项
    private void addSwitchItem(LinearLayout layout, String label, String explain, String key, boolean defaultValue) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(0, 15, 0, 15);
        container.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.7f));

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(16);
        labelView.setTextColor(0xFF333333);
        textLayout.addView(labelView);

        TextView explainView = new TextView(this);
        explainView.setText(explain);
        explainView.setTextSize(12);
        explainView.setTextColor(0xFF666666);
        textLayout.addView(explainView);

        LinearLayout switchContainer = new LinearLayout(this);
        switchContainer.setLayoutParams(new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.MATCH_PARENT, 0.3f));
        switchContainer.setGravity(android.view.Gravity.CENTER);

        Switch switchView = new Switch(new ContextThemeWrapper(this, R.style.XFSwitch));
        switchView.setChecked(prefs.getBoolean(key, defaultValue));
        switchContainer.addView(switchView);

        container.addView(textLayout);
        container.addView(switchContainer);
        layout.addView(container);

        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(key, isChecked).apply();
            Intent intent = new Intent("com.xuanfeng.browser." + key.toUpperCase() + "_CHANGED");
            sendBroadcast(intent);
        });
    }
    
    // 选择类设置项
    private void addPickerItem(LinearLayout layout, String label, String explain, String key, String defaultValue, String[] options, String[] values) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(0, 15, 0, 15);
        container.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.7f));

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(16);
        labelView.setTextColor(0xFF333333);
        textLayout.addView(labelView);

        if (explain != null && !explain.isEmpty()) {
            TextView explainView = new TextView(this);
            explainView.setText(explain);
            explainView.setTextSize(12);
            explainView.setTextColor(0xFF666666);
            textLayout.addView(explainView);
        }

        LinearLayout valueContainer = new LinearLayout(this);
        valueContainer.setLayoutParams(new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.MATCH_PARENT, 0.3f));
        valueContainer.setGravity(android.view.Gravity.CENTER_VERTICAL);

        final TextView valueView = new TextView(this);
        String current = prefs.getString(key, defaultValue);
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) {
                valueView.setText(options[i]);
                break;
            }
        }
        valueView.setTextSize(14);
        valueView.setTextColor(0xFF2196F3);
        valueView.setPadding(10, 0, 0, 0);
        valueView.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle(label)
                .setItems(options, (dialog, which) -> {
                    prefs.edit().putString(key, values[which]).apply();
                    valueView.setText(options[which]);
                    Intent intent = new Intent("com.xuanfeng.browser." + key.toUpperCase() + "_CHANGED");
                    sendBroadcast(intent);
                })
                .show();
        });
        valueContainer.addView(valueView);

        container.addView(textLayout);
        container.addView(valueContainer);
        layout.addView(container);
    }
}