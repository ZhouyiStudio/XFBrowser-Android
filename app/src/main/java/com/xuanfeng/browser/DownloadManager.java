package com.xuanfeng.browser;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DownloadManager {
    private static DownloadManager instance;
    private Context context;
    private File jsonFile;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    
    private DownloadManager(Context context) {
    this.context = context.getApplicationContext();
    // /storage/emulated/0/Android/data/com.xuanfeng.browser/files/download.json
    File dir = context.getExternalFilesDir(null);
    if (dir != null) {
        jsonFile = new File(dir, "download.json");
        // 不，创空
        if (!jsonFile.exists()) {
            try {
                jsonFile.getParentFile().mkdirs();
                FileWriter writer = new FileWriter(jsonFile);
                writer.write("[]");
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
    
    public static synchronized DownloadManager getInstance(Context context) {
        if (instance == null) {
            instance = new DownloadManager(context);
        }
        return instance;
    }
    
    // 加载记录
    public List<DownloadItem> loadFromJson() {
        List<DownloadItem> items = new ArrayList<>();
        
        if (jsonFile != null && jsonFile.exists()) {
            try (FileReader reader = new FileReader(jsonFile)) {
                StringBuilder sb = new StringBuilder();
                char[] buffer = new char[1024];
                int len;
                while ((len = reader.read(buffer)) != -1) {
                    sb.append(buffer, 0, len);
                }
                
                JSONArray array = new JSONArray(sb.toString());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    
                    DownloadItem item = new DownloadItem(
                        obj.getString("fileName"),
                        obj.getString("filePath")
                    );
                    item.setForeign(false);
                    item.setStatus(obj.optInt("status", 3));
                    item.setProgress(obj.optInt("progress", 0));
                    item.setFileSize(obj.optLong("fileSize", 0));
                    item.setDownloadTime(obj.optString("downloadTime", ""));
                    item.setFileType(obj.optString("fileType", ""));
                    
                    items.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return items;
    }
    
    // 存记录
    private void saveToJson(List<DownloadItem> items) {
        if (jsonFile == null) return;
        
        try {
            JSONArray array = new JSONArray();
            for (DownloadItem item : items) {
                JSONObject obj = new JSONObject();
                obj.put("fileName", item.getFileName());
                obj.put("filePath", item.getFilePath());
                obj.put("status", item.getStatus());
                obj.put("progress", item.getProgress());
                obj.put("fileSize", item.getFileSize());
                obj.put("downloadTime", item.getDownloadTime());
                obj.put("fileType", item.getFileType());
                array.put(obj);
            }
            
            try (FileWriter writer = new FileWriter(jsonFile)) {
                writer.write(array.toString(2)); // 格式化输出
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 加新记录
    public void addDownloadRecord(String url, String fileName, String filePath, long fileSize) {
        List<DownloadItem> items = loadFromJson();
        
        DownloadItem item = new DownloadItem(fileName, filePath);
        item.setForeign(false);
        item.setStatus(3);
        item.setFileSize(fileSize);
        item.setDownloadTime(dateFormat.format(new Date()));
        item.setFileType(getFileExtension(fileName));
        
        // yn存在
        boolean exists = false;
        for (DownloadItem existing : items) {
            if (existing.getFilePath().equals(filePath)) {
                exists = true;
                break;
            }
        }
        
        if (!exists) {
            items.add(item);
            saveToJson(items);
        }
    }
    
    // 删
    public void removeFromJson(DownloadItem item) {
        List<DownloadItem> items = loadFromJson();
        items.removeIf(i -> i.getFilePath().equals(item.getFilePath()));
        saveToJson(items);
    }
    
    // 更记录
    public void updateRecord(DownloadItem item) {
        List<DownloadItem> items = loadFromJson();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getFilePath().equals(item.getFilePath())) {
                items.set(i, item);
                break;
            }
        }
        saveToJson(items);
    }
    
    // 后缀
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    // 创建任务
    public String createDownloadTask(String url, String fileName) {
        String taskId = String.valueOf(System.currentTimeMillis());
        return taskId;
    }
    
    // 暂停（功能待实现）
    public void pauseDownload(String taskId) {
        Toast.makeText(context, "暂停功能开发中", Toast.LENGTH_SHORT).show();
    }
    
    // 继续
    public void resumeDownload(String taskId) {
        Toast.makeText(context, "继续功能开发中", Toast.LENGTH_SHORT).show();
    }
    
    // 取消
    public void cancelDownload(String taskId) {
        Toast.makeText(context, "取消功能开发中", Toast.LENGTH_SHORT).show();
    }
}