package com.xuanfeng.browser;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ProtocolManager {
    
    private Context context;
    private File protocolFile;
    private List<Protocol> protocols = new ArrayList<>();
    
    public static class Protocol {
        public String name;
        public String description;
        public String type;
        public int defaultPort;
        public String handler;
        public JSONObject config;
        public JSONObject ui;
        
        public Protocol(JSONObject obj) throws Exception {
            this.name = obj.getString("name");
            this.description = obj.optString("description", "");
            this.type = obj.getString("type");
            this.defaultPort = obj.optInt("defaultPort", 0);
            this.handler = obj.optString("handler", "raw");
            this.config = obj.optJSONObject("config");
            this.ui = obj.optJSONObject("ui");
        }
    }
    
    public ProtocolManager(Context context) {
        this.context = context;
        this.protocolFile = new File(context.getExternalFilesDir(null), "protocol/protocol.json");
        checkAndLoad();
    }
    
    private void checkAndLoad() {
        if (!protocolFile.exists()) {
            // 文件不存在，询问是否下载
            askDownload();
        } else {
            loadProtocols();
        }
    }
    
    private void askDownload() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle("下载基础功能");
        builder.setMessage("未检测到协议配置文件，是否下载基础功能包？");
        builder.setPositiveButton("下载", (dialog, which) -> {
            downloadProtocolFile();
        });
        builder.setNegativeButton("取消", (dialog, which) -> {
            // 不下载，使用空列表
        });
        builder.show();
    }
    
    private void downloadProtocolFile() {
        new Thread(() -> {
            try {
                URL url = new URL("https://xfbrowser.fwh.is/protocol.json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                
                if (conn.getResponseCode() == 200) {
                    protocolFile.getParentFile().mkdirs();
                    FileOutputStream fos = new FileOutputStream(protocolFile);
                    InputStream is = conn.getInputStream();
                    
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    is.close();
                    
                    // 下载完成，重新加载
                    loadProtocols();
                    
                    ((android.app.Activity)context).runOnUiThread(() -> {
                        Toast.makeText(context, "协议文件下载完成", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    showError("下载失败，服务器返回: " + conn.getResponseCode());
                }
                conn.disconnect();
            } catch (Exception e) {
                showError("下载失败: " + e.getMessage());
            }
        }).start();
    }
    
    private void showError(String msg) {
        ((android.app.Activity)context).runOnUiThread(() -> {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        });
    }
    
    private void loadProtocols() {
        protocols.clear();
        try {
            FileReader reader = new FileReader(protocolFile);
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int len;
            while ((len = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, len);
            }
            reader.close();
            
            JSONArray array = new JSONArray(sb.toString());
            for (int i = 0; i < array.length(); i++) {
                protocols.add(new Protocol(array.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public List<Protocol> getProtocols() {
        return protocols;
    }
}