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
import android.view.View;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Typeface;

public class SettingsActivity extends Activity {
    private SharedPreferences prefs;
    private AlertDialog historyDialog;

    // 主题色（与 colors.xml 定义一致）
    private static final int COLOR_PRIMARY = 0xFF1565C0;
    private static final int COLOR_BACKGROUND = 0xFFF5F5F5;
    private static final int COLOR_SURFACE = 0xFFFFFFFF;
    private static final int COLOR_TEXT_PRIMARY = 0xFF212121;
    private static final int COLOR_TEXT_SECONDARY = 0xFF757575;
    private static final int COLOR_DIVIDER = 0xFFE0E0E0;
    private static final int COLOR_PRIMARY_LIGHT_BG = 0xFFE3F2FD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(COLOR_BACKGROUND);
        scrollView.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 32);

        // ====== 标题栏 ======
        root.addView(createTitleBar());

        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        // ====== 浏览设置 ======
        LinearLayout browseCard = createCard("浏览设置");
        addPickerItem(browseCard, "搜索引擎", "",
                "search_engine", "baidu",
                new String[]{"百度", "Google", "Bing", "DuckDuckGo", "Ahmia", "搜狗", "360",
                        "Yandex", "Qwant", "Ecosia", "Startpage", "Brave", "Yahoo", "Naver"},
                new String[]{"baidu", "google", "bing", "duckduckgo", "ahmia", "sogou", "360",
                        "yandex", "qwant", "ecosia", "startpage", "brave", "yahoo", "naver"});
        addDivider(browseCard);
        addPickerItem(browseCard, "网址栏模式", "需重启生效",
                "url_mode", "always",
                new String[]{"始终显示网址", "选中时显示网址"},
                new String[]{"always", "on_focus"});
        root.addView(browseCard);

        // ====== 界面设置 ======
        LinearLayout uiCard = createCard("界面设置");
        addSwitchItem(uiCard, "隐藏标题", "隐藏主界面顶部的标题", "hide_title", false);
        addDivider(uiCard);
        addSwitchItem(uiCard, "选中网址栏时全选", "点击网址栏自动全选文字", "select_all_on_focus", true);
        root.addView(uiCard);

        // ====== 数据管理 ======
        LinearLayout dataCard = createCard("数据管理");
        dataCard.addView(createClickableRow("历史记录管理", "查看和管理浏览历史", v -> showHistoryDialog()));
        root.addView(dataCard);

        // ====== 关于 ======
        LinearLayout aboutCard = createCard("关于");
        aboutCard.addView(createInfoRow("版本", "XFbrowser v4.9.x"));
        addDivider(aboutCard);
        aboutCard.addView(createInfoRow("开发者", "xuanfeng0316"));
        root.addView(aboutCard);

        // 底部留白
        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 40));
        root.addView(spacer);

        scrollView.addView(root);
        setContentView(scrollView);
    }

    // ==================== 标题栏 ====================

    private LinearLayout createTitleBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setPadding(8, 16, 8, 24);
        bar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // 返回按钮 —— 使用 ImageView + colorFilter
        ImageView backBtn = new ImageView(this);
        backBtn.setImageResource(R.drawable.ic_arrow_back);
        backBtn.setColorFilter(COLOR_PRIMARY);
        backBtn.setPadding(0, 0, 16, 0);
        backBtn.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
        backBtn.setOnClickListener(v -> finish());
        bar.addView(backBtn);

        TextView title = new TextView(this);
        title.setText("设置");
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(COLOR_TEXT_PRIMARY);
        bar.addView(title);

        return bar;
    }

    // ==================== 卡片容器 ====================

    private LinearLayout createCard(String titleText) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 20, 20, 16);
        card.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // 让 card 之间有间距
        ((LinearLayout.LayoutParams) card.getLayoutParams()).bottomMargin = 16;

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setColor(COLOR_SURFACE);
        bg.setCornerRadius(16);
        bg.setStroke(1, 0x12000000);
        card.setBackground(bg);

        // 卡片标题
        TextView cardTitle = new TextView(this);
        cardTitle.setText(titleText);
        cardTitle.setTextSize(13);
        cardTitle.setTextColor(COLOR_PRIMARY);
        cardTitle.setTypeface(null, Typeface.BOLD);
        cardTitle.setPadding(0, 0, 0, 12);
        card.addView(cardTitle);

        return card;
    }

    // ==================== 分割线 ====================

    private void addDivider(LinearLayout parent) {
        View divider = new View(this);
        divider.setBackgroundColor(COLOR_DIVIDER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1);
        params.topMargin = 4;
        params.bottomMargin = 4;
        divider.setLayoutParams(params);
        parent.addView(divider);
    }

    // ==================== 可点击行（历史记录入口） ====================

    private LinearLayout createClickableRow(String label, String explain, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 16, 0, 16);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(listener);

        // 文字区域
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(16);
        labelView.setTextColor(COLOR_TEXT_PRIMARY);
        textLayout.addView(labelView);

        if (explain != null && !explain.isEmpty()) {
            TextView explainView = new TextView(this);
            explainView.setText(explain);
            explainView.setTextSize(12);
            explainView.setTextColor(COLOR_TEXT_SECONDARY);
            explainView.setPadding(0, 2, 0, 0);
            textLayout.addView(explainView);
        }

        row.addView(textLayout);

        // 右箭头图标
        ImageView arrowView = new ImageView(this);
        arrowView.setImageResource(R.drawable.ic_forward);
        arrowView.setColorFilter(0xFFBDBDBD);
        arrowView.setLayoutParams(new LinearLayout.LayoutParams(24, 24));
        arrowView.setScaleType(ImageView.ScaleType.CENTER);
        row.addView(arrowView);

        return row;
    }

    // ==================== 信息行（关于） ====================

    private LinearLayout createInfoRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 14, 0, 14);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(15);
        labelView.setTextColor(COLOR_TEXT_PRIMARY);
        labelView.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(labelView);

        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextSize(15);
        valueView.setTextColor(COLOR_TEXT_SECONDARY);
        valueView.setGravity(android.view.Gravity.END);
        row.addView(valueView);

        return row;
    }

    // ==================== 历史记录对话框 ====================

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

        ListView listView = new ListView(this);
        listView.setDivider(null);
        listView.setPadding(0, 8, 0, 8);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, historyList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(COLOR_TEXT_PRIMARY);
                textView.setTextSize(14);
                textView.setPadding(24, 16, 24, 16);
                // 圆角浅色背景
                GradientDrawable gd = new GradientDrawable();
                gd.setShape(GradientDrawable.RECTANGLE);
                gd.setColor(0xFFF5F5F5);
                gd.setCornerRadius(10);
                textView.setBackground(gd);
                textView.setPadding(24, 16, 24, 16);
                return textView;
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

        // 底部按钮栏
        LinearLayout buttonBar = new LinearLayout(this);
        buttonBar.setOrientation(LinearLayout.HORIZONTAL);
        buttonBar.setPadding(0, 20, 0, 0);

        ImageButton clearAllBtn = new ImageButton(this);
        clearAllBtn.setImageResource(R.drawable.ic_close);
        clearAllBtn.setScaleType(ImageView.ScaleType.CENTER);
        clearAllBtn.setBackgroundColor(0xFFE53935);
        clearAllBtn.setColorFilter(0xFFFFFFFF);
        clearAllBtn.setLayoutParams(new LinearLayout.LayoutParams(0, 50, 1));
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

        View btnSpacer = new View(this);
        btnSpacer.setLayoutParams(new LinearLayout.LayoutParams(16, 1));
        buttonBar.addView(btnSpacer);

        ImageButton closeBtn = new ImageButton(this);
        closeBtn.setImageResource(R.drawable.ic_back);
        closeBtn.setScaleType(ImageView.ScaleType.CENTER);
        closeBtn.setBackgroundColor(COLOR_PRIMARY_LIGHT_BG);
        closeBtn.setColorFilter(COLOR_PRIMARY);
        closeBtn.setLayoutParams(new LinearLayout.LayoutParams(0, 50, 1));
        closeBtn.setOnClickListener(v -> {
            if (historyDialog != null) {
                historyDialog.dismiss();
            }
        });
        buttonBar.addView(closeBtn);

        layout.addView(buttonBar);

        // ★ BUG FIX: 赋值给成员变量，而非声明新局部变量 ★
        historyDialog = new AlertDialog.Builder(this)
                .setTitle("历史记录")
                .setView(layout)
                .create();

        historyDialog.show();
    }

    // ==================== 开关类设置项 ====================

    private void addSwitchItem(LinearLayout layout, String label, String explain, String key, boolean defaultValue) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(0, 16, 0, 16);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(16);
        labelView.setTextColor(COLOR_TEXT_PRIMARY);
        textLayout.addView(labelView);

        if (explain != null && !explain.isEmpty()) {
            TextView explainView = new TextView(this);
            explainView.setText(explain);
            explainView.setTextSize(12);
            explainView.setTextColor(COLOR_TEXT_SECONDARY);
            explainView.setPadding(0, 2, 0, 0);
            textLayout.addView(explainView);
        }

        LinearLayout switchContainer = new LinearLayout(this);
        switchContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        switchContainer.setGravity(android.view.Gravity.CENTER_VERTICAL);
        switchContainer.setPadding(16, 0, 0, 0);

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

    // ==================== 选择类设置项 ====================

    private void addPickerItem(LinearLayout layout, String label, String explain,
                               String key, String defaultValue,
                               String[] options, String[] values) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(0, 16, 0, 16);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(16);
        labelView.setTextColor(COLOR_TEXT_PRIMARY);
        textLayout.addView(labelView);

        if (explain != null && !explain.isEmpty()) {
            TextView explainView = new TextView(this);
            explainView.setText(explain);
            explainView.setTextSize(12);
            explainView.setTextColor(COLOR_TEXT_SECONDARY);
            explainView.setPadding(0, 2, 0, 0);
            textLayout.addView(explainView);
        }

        LinearLayout valueContainer = new LinearLayout(this);
        valueContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        valueContainer.setGravity(android.view.Gravity.CENTER_VERTICAL);
        valueContainer.setPadding(16, 0, 0, 0);

        final TextView valueView = new TextView(this);
        String current = prefs.getString(key, defaultValue);
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) {
                valueView.setText(options[i]);
                break;
            }
        }
        valueView.setTextSize(14);
        valueView.setTextColor(COLOR_PRIMARY);
        valueView.setTypeface(null, Typeface.BOLD);
        valueView.setPadding(12, 6, 12, 6);
        valueView.setClickable(true);
        valueView.setFocusable(true);

        // 圆角蓝色浅背景
        GradientDrawable valueBg = new GradientDrawable();
        valueBg.setShape(GradientDrawable.RECTANGLE);
        valueBg.setColor(COLOR_PRIMARY_LIGHT_BG);
        valueBg.setCornerRadius(8);
        valueView.setBackground(valueBg);

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
