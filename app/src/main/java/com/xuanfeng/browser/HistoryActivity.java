package com.xuanfeng.browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends Activity {

    private static final int COLOR_PRIMARY = 0xFF1565C0;
    private static final int COLOR_TEXT_PRIMARY = 0xFF212121;
    private static final int COLOR_TEXT_SECONDARY = 0xFF757575;
    private static final int COLOR_SURFACE = 0xFFFFFFFF;
    private static final int COLOR_DIVIDER = 0xFFE0E0E0;

    private ListView listView;
    private List<HistoryManager.HistoryItem> historyItems;
    private List<String> displayList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(0xFFF5F5F5);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // 标题栏
        LinearLayout titleBar = createTitleBar();
        rootLayout.addView(titleBar);

        // 内容区域
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(16, 8, 16, 0);
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0, 1f));

        // 标题文字
        TextView pageTitle = new TextView(this);
        pageTitle.setText("浏览历史");
        pageTitle.setTextSize(20);
        pageTitle.setTypeface(null, Typeface.BOLD);
        pageTitle.setTextColor(COLOR_TEXT_PRIMARY);
        pageTitle.setPadding(8, 16, 8, 16);
        contentLayout.addView(pageTitle);

        // 工具按钮行
        LinearLayout toolBar = new LinearLayout(this);
        toolBar.setOrientation(LinearLayout.HORIZONTAL);
        toolBar.setPadding(0, 0, 0, 12);

        TextView clearAll = new TextView(this);
        clearAll.setText("清空全部");
        clearAll.setTextSize(14);
        clearAll.setTextColor(0xFFE53935);
        clearAll.setPadding(12, 8, 12, 8);
        clearAll.setClickable(true);
        clearAll.setOnClickListener(v -> showClearAllConfirm());
        toolBar.addView(clearAll);

        TextView clearDone = new TextView(this);
        clearDone.setText("关闭");
        clearDone.setTextSize(14);
        clearDone.setTextColor(COLOR_PRIMARY);
        clearDone.setPadding(12, 8, 12, 8);
        clearDone.setClickable(true);
        clearDone.setOnClickListener(v -> finish());
        toolBar.addView(clearDone);

        contentLayout.addView(toolBar);

        // 列表视图
        listView = new ListView(this);
        listView.setDivider(null);
        listView.setPadding(0, 0, 0, 0);
        listView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        refreshList();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            HistoryManager.HistoryItem item = historyItems.get(position);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.url));
            startActivity(intent);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            HistoryManager.HistoryItem item = historyItems.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("删除记录")
                    .setMessage("删除 " + item.title + " ？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        HistoryManager.getInstance(this).removeHistory(position);
                        refreshList();
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return true;
        });

        contentLayout.addView(listView);
        rootLayout.addView(contentLayout);

        setContentView(rootLayout);
    }

    private LinearLayout createTitleBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setPadding(8, 32, 8, 8);
        bar.setBackgroundColor(COLOR_SURFACE);
        bar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        ImageView backBtn = new ImageView(this);
        backBtn.setImageResource(R.drawable.ic_arrow_back);
        backBtn.setColorFilter(COLOR_PRIMARY);
        backBtn.setPadding(0, 0, 16, 0);
        backBtn.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
        backBtn.setOnClickListener(v -> finish());
        backBtn.setClickable(true);
        bar.addView(backBtn);

        TextView title = new TextView(this);
        title.setText("历史记录");
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(COLOR_TEXT_PRIMARY);
        bar.addView(title);

        return bar;
    }

    private void refreshList() {
        historyItems = HistoryManager.getInstance(this).getHistoryList();
        displayList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

        for (HistoryManager.HistoryItem item : historyItems) {
            String timeStr = sdf.format(new Date(item.timestamp));
            displayList.add(item.title + "\n" + item.url + "\n" + timeStr);
        }

        if (adapter == null) {
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, displayList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    if (view instanceof TextView) {
                        TextView tv = (TextView) view;
                        tv.setTextColor(COLOR_TEXT_PRIMARY);
                        tv.setTextSize(13);
                        tv.setPadding(20, 16, 20, 16);
                        tv.setLineSpacing(4, 1);

                        GradientDrawable gd = new GradientDrawable();
                        gd.setShape(GradientDrawable.RECTANGLE);
                        gd.setColor(COLOR_SURFACE);
                        gd.setCornerRadius(10);
                        tv.setBackground(gd);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        params.bottomMargin = 8;
                        tv.setLayoutParams(params);
                    }
                    return view;
                }
            };
            listView.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.addAll(displayList);
            adapter.notifyDataSetChanged();
        }
    }

    private void showClearAllConfirm() {
        new AlertDialog.Builder(this)
                .setTitle("全部删除")
                .setMessage("确定删除所有历史记录吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    HistoryManager.getInstance(this).clearHistory();
                    refreshList();
                    Toast.makeText(this, "历史记录已清空", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
