package com.xuanfeng.browser;

import android.content.Context;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
    private static HistoryManager instance;
    private Context context;
    private List<HistoryItem> historyList;
    private File historyFile;

    public static class HistoryItem {
        public String url;
        public String title;
        public long timestamp;

        public HistoryItem(String url, String title, long timestamp) {
            this.url = url;
            this.title = title;
            this.timestamp = timestamp;
        }

        public JSONObject toJson() {
            try {
                JSONObject obj = new JSONObject();
                obj.put("url", url);
                obj.put("title", title);
                obj.put("timestamp", timestamp);
                return obj;
            } catch (Exception e) {
                return new JSONObject();
            }
        }

        public static HistoryItem fromJson(JSONObject obj) {
            try {
                return new HistoryItem(
                    obj.optString("url", ""),
                    obj.optString("title", ""),
                    obj.optLong("timestamp", 0)
                );
            } catch (Exception e) {
                return new HistoryItem("", "", 0);
            }
        }
    }

    private HistoryManager(Context context) {
        this.context = context.getApplicationContext();
        historyList = new ArrayList<>();
        historyFile = new File(context.getExternalFilesDir(null), "history_v2.json");
        loadHistory();
    }

    public static synchronized HistoryManager getInstance(Context context) {
        if (instance == null) {
            instance = new HistoryManager(context);
        }
        return instance;
    }

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
                    JSONObject obj = jsonArray.getJSONObject(i);
                    historyList.add(HistoryItem.fromJson(obj));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveHistory() {
        try {
            if (historyFile != null) {
                historyFile.getParentFile().mkdirs();
                JSONArray jsonArray = new JSONArray();
                for (HistoryItem item : historyList) {
                    jsonArray.put(item.toJson());
                }
                try (FileWriter writer = new FileWriter(historyFile)) {
                    writer.write(jsonArray.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 添加记录（去重，最新在最前）
    public void addHistory(String url, String title) {
        if (url == null || url.isEmpty()) return;
        if (title == null || title.isEmpty()) {
            title = url;
        }
        // 移除已存在的相同URL
        HistoryItem existing = null;
        for (HistoryItem item : historyList) {
            if (item.url.equals(url)) {
                existing = item;
                break;
            }
        }
        if (existing != null) {
            historyList.remove(existing);
        }
        // 插入到最前面
        historyList.add(0, new HistoryItem(url, title, System.currentTimeMillis()));
        // 保留最多200条
        while (historyList.size() > 200) {
            historyList.remove(historyList.size() - 1);
        }
        saveHistory();
    }

    // 兼容旧版：只传url时使用url作为标题
    public void addHistory(String url) {
        addHistory(url, url);
    }

    // 删除记录
    public boolean removeHistory(int position) {
        if (position >= 0 && position < historyList.size()) {
            historyList.remove(position);
            saveHistory();
            return true;
        }
        return false;
    }

    // 获取历史列表
    public List<HistoryItem> getHistoryList() {
        return new ArrayList<>(historyList);
    }

    // 清除所有历史
    public void clearHistory() {
        historyList.clear();
        saveHistory();
    }
}
