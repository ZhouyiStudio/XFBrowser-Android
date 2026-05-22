package com.xuanfeng.browser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ToolboxActivity extends Activity {

    private static final int COLOR_PRIMARY = 0xFF1565C0;
    private static final int COLOR_BACKGROUND = 0xFFF5F5F5;
    private static final int COLOR_SURFACE = 0xFFFFFFFF;
    private static final int COLOR_TEXT_PRIMARY = 0xFF212121;
    private static final int COLOR_TEXT_SECONDARY = 0xFF757575;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(COLOR_BACKGROUND);
        root.setPadding(24, 24, 24, 32);

        // 标题栏
        root.addView(createTitleBar());

        // ====== 一般工具 ======
        LinearLayout toolCard = createCard("一般工具");
        toolCard.addView(createToolItem("编辑网页源码", "ic_source", v -> {
            // 发送广播让 MainActivity 处理
            sendToolAction("EDIT_SOURCE");
        }));
        addDivider(toolCard);
        toolCard.addView(createToolItem("页内查找", "ic_find_in_page", v -> {
            sendToolAction("FIND_IN_PAGE");
        }));
        addDivider(toolCard);
        toolCard.addView(createToolItem("爬取", "ic_crawl", v -> {
            sendToolAction("CRAWL");
        }));
        addDivider(toolCard);
        toolCard.addView(createToolItem("下载管理", "ic_download_mgr", v -> {
            Intent intent = new Intent(ToolboxActivity.this, DownloadActivity.class);
            startActivity(intent);
        }));
        addDivider(toolCard);
        toolCard.addView(createToolItem("PingHub", "ic_ping", v -> {
            Intent intent = new Intent(ToolboxActivity.this, ProtocolConnectActivity.class);
            startActivity(intent);
        }));
        root.addView(toolCard);

        // ====== 浏览模式 ======
        LinearLayout modeCard = createCard("浏览模式");
        modeCard.addView(createToggleItem("自动刷新", "auto_refresh", true));
        addDivider(modeCard);
        modeCard.addView(createToggleItem("阻止音视频自动播放", "block_autoplay", false));
        addDivider(modeCard);
        modeCard.addView(createToggleItem("文本模式", "text_mode", false));
        addDivider(modeCard);
        modeCard.addView(createToggleItem("禁用JavaScript", "js_disabled", false));
        addDivider(modeCard);
        modeCard.addView(createToggleItem("阅读模式", "reading_mode", false));
        root.addView(modeCard);

        // 底部留白
        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 40));
        root.addView(spacer);

        // 放在 ScrollView 中
        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        scrollView.setBackgroundColor(COLOR_BACKGROUND);
        scrollView.setFillViewport(true);
        scrollView.addView(root);

        setContentView(scrollView);
    }

    private void sendToolAction(String action) {
        Intent intent = new Intent("com.xuanfeng.browser.TOOLBOX_ACTION");
        intent.putExtra("action", action);
        sendBroadcast(intent);
        finish();
    }

    private LinearLayout createTitleBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setPadding(8, 16, 8, 24);
        bar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        ImageView backBtn = new ImageView(this);
        backBtn.setImageResource(R.drawable.ic_arrow_back);
        backBtn.setColorFilter(COLOR_PRIMARY);
        backBtn.setPadding(0, 0, 16, 0);
        backBtn.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
        backBtn.setOnClickListener(v -> finish());
        bar.addView(backBtn);

        TextView title = new TextView(this);
        title.setText("工具箱");
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(COLOR_TEXT_PRIMARY);
        bar.addView(title);

        return bar;
    }

    private LinearLayout createCard(String titleText) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 20, 20, 16);
        card.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        ((LinearLayout.LayoutParams) card.getLayoutParams()).bottomMargin = 16;

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setColor(COLOR_SURFACE);
        bg.setCornerRadius(16);
        bg.setStroke(1, 0x12000000);
        card.setBackground(bg);

        TextView cardTitle = new TextView(this);
        cardTitle.setText(titleText);
        cardTitle.setTextSize(13);
        cardTitle.setTextColor(COLOR_PRIMARY);
        cardTitle.setTypeface(null, Typeface.BOLD);
        cardTitle.setPadding(0, 0, 0, 12);
        card.addView(cardTitle);

        return card;
    }

    private void addDivider(LinearLayout parent) {
        View divider = new View(this);
        divider.setBackgroundColor(0xFFE0E0E0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1);
        params.topMargin = 4;
        params.bottomMargin = 4;
        divider.setLayoutParams(params);
        parent.addView(divider);
    }

    private LinearLayout createToolItem(String label, String iconName, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 14, 0, 14);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(listener);

        // Icon
        ImageView iconView = new ImageView(this);
        int iconRes = getResources().getIdentifier(iconName, "drawable", getPackageName());
        if (iconRes != 0) iconView.setImageResource(iconRes);
        iconView.setColorFilter(0xFF757575);
        iconView.setLayoutParams(new LinearLayout.LayoutParams(40, 40));
        iconView.setPadding(0, 0, 16, 0);
        row.addView(iconView);

        // Label
        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(16);
        labelView.setTextColor(COLOR_TEXT_PRIMARY);
        labelView.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(labelView);

        // 右箭头
        ImageView arrowView = new ImageView(this);
        arrowView.setImageResource(R.drawable.ic_forward);
        arrowView.setColorFilter(0xFFBDBDBD);
        arrowView.setLayoutParams(new LinearLayout.LayoutParams(24, 24));
        arrowView.setScaleType(ImageView.ScaleType.CENTER);
        row.addView(arrowView);

        return row;
    }

    private LinearLayout createToggleItem(String label, String key, boolean defaultValue) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 14, 0, 14);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(16);
        labelView.setTextColor(COLOR_TEXT_PRIMARY);
        labelView.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(labelView);

        android.widget.Switch switchView = new android.widget.Switch(this);
        switchView.setChecked(prefs.getBoolean(key, defaultValue));
        row.addView(switchView);

        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(key, isChecked).apply();
            Intent intent = new Intent("com.xuanfeng.browser." + key.toUpperCase() + "_CHANGED");
            sendBroadcast(intent);
        });

        return row;
    }
}
