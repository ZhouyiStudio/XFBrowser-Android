package com.xuanfeng.browser;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
    private static HistoryManager instance;
    private Context context;
    private List<String> historyList;
    private File historyFile;
    
    private HistoryManager(Context context) {
        this.context = context.getApplicationContext();
        historyList = new ArrayList<>();
        historyFile = new File(context.getExternalFilesDir(null), "history.json");
        loadHistory();
    }
    
    public static synchronized HistoryManager getInstance(Context context) {
        if (instance == null) {
            instance = new HistoryManager(context);
        }
        return instance;
    }
    
    // 加载历史记录到内存缓存
    private void loadHistory() {
        historyList.clear();
        if (historyFile.exists()) {
            try (FileReader reader = new FileReader(historyFile)) {
                StringBuilder sb = new StringBuilder();
                char[] buffer = new char[4096];
                int len;
                while ((len = reader.read(buffer)) != -1) {
                    sb.append(buffer, 0, len);
                }
                JSONArray jsonArray = new JSONArray(sb.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    historyList.add(jsonArray.getString(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // 持久化保存
    private void saveHistory() {
        try {
            if (historyFile != null) {
                historyFile.getParentFile().mkdirs();
                JSONArray jsonArray = new JSONArray(historyList);
                try (FileWriter writer = new FileWriter(historyFile)) {
                    writer.write(jsonArray.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 添加记录（去重，最新在最前）
    public void addHistory(String url) {
        if (url == null || url.isEmpty()) return;
        // 移除已存在的
        historyList.remove(url);
        // 插入到最前面
        historyList.add(0, url);
        // 保留最多50条
        while (historyList.size() > 50) {
            historyList.remove(historyList.size() - 1);
        }
        saveHistory();
    }
    
    // 删除记录
    public void removeHistory(int position) {
        if (position >= 0 && position < historyList.size()) {
            historyList.remove(position);
            saveHistory();
        }
    }
    
    // 获取历史列表
    public List<String> getHistoryList() {
        return new ArrayList<>(historyList);
    }
    
    // 清除所有历史
    public void clearHistory() {
        historyList.clear();
        saveHistory();
    }
}