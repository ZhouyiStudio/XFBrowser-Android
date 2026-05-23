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
import java.util.ArrayList;
import android.view.View;
import android.graphics.Typeface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    private SharedPreferences prefs;

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
        addDivider(browseCard);
        addPickerItem(browseCard, "网络模式", "",
                "network_mode", "internet",
                new String[]{"Internet", "Xuanfeng Network"},
                new String[]{"internet", "xf"});
        addDivider(browseCard);
        addSwitchItem(browseCard, "开发者工具", "启用 WebView 远程调试 (需重启)", "devtools_enabled", false);
        root.addView(browseCard);

        // ====== 界面设置 ======
        LinearLayout uiCard = createCard("界面设置");
        addSwitchItem(uiCard, "隐藏标题", "隐藏主界面顶部的标题", "hide_title", false);
        addDivider(uiCard);
        addSwitchItem(uiCard, "选中网址栏时全选", "点击网址栏自动全选文字", "select_all_on_focus", true);
        addDivider(uiCard);
        addSwitchItem(uiCard, "深色模式", "切换深色/浅色主题", "dark_mode", false);
        addDivider(uiCard);
        addPickerItem(uiCard, "字体", "选择浏览器界面和网页字体",
                "custom_font", "default",
                new String[]{"系统默认", "宋体", "黑体", "楷体", "微软雅黑", "Sans Serif", "Serif", "Monospace"},
                new String[]{"default", "宋体", "黑体", "楷体", "微软雅黑", "sans-serif", "serif", "monospace"});
        root.addView(uiCard);

        // ====== 颜色设置 ======
        LinearLayout colorCard = createCard("颜色设置");
        addColorItem(colorCard, "主题色", "xf_primary", "0xFF1565C0",
                new String[]{"蓝色", "红色", "绿色", "紫色", "橙色", "青色", "灰色"},
                new String[]{"0xFF1565C0", "0xFFE53935", "0xFF43A047", "0xFF7B1FA2", "0xFFEF6C00", "0xFF00ACC1", "0xFF757575"});
        addDivider(colorCard);
        addColorItem(colorCard, "背景色", "xf_background", "0xFFF5F5F5",
                new String[]{"浅灰", "白色", "深色"},
                new String[]{"0xFFF5F5F5", "0xFFFFFFFF", "0xFF212121"});
        addDivider(colorCard);
        addColorItem(colorCard, "工具栏图标色", "xf_toolbar_icon", "0xFF333333",
                new String[]{"深灰", "黑色", "蓝色", "白色"},
                new String[]{"0xFF333333", "0xFF000000", "0xFF1565C0", "0xFFFFFFFF"});
        root.addView(colorCard);

        // ====== 数据管理 ======
        LinearLayout dataCard = createCard("数据管理");
        dataCard.addView(createClickableRow("书签管理", "查看和管理您的书签", v -> showBookmarkDialog()));
        addDivider(dataCard);
        dataCard.addView(createClickableRow("历史记录管理", "查看和管理浏览历史", v -> startActivity(new Intent(this, HistoryActivity.class))));
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


    private void showBookmarkDialog() {
        final List<BookmarkManager.BookmarkItem> bookmarks = BookmarkManager.getInstance(this).getBookmarks();

        if (bookmarks == null || bookmarks.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("书签")
                    .setMessage("暂无书签")
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

        List<String> displayList = new ArrayList<>();
        for (BookmarkManager.BookmarkItem item : bookmarks) {
            displayList.add(item.title + "\n" + item.url);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, displayList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(COLOR_TEXT_PRIMARY);
                    ((TextView) view).setTextSize(14);
                    ((TextView) view).setPadding(24, 12, 24, 12);
                }
                return view;
            }
        };
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            BookmarkManager.BookmarkItem item = bookmarks.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("删除书签")
                    .setMessage("删除 " + item.title + " ?")
                    .setPositiveButton("删除", (dialog, which) -> {
                        BookmarkManager.getInstance(this).removeBookmark(item.title, item.url);
                        bookmarks.remove(position);
                        displayList.remove(position);
                        adapter.notifyDataSetChanged();
                        if (adapter.getCount() == 0) {
                            Toast.makeText(this, "书签已清空", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        layout.addView(listView, new LinearLayout.LayoutParams(-1, -1));

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("书签管理")
                .setView(layout)
                .setPositiveButton("关闭", null)
                .create();

        dialog.show();
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

    private void addColorItem(LinearLayout layout, String label,
                               String key, String defaultColor,
                               String[] displayNames, String[] colorValues) {
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

        // 右侧颜色预览区
        LinearLayout valueContainer = new LinearLayout(this);
        valueContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        valueContainer.setGravity(android.view.Gravity.CENTER_VERTICAL);
        valueContainer.setPadding(16, 0, 0, 0);

        // 颜色圆形预览
        final ImageView colorPreview = new ImageView(this);
        int previewSize = 36;
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(previewSize, previewSize);
        colorPreview.setLayoutParams(previewParams);
        colorPreview.setScaleType(ImageView.ScaleType.CENTER);
        colorPreview.setClickable(true);
        colorPreview.setFocusable(true);

        // 设置初始颜色
        String currentColor = prefs.getString(key, defaultColor);
        updateColorPreview(colorPreview, currentColor);

        colorPreview.setOnClickListener(v -> {
            // 构建颜色选项弹窗
            LinearLayout dialogLayout = new LinearLayout(this);
            dialogLayout.setOrientation(LinearLayout.HORIZONTAL);
            dialogLayout.setPadding(24, 24, 24, 24);
            dialogLayout.setGravity(android.view.Gravity.CENTER);

            // 先创建 Dialog，保存引用以便点击后关闭
            AlertDialog colorDialog = new AlertDialog.Builder(this)
                    .setTitle(label)
                    .setView(dialogLayout)
                    .setPositiveButton("取消", null)
                    .create();

            for (int i = 0; i < colorValues.length; i++) {
                final int index = i;
                final String colorStr = colorValues[i];
                ImageView swatch = new ImageView(this);
                int swatchSize = 48;
                LinearLayout.LayoutParams swatchParams = new LinearLayout.LayoutParams(swatchSize, swatchSize);
                swatchParams.setMargins(8, 0, 8, 0);
                swatch.setLayoutParams(swatchParams);
                swatch.setScaleType(ImageView.ScaleType.CENTER);
                swatch.setClickable(true);
                swatch.setFocusable(true);

                // 绘制颜色圆形
                updateColorPreview(swatch, colorStr);

                // 选中标记
                if (colorStr.equals(prefs.getString(key, defaultColor))) {
                    // 加边框表示选中
                    GradientDrawable border = new GradientDrawable();
                    border.setShape(GradientDrawable.OVAL);
                    border.setStroke(4, 0xFF1976D2);
                    int bgColor = safeParseColor(colorStr);
                    border.setColor(bgColor);
                    swatch.setImageDrawable(null);
                    swatch.setBackground(border);
                }

                final AlertDialog dialogRef = colorDialog;
                swatch.setOnClickListener(sv -> {
                    prefs.edit().putString(key, colorStr).apply();
                    updateColorPreview(colorPreview, colorStr);
                    Intent intent = new Intent("com.xuanfeng.browser." + key.toUpperCase() + "_CHANGED");
                    sendBroadcast(intent);
                    dialogRef.dismiss(); // 关闭弹窗
                });
                dialogLayout.addView(swatch);
            }

            colorDialog.show();
        });

        valueContainer.addView(colorPreview);
        container.addView(textLayout);
        container.addView(valueContainer);
        layout.addView(container);
    }

    // 将 0x... 格式转为 #... 格式，兼容两种写法
    private int safeParseColor(String colorStr) {
        if (colorStr == null) colorStr = "#FF1565C0";
        if (colorStr.startsWith("0x") || colorStr.startsWith("0X")) {
            colorStr = "#" + colorStr.substring(2);
        }
        return Color.parseColor(colorStr);
    }

    private void updateColorPreview(ImageView view, String colorHex) {
        try {
            int color = safeParseColor(colorHex);
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(color);
            // 加浅边框以便白色颜色可见
            circle.setStroke(1, 0x44000000);
            view.setBackground(circle);
            view.setImageDrawable(null);
        } catch (Exception e) {
            // fallback
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(0xFF1565C0);
            view.setBackground(circle);
            view.setImageDrawable(null);
        }
    }
}
