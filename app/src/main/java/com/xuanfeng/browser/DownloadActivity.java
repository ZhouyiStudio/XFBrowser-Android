
package com.xuanfeng.browser;

import android.app.Activity;  // 改这里
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DownloadActivity extends Activity { 
    
    private ListView listView;
    private TextView tvEmpty;
    private DownloadAdapter adapter;
    private List<DownloadItem> downloadItems = new ArrayList<>();
    private DownloadManager downloadManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
    
      try {
          setContentView(R.layout.activity_download);
        
          downloadManager = DownloadManager.getInstance(this);
        
          listView = findViewById(R.id.list_view);
          tvEmpty = findViewById(R.id.tv_empty);
        
          findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
          adapter = new DownloadAdapter(downloadItems, item -> showItemDialog(item));
          listView.setAdapter(adapter);
      } catch (Exception e) {
          Toast.makeText(this, "错误: " + e.getMessage(), Toast.LENGTH_LONG).show();
          e.printStackTrace();
      }
    }
    
    
    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }
    
    private void refreshList() {
        downloadItems.clear();
        
        // JSON加
        List<DownloadItem> recordedItems = downloadManager.loadFromJson();
        
        // 扫实
        File downloadDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS), "xfbrowser/下载/");
        
        if (downloadDir.exists()) {
            File[] files = downloadDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        boolean found = false;
                        for (DownloadItem item : recordedItems) {
                            if (item.getFilePath().equals(file.getAbsolutePath())) {
                                item.setFileSize(file.length());
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            DownloadItem foreign = new DownloadItem(file.getName(), file.getAbsolutePath());
                            foreign.setFileSize(file.length());
                            foreign.setForeign(true);
                            recordedItems.add(foreign);
                        }
                    }
                }
            }
        }
        
        // 滤不在
        List<DownloadItem> toRemove = new ArrayList<>();
        for (DownloadItem item : recordedItems) {
            if (!item.isForeign()) {
                File file = new File(item.getFilePath());
                if (!file.exists()) {
                    toRemove.add(item);
                }
            }
        }
        recordedItems.removeAll(toRemove);
        
        // 排
        Collections.sort(recordedItems, (o1, o2) -> {
            if (o1.getStatus() == 1 && o2.getStatus() != 1) return -1;
            if (o2.getStatus() == 1 && o1.getStatus() != 1) return 1;
            if (o1.isForeign() && !o2.isForeign()) return 1;
            if (o2.isForeign() && !o1.isForeign()) return -1;
            if (o1.getDownloadTime() != null && o2.getDownloadTime() != null) {
                return o2.getDownloadTime().compareTo(o1.getDownloadTime());
            }
            return 0;
        });
        
        downloadItems.addAll(recordedItems);
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(downloadItems.isEmpty() ? View.VISIBLE : View.GONE);
    }
    
    private void showItemDialog(DownloadItem item) {
        StringBuilder info = new StringBuilder();
        info.append("文件名: ").append(item.getFileName()).append("\n");
        info.append("大小: ").append(item.getFormattedSize()).append("\n");
        info.append("路径: ").append(item.getFilePath()).append("\n");
        
        if (item.isForeign()) {
            info.append("类型: 外来文件");
        } else {
            info.append("下载时间: ").append(item.getDownloadTime()).append("\n");
            info.append("状态: ").append(getStatusText(item.getStatus()));
            if (item.getStatus() == 1) {
                info.append(" ").append(item.getProgress()).append("%");
            }
        }
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 8, 16, 8);
        
        if (item.isForeign()) {
            layout.addView(createIconRow("ic_close", "删除文件", () -> deleteFile(item)));
        } else {
            layout.addView(createIconRow("ic_menu", "更多操作", () -> showMoreOptions(item)));
        }
        
        new AlertDialog.Builder(this)
            .setTitle("文件信息")
            .setMessage(info.toString())
            .setView(layout)
            .setPositiveButton("确定", null)
            .show();
    }
    
    private LinearLayout createIconRow(String iconName, String label, Runnable onClick) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(12, 16, 12, 16);
        row.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        row.setClickable(true);
        row.setFocusable(true);
        row.setBackgroundResource(android.R.drawable.list_selector_background);
        
        ImageView iconView = new ImageView(this);
        int iconRes = getResources().getIdentifier(iconName, "drawable", getPackageName());
        if (iconRes != 0) iconView.setImageResource(iconRes);
        iconView.setColorFilter(0xFF757575);
        iconView.setLayoutParams(new LinearLayout.LayoutParams(36, 36));
        iconView.setPadding(0, 0, 16, 0);
        row.addView(iconView);
        
        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(16);
        labelView.setTextColor(0xFF212121);
        labelView.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(labelView);
        
        row.setOnClickListener(v -> onClick.run());
        
        return row;
    }
    
    private String getStatusText(int status) {
        switch (status) {
            case 1: return "下载中";
            case 2: return "已暂停";
            case 3: return "已完成";
            case 4: return "失败";
            default: return "未知";
        }
    }
    
    private void deleteFile(DownloadItem item) {
        new AlertDialog.Builder(this)
            .setTitle("删除文件")
            .setMessage("确定删除该文件吗？")
            .setPositiveButton("删除", (dialog, which) -> {
                File file = new File(item.getFilePath());
                if (file.exists()) {
                    file.delete();
                }
                if (!item.isForeign()) {
                    downloadManager.removeFromJson(item);
                }
                refreshList();
                Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    private void showMoreOptions(DownloadItem item) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 8, 16, 8);
        
        if (item.getStatus() == 1) {
            layout.addView(createIconRow("ic_stop", "暂停", () -> {
                downloadManager.pauseDownload(item.getTaskId());
                refreshList();
            }));
            layout.addView(createIconRow("ic_close", "取消下载", () -> {
                downloadManager.cancelDownload(item.getTaskId());
                deleteFile(item);
            }));
        } else if (item.getStatus() == 2) {
            layout.addView(createIconRow("ic_refresh", "继续", () -> {
                downloadManager.resumeDownload(item.getTaskId());
                refreshList();
            }));
            layout.addView(createIconRow("ic_close", "取消下载", () -> {
                downloadManager.cancelDownload(item.getTaskId());
                deleteFile(item);
            }));
        } else {
            layout.addView(createIconRow("ic_download_mgr", "打开文件", () -> openFile(item)));
            layout.addView(createIconRow("ic_menu", "打开文件夹", () -> openFolder(item)));
            layout.addView(createIconRow("ic_close", "删除", () -> deleteFile(item)));
        }
        
        new AlertDialog.Builder(this)
            .setTitle("操作")
            .setView(layout)
            .setNegativeButton("返回", null)
            .show();
    }
    
    private void openFile(DownloadItem item) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            File file = new File(item.getFilePath());
            Uri uri = Uri.fromFile(file);
            String mimeType = getMimeType(item.getFileName());
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            // 试MT
            intent.setPackage("bin.mt.plus");
            startActivity(intent);
            Toast.makeText(this, "为了方便已使用MT打开", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // MTno默认
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                File file = new File(item.getFilePath());
                Uri uri = Uri.fromFile(file);
                String mimeType = getMimeType(item.getFileName());
                intent.setDataAndType(uri, mimeType);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
                // 不提
            } catch (Exception e2) {
                Toast.makeText(this, "无法打开文件", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void openFolder(DownloadItem item) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            File file = new File(item.getFilePath());
            Uri uri = Uri.fromFile(file.getParentFile());
            intent.setDataAndType(uri, "resource/folder");
            
            // 优先MT
            intent.setPackage("bin.mt.plus");
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "请安装 MT 管理器", Toast.LENGTH_SHORT).show();
        }
    }
    
    private String getMimeType(String fileName) {
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (ext) {
            case "mp4": case "avi": case "mkv": return "video/*";
            case "mp3": case "wav": case "flac": return "audio/*";
            case "jpg": case "jpeg": case "png": case "gif": return "image/*";
            case "pdf": return "application/pdf";
            case "txt": return "text/plain";
            case "apk": return "application/vnd.android.package-archive";
            case "zip": case "rar": case "7z": return "application/zip";
            default: return "*/*";
        }
    }
}