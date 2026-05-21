package com.xuanfeng.browser;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import java.io.PrintWriter;
import java.io.StringWriter;

public class CrashService extends Service {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 独进程捕崩
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            try {
                // 收崩信
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                String stackTrace = sw.toString();
                
                String errorMsg = ex.toString();
                String logMsg = stackTrace.length() > 500 ? 
                    stackTrace.substring(0, 500) + "..." : stackTrace;
                
                // 启
                Intent intent = new Intent(this, CrashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("error", errorMsg);
                intent.putExtra("log", stackTrace);
                intent.putExtra("process", "主进程崩溃");
                startActivity(intent);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // e
            Process.killProcess(Process.myPid());
            System.exit(1);
        });
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}