package com.xuanfeng.browser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CrashActivity extends Activity {
    
    private TextView tvLog, tvError, tvProcess;
    private ImageButton btnExport, btnRestart, btnExit;
    private String crashLog = "";
    private String crashError = "";
    private String crashProcess = "";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);
        
        // 崩信息
        crashError = getIntent().getStringExtra("error");
        crashLog = getIntent().getStringExtra("log");
        crashProcess = getIntent().getStringExtra("process");
        
        tvLog = findViewById(R.id.tv_log);
        tvError = findViewById(R.id.tv_error);
        tvProcess = findViewById(R.id.tv_process);
        btnExport = findViewById(R.id.btn_export);
        btnRestart = findViewById(R.id.btn_restart);
        btnExit = findViewById(R.id.btn_exit);
        
        tvLog.setText("log: " + (crashLog != null ? crashLog : "无"));
        tvError.setText("error: " + (crashError != null ? crashError : "无"));
        tvProcess.setText("Error process: " + (crashProcess != null ? crashProcess : "未知"));
        
        btnExport.setOnClickListener(v -> exportLog());
        btnRestart.setOnClickListener(v -> restartApp());
        btnExit.setOnClickListener(v -> exitApp());
    }
    
    private void exportLog() {
        try {
            File logDir = new File(getExternalFilesDir(null), "crash_log");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            String fileName = dateFormat.format(new Date()) + "_crash.log";
            File logFile = new File(logDir, fileName);
            
            FileWriter writer = new FileWriter(logFile);
            writer.write("=== XuanFeng Browser 崩溃日志 ===\n");
            writer.write("时间: " + dateFormat.format(new Date()) + "\n");
            writer.write("错误进程: " + crashProcess + "\n");
            writer.write("错误信息: " + crashError + "\n\n");
            writer.write("详细日志:\n" + crashLog + "\n");
            writer.close();
            
            Toast.makeText(this, "日志已导出: " + logFile.getPath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Process.killProcess(Process.myPid());
    }
    
    private void exitApp() {
        finish();
        Process.killProcess(Process.myPid());
        System.exit(0);
    }
}