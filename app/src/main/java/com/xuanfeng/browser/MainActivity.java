package com.xuanfeng.browser;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.*;
import android.widget.*;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Color;
import android.content.res.ColorStateList;
import java.util.Map;
import java.util.HashMap;
import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import java.io.File;
import java.io.FileOutputStream;
import android.os.Environment;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.view.ViewGroup;
import android.content.SharedPreferences;
import android.app.DownloadManager;
import android.net.Uri;
import android.webkit.DownloadListener;
import android.content.Context;
import android.widget.EditText;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.widget.CheckBox;
import java.net.HttpURLConnection;
import java.net.URL;
import android.webkit.CookieManager;
import java.net.URLEncoder;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import android.view.MotionEvent;
import android.graphics.Typeface;
import java.util.Locale;
import java.io.FileWriter;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;
import android.view.inputmethod.InputMethodManager;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.HorizontalScrollView;
import android.widget.ListPopupWindow;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.webkit.WebViewClient;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import android.widget.ArrayAdapter;

public class MainActivity extends Activity {
    
    private List<WebView> webViewList = new ArrayList<>();
    private int currentTabIndex = 0;
    private TextView tvTabCount;
    private WebView webView;
    private AutoCompleteTextView etUrl;
    private FrameLayout webviewContainer;
    
    private ImageButton btnBack, btnForward, btnRefresh, btnDesktop, btnSettings, btnMenu, btnTabs;
    private ProgressBar progressBar;
    private boolean isAdBlockEnabled = false;
    private boolean safeBrowsingEnabled = false;
    private boolean adBlockEnabled = false;  
    private List<String> adRules = new ArrayList<>();  
    
    private ValueCallback<Uri[]> uploadMessage;
    private final static int FILE_CHOOSER_RESULT_CODE = 1;
    private SharedPreferences prefs;
    
    // F12
    private FrameLayout f12Float;
    private boolean f12Visible = false;
    private int lastX, lastY;
    private int floatWidth = 500;
    private int floatHeight = 600;
    private boolean f12Dialogmenuing = false;
    
    // 页内查找
    private FrameLayout searchFloat;
    private boolean searchVisible = false;
    private EditText etSearchInput;
    private TextView tvSearchResult;
    private boolean enhancedMode = false;
    
    private boolean isDesktopMode = false;
    private boolean alwaysLog = false;
    private String urlMode = "always";
    
    // 爬虫相关
    private Set<String> crawledUrls = new HashSet<>();
    private Queue<String> urlQueue = new LinkedList<>();
    private int crawledCount = 0;
    private String crawlBaseDomain = "";
    private String crawlSaveDir = "";
    private int crawlMaxDepth = 3;
    private int crawlDelay = 1000;
    private boolean isCrawling = false;
    
    private SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private Map<String, String> headers;
    
    private AlertDialog currentDialog;
    
    // BroadcastReceiver 引用（用于在 onDestroy 中注销）
    private BroadcastReceiver logReceiver;
    private BroadcastReceiver titleHideReceiver;
    private BroadcastReceiver toolboxReceiver;
    private BroadcastReceiver colorChangeReceiver;
    

    private LinearLayout historyPage;
    private ListView historyListView;
    private ArrayAdapter<String> historyPageAdapter;
    
    private String currentXfUrl = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 日志设置广播
        logReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.xuanfeng.browser.LOG_SETTING_CHANGED".equals(intent.getAction())) {
                    alwaysLog = intent.getBooleanExtra("enabled", false);
                    startLogCapture();
                }
            }
        };
        registerReceiver(logReceiver, new IntentFilter("com.xuanfeng.browser.LOG_SETTING_CHANGED"));
        
        // 工具箱广播
        toolboxReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.xuanfeng.browser.TOOLBOX_ACTION".equals(intent.getAction())) {
                    String action = intent.getStringExtra("action");
                    if ("EDIT_SOURCE".equals(action)) {
                        showEditSourceDialog();
                    } else if ("FIND_IN_PAGE".equals(action)) {
                        toggleSearchFloat();
                    } else if ("CRAWL".equals(action)) {
                        showCrawlDialog();
                    }
                }
            }
        };
        registerReceiver(toolboxReceiver, new IntentFilter("com.xuanfeng.browser.TOOLBOX_ACTION"));
        
        // 初始化
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        alwaysLog = prefs.getBoolean("always_log", false);
        urlMode = prefs.getString("url_mode", "always");
        
        // 标题隐藏广播
        titleHideReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.xuanfeng.browser.TITLE_HIDE_CHANGED".equals(intent.getAction())) {
                    boolean hide = intent.getBooleanExtra("hide", false);
                    TextView tvTitle = findViewById(R.id.tv_title);
                    if (tvTitle != null) {
                        tvTitle.setVisibility(hide ? View.GONE : View.VISIBLE);
                    }
                }
            }
        };
        registerReceiver(titleHideReceiver, new IntentFilter("com.xuanfeng.browser.TITLE_HIDE_CHANGED"));
        
        // 修复隐藏标题重启失效
        try {
            boolean hideTitleSetting = prefs.getBoolean("hide_title", false);
            TextView titleView = findViewById(R.id.tv_title);
            if (titleView != null) {
                titleView.setVisibility(hideTitleSetting ? View.GONE : View.VISIBLE);
            }
        } catch (Exception e) {
            // 忽略，不影响主流程
        }
        
        // errorlog
        startService(new Intent(this, CrashService.class));
        
        initViews();
        
        // 颜色设置广播与应用
        colorChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && action.endsWith("_CHANGED")) {
                    applyColors();
                }
            }
        };
        IntentFilter colorFilter = new IntentFilter();
        colorFilter.addAction("com.xuanfeng.browser.XF_PRIMARY_CHANGED");
        colorFilter.addAction("com.xuanfeng.browser.XF_BACKGROUND_CHANGED");
        colorFilter.addAction("com.xuanfeng.browser.XF_TOOLBAR_ICON_CHANGED");
        registerReceiver(colorChangeReceiver, colorFilter);
        applyColors();
        
        setupWebView();
        setupListeners();
        
        // 强关同步存设置
        safeBrowsingEnabled = false;
        prefs.edit().putBoolean("safe_browsing_enabled", false).apply();
        
        loadWithSafeBrowsing("https://xfbrowser.fwh.is/home.html");
        
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null && "xuanfeng".equals(data.getScheme())) {
                String url = data.toString();
                handleXuanfengUrl(url);
            }
        }

    }
    
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        safeBrowsingEnabled = prefs.getBoolean("safe_browsing_enabled", false);
        applyColors(); // 从设置返回时刷新颜色
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logReceiver != null) {
            unregisterReceiver(logReceiver);
        }
        if (titleHideReceiver != null) {
            unregisterReceiver(titleHideReceiver);
        }
        if (toolboxReceiver != null) {
            unregisterReceiver(toolboxReceiver);
        }
        if (colorChangeReceiver != null) {
            unregisterReceiver(colorChangeReceiver);
        }
    }

    private void initViews() {
    webView = findViewById(R.id.webview);
    etUrl = findViewById(R.id.et_url);
    
    
    btnBack = findViewById(R.id.btn_back);
    btnForward = findViewById(R.id.btn_forward);
    btnRefresh = findViewById(R.id.btn_refresh);
    btnDesktop = findViewById(R.id.btn_desktop);
    btnSettings = findViewById(R.id.btn_settings);
    btnMenu = findViewById(R.id.btn_toolbox);
    btnTabs = findViewById(R.id.btn_tabs);
    tvTabCount = findViewById(R.id.tv_tab_count);
    webviewContainer = findViewById(R.id.webview_container);
    progressBar = findViewById(R.id.progress_bar);
    
historyPage = findViewById(R.id.history_page);
historyListView = findViewById(R.id.history_list_view);

etUrl.setOnFocusChangeListener((v, hasFocus) -> {
boolean selectAllOnFocus = prefs.getBoolean("select_all_on_focus", true);

if (hasFocus) {
    String url = getCurrentWebView().getUrl();
    if (url != null && !url.isEmpty()) {
        etUrl.setText(url);
        if (selectAllOnFocus) {
            etUrl.dismissDropDown();
            etUrl.post(() -> etUrl.selectAll());
        }
    }
    refreshHistoryPage(url);
    historyPage.setVisibility(View.VISIBLE);
} else {
    String title = getCurrentWebView().getTitle();
    if (title != null && !title.isEmpty()) {
        etUrl.setText(title);
    } else {
        etUrl.setText(getCurrentWebView().getUrl());
    }
    historyPage.setVisibility(View.GONE);
}
});

etUrl.addTextChangedListener(new TextWatcher() {
@Override
public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
@Override
public void onTextChanged(CharSequence s, int start, int before, int count) {}
@Override
public void afterTextChanged(Editable s) {
    if (etUrl.hasFocus()) {
        refreshHistoryPage(s.toString());
    }
}
});

historyPageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
historyListView.setAdapter(historyPageAdapter);

historyListView.setOnItemClickListener((parent, view, position, id) -> {
    String item = historyPageAdapter.getItem(position);
    
    if (item.startsWith("使用 ") && item.contains(" 搜索:")) {
        // 搜索
        String searchKeyword = item.substring(item.indexOf("搜索:") + 3);
        try {
            String encoded = URLEncoder.encode(searchKeyword, "UTF-8");
            loadWithSafeBrowsing(getSearchUrl(encoded));
        } catch (Exception e) {}
    } else if (item.equals("强制作为网址访问")) {
        String input = etUrl.getText().toString().trim();
        String url = input.startsWith("http") ? input : "https://" + input;
        loadWithSafeBrowsing(url);
    } else if (item.startsWith("访问 ")) {
        String url = item.substring(3, item.length() - 2);
        loadWithSafeBrowsing(url);
    } else if (item.startsWith("使用 Xuanfeng Network 访问: ")) {
        String xfInput = item.substring(27);
        handleXuanfengUrl("xuanfeng://" + xfInput);
    } else if (item.startsWith("xuanfeng://")) {
        handleXuanfengUrl(item);
    } else if (!"暂无历史记录".equals(item)) {
        // 普通历史记录
        String networkMode = getCurrentNetworkMode();
        if ("xf".equals(networkMode)) {
            handleXuanfengUrl("xuanfeng://" + item);
        } else {
            loadWithSafeBrowsing(item);
        }
    }
    
    historyPage.setVisibility(View.GONE);
    etUrl.clearFocus();
});



}
private String getSearchUrl(String encoded) {
    SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
    String engine = prefs.getString("search_engine", "baidu");
    switch (engine) {
        case "baidu": return "https://www.baidu.com/s?wd=" + encoded;
        case "google": return "https://www.google.com/search?q=" + encoded;
        case "bing": return "https://www.bing.com/search?q=" + encoded;
        case "duckduckgo": return "https://duckduckgo.com/?q=" + encoded;
        case "sogou": return "https://www.sogou.com/web?query=" + encoded;
        case "360": return "https://www.so.com/s?q=" + encoded;
        default: return "https://www.baidu.com/s?wd=" + encoded;
    }
}

private String getCurrentNetworkMode() {
    SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
    return prefs.getString("network_mode", "internet");
}


private void refreshHistoryPage(String input) {
    List<String> allHistory = HistoryManager.getInstance(this).getHistoryList();
    List<String> filteredHistory = new ArrayList<>();
    
    if (input == null || input.isEmpty()) {
        filteredHistory.addAll(allHistory);
    } else {
        for (String url : allHistory) {
            if (url.toLowerCase().contains(input.toLowerCase())) {
                filteredHistory.add(url);
            }
        }
    }
    
    historyPageAdapter.clear();
    
    String networkMode = getCurrentNetworkMode();
    
    // 根据网络模式显示不同的提示
    if (input != null && !input.isEmpty()) {
        if ("xf".equals(networkMode)) {
            // Xuanfeng 模式：显示 xuanfeng:// 格式
            historyPageAdapter.add("使用 Xuanfeng Network 访问: " + input);
            historyPageAdapter.add("强制作为网址访问");
        } else {
            boolean isUrl = input.matches("^(https?://)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(/.*)?$") ||
                            input.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+.*$");
            if (isUrl) {
                String url = input.startsWith("http") ? input : "https://" + input;
                historyPageAdapter.add("访问 " + url + " →");
            } else {
                SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
                String engine = prefs.getString("search_engine", "baidu");
                historyPageAdapter.add("使用 " + getEngineName(engine) + " 搜索: " + input);
                historyPageAdapter.add("强制作为网址访问");
            }
        }
    }
    
    // 添加历史记录
    for (String url : filteredHistory) {
        if ("xf".equals(networkMode)) {
            // Xuanfeng 模式：历史记录转换为 xuanfeng:// 格式
            String displayUrl = url;
            if (displayUrl.startsWith("http://") || displayUrl.startsWith("https://")) {
                displayUrl = "xuanfeng://" + displayUrl.replace("http://", "").replace("https://", "");
            }
            historyPageAdapter.add(displayUrl);
        } else {
            historyPageAdapter.add(url);
        }
    }
    
    if (historyPageAdapter.getCount() == 0) {
        historyPageAdapter.add("暂无历史记录");
    }
    
    historyPageAdapter.notifyDataSetChanged();
}
private String getEngineName(String engineValue) {
    switch (engineValue) {
        case "baidu": return "百度";
        case "google": return "Google";
        case "bing": return "Bing";
        case "duckduckgo": return "DuckDuckGo";
        case "sogou": return "搜狗";
        case "360": return "360";
        default: return "百度";
    }
}

    private WebView getCurrentWebView() {
        return webViewList.get(currentTabIndex);
    }
    
    private void applyTheme(String theme) {
        View rootView = getWindow().getDecorView();
        switch(theme) {
            case "light":
                rootView.setBackgroundColor(0xFFFFFFFF);
                etUrl.setBackgroundColor(0xFFEEEEEE);
                etUrl.setTextColor(0xFF000000);
                etUrl.setHintTextColor(0xFF666666);
                getCurrentWebView().evaluateJavascript(
                    "document.body.style.backgroundColor='#FFFFFF';" +
                    "document.body.style.color='#000000';", null);
                break;
            case "dark":
                rootView.setBackgroundColor(0xFF000000);
                etUrl.setBackgroundColor(0xFF222222);
                etUrl.setTextColor(0xFF00FF00);
                etUrl.setHintTextColor(0xFF666666);
                getCurrentWebView().evaluateJavascript(
                    "document.body.style.backgroundColor='#000000';" +
                    "document.body.style.color='#00FF00';", null);
                break;
            case "system":
            default:
                rootView.setBackgroundColor(0xFFF5F5F5);
                etUrl.setBackgroundColor(0xFFFFFFFF);
                etUrl.setTextColor(0xFF000000);
                etUrl.setHintTextColor(0xFF666666);
                getCurrentWebView().evaluateJavascript(
                    "document.body.style.backgroundColor='';" +
                    "document.body.style.color='';", null);
                break;
        }
    }
    
    private void updateTabCount() {
        if (tvTabCount != null) {
            tvTabCount.setText(String.valueOf(webViewList.size()));
        }
    }
    
    private void addNewTab(String url) {
        WebView newWebView = new WebView(this);
        webviewContainer.addView(newWebView);
        WebSettings settings = newWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUserAgentString(webView.getSettings().getUserAgentString());
        settings.setDomStorageEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        
        newWebView.setVisibility(View.GONE);
        webViewList.add(newWebView);
        
        if (url != null) {
            if (safeBrowsingEnabled) {
                String proxyUrl;
                try {
                    proxyUrl = "http://154.12.55.135:12316/?url=" + URLEncoder.encode(url, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    proxyUrl = url;
                }
                newWebView.loadUrl(proxyUrl);
            } else {
                newWebView.loadUrl(url);
            }
        }
        updateTabCount();
    }
    
    private void switchToTab(int index) {
        if (index < 0 || index >= webViewList.size()) return;
        webViewList.get(currentTabIndex).setVisibility(View.GONE);
        webViewList.get(index).setVisibility(View.VISIBLE);
        currentTabIndex = index;
        WebView current = getCurrentWebView();
        
        if ("always".equals(urlMode)) {
            etUrl.setText(getDisplayUrl(current.getUrl()));
        } else {
            String title = current.getTitle();
            if (title != null && !title.isEmpty()) {
                etUrl.setText(title);
            } else {
                etUrl.setText(getDisplayUrl(current.getUrl()));
            }
        }
        btnBack.setEnabled(current.canGoBack());
        btnForward.setEnabled(current.canGoForward());
    }
    
    private void closeTab(int index) {
        webviewContainer.removeView(webViewList.get(index));
        if (webViewList.size() <= 1) return;
        if (index == currentTabIndex) {
            int newIndex = (index > 0) ? index - 1 : index + 1;
            switchToTab(newIndex);
        }
        webviewContainer.removeView(webViewList.get(index));
        webViewList.remove(index);
        if (index < currentTabIndex) {
            currentTabIndex--;
        }
        updateTabCount();
    }
    
    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        
        String ua = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";
        webSettings.setUserAgentString(ua);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setGeolocationEnabled(true);
        // setAppCacheEnabled was removed in newer Android WebView, cache mode is already set above
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setBlockNetworkImage(false);
        webSettings.setBlockNetworkLoads(false);
        
        headers = new HashMap<>();
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Connection", "keep-alive");
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("Sec-Fetch-Dest", "document");
        headers.put("Sec-Fetch-Mode", "navigate");
        headers.put("Sec-Fetch-Site", "none");
        headers.put("Sec-Fetch-User", "?1");
        headers.put("Cache-Control", "max-age=0");
        
        webView.setWebViewClient(new WebViewClient() {
@Override
public boolean shouldOverrideUrlLoading(WebView view, String url) {
    String networkMode = getCurrentNetworkMode();
    
    // Xuanfeng Network 模式：所有非 http/https 请求都走 XF 协议
    if ("xf".equals(networkMode)) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            handleXuanfengUrl("xuanfeng://" + url);
            return true;
        }
    }
    
    // 处理 xuanfeng:// 协议
    if (url.startsWith("xuanfeng://")) {
        handleXuanfengUrl(url);
        return true;
    }
    
    // 正常的 http/https
    if (url.startsWith("http://") || url.startsWith("https://")) {
        if (safeBrowsingEnabled) {
            runOnUiThread(() -> etUrl.setText(getDisplayUrl(url)));
        }
        view.loadUrl(url, headers);
        return true;
    }
    
    // 其他协议交给系统
    try {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    } catch (Exception e) {}
    return true;
}
            
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (safeBrowsingEnabled) {
                    etUrl.setText(getDisplayUrl(url));
                }
            }
            
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return super.shouldInterceptRequest(view, request);
            }
            
            @Override
public void onPageFinished(WebView view, String url) {
    super.onPageFinished(view, url);
    btnRefresh.setImageResource(R.drawable.ic_refresh);
    // 网址栏有焦点时不更新
    if (etUrl.hasFocus()) {
        btnBack.setEnabled(view.canGoBack());
        btnForward.setEnabled(view.canGoForward());
        progressBar.setVisibility(View.GONE);
        return;
    }
    
    if ("always".equals(MainActivity.this.urlMode)) {
        etUrl.setText(getDisplayUrl(url));
    } else {
        String title = view.getTitle();
        if (title != null && !title.isEmpty()) {
            etUrl.setText(title);
        } else {
            etUrl.setText(getDisplayUrl(url));
        }
    }
    
    btnBack.setEnabled(view.canGoBack());
    btnForward.setEnabled(view.canGoForward());
    progressBar.setVisibility(View.GONE);
    
    if (isCrawling) {
//        saveCurrentPageForCrawl(url);
    }
}
            /*
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
    // 只处理 http/https 的错误
    if (failingUrl != null && (failingUrl.startsWith("http://") || failingUrl.startsWith("https://"))) {
        String customHtml = getCustomErrorPage(failingUrl, errorCode);
        if (customHtml != null) {
            view.loadDataWithBaseURL(null, customHtml, "text/html", "UTF-8", null);
        }
    }
}
*/
        });
        
        webViewList.add(webView);
        updateTabCount();
        
        webView.setWebChromeClient(new WebChromeClient() {
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        progressBar.setProgress(newProgress);
        if (newProgress < 100) {
            progressBar.setVisibility(View.VISIBLE);
            btnRefresh.setImageResource(R.drawable.ic_stop);
        } else {
            progressBar.setVisibility(View.GONE);
            btnRefresh.setImageResource(R.drawable.ic_refresh);
        }
    }
    
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        new AlertDialog.Builder(MainActivity.this)
            .setTitle("上传文件")
            .setMessage("允许此网页选择文件吗？")
            .setPositiveButton("选择文件", (dialog, which) -> {
                uploadMessage = filePathCallback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(Intent.createChooser(intent, "选择文件"), FILE_CHOOSER_RESULT_CODE);
            })
            .setNegativeButton("取消", (dialog, which) -> {
                filePathCallback.onReceiveValue(null);
            })
            .show();
        return true;
    }
});
        
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void showSource(String html) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(MainActivity.this)
                            .setTitle("源码")
                            .setMessage(html)
                            .setPositiveButton("确定", null)
                            .setNegativeButton("复制", (dialog, which) -> {
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("source", html);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(MainActivity.this, "已复制", Toast.LENGTH_SHORT).show();
                            })
                            .show();
                    }
                });
            }
            
            @JavascriptInterface
            public void downloadSource(String domain, String html) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) 
                                + "/xfbrowser/网页源码/" + domain + "/";
                            File dir = new File(dirPath);
                            if (!dir.exists()) dir.mkdirs();
                            String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".html";
                            File file = new File(dir, fileName);
                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write(html.getBytes("UTF-8"));
                            fos.close();
                            Toast.makeText(MainActivity.this, "源码已保存到: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }, "XF");
        
        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            String fileName = getFileName(url, contentDisposition);
            downloadFile(url, fileName);
        });
    }
    
    private String getDisplayUrl(String url) {
        if (safeBrowsingEnabled && url != null) {
            if (url.startsWith("http://154.12.55.135:12316/?url=")) {
                String encoded = url.substring("http://154.12.55.135:12316/?url=".length());
                try {
                    return URLDecoder.decode(encoded, "UTF-8");
                } catch (Exception e) {
                    return url;
                }
            }
        }
        return url;
    }
    
    private String getFileName(String url, String contentDisposition) {
        if (contentDisposition != null) {
            String[] parts = contentDisposition.split("filename=");
            if (parts.length > 1) {
                String name = parts[1].replace("\"", "").split(";")[0];
                if (!name.isEmpty()) return name;
            }
        }
        try {
            String name = url.substring(url.lastIndexOf("/") + 1);
            if (name.contains("?")) name = name.substring(0, name.indexOf("?"));
            return java.net.URLDecoder.decode(name, "UTF-8");
        } catch (Exception e) {
            return "download_" + System.currentTimeMillis();
        }
    }
    
    private void downloadFile(String url, String fileName) {
        runOnUiThread(() -> {
            new AlertDialog.Builder(MainActivity.this)
                .setTitle("下载文件")
                .setMessage("文件名: " + fileName + "\n确定要下载吗？")
                .setPositiveButton("下载", (dialog, which) -> {
                    startDownload(url, fileName);
                })
                .setNegativeButton("取消", null)
                .show();
        });
    }
    
    private void startDownload(String url, String fileName) {
        String userAgent = webView.getSettings().getUserAgentString();
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("HEAD");
                conn.setInstanceFollowRedirects(false);
                conn.setRequestProperty("User-Agent", userAgent);
                conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
                conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
                conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
                conn.setRequestProperty("Connection", "keep-alive");
                conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
                conn.setRequestProperty("Sec-Fetch-Dest", "document");
                conn.setRequestProperty("Sec-Fetch-Mode", "navigate");
                conn.setRequestProperty("Sec-Fetch-Site", "none");
                conn.setRequestProperty("Sec-Fetch-User", "?1");
                conn.setRequestProperty("Cache-Control", "max-age=0");
                
                android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
                String cookie = cookieManager.getCookie(url);
                if (cookie != null) {
                    conn.setRequestProperty("Cookie", cookie);
                }
                conn.connect();
                String contentType = conn.getContentType();
                int responseCode = conn.getResponseCode();
                
                if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || 
                    responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
                    String newUrl = conn.getHeaderField("Location");
                    if (newUrl != null) {
                        String finalNewUrl = newUrl;
                        runOnUiThread(() -> {
                            webView.loadUrl(finalNewUrl);
                        });
                        return;
                    }
                }
                
                if (contentType != null && contentType.contains("text/html")) {
                    runOnUiThread(() -> {
                        webView.loadUrl(url);
                    });
                    return;
                }
                
                runOnUiThread(() -> {
                    try {
                        String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) 
                            + "/xfbrowser/下载/";
                        File dir = new File(dirPath);
                        if (!dir.exists()) dir.mkdirs();
                        File downloadFile = new File(dir, fileName);
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.setDestinationUri(Uri.fromFile(new File(dir, fileName)));
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setAllowedOverMetered(true);
                        request.setAllowedOverRoaming(true);
                        request.addRequestHeader("User-Agent", userAgent);
                        request.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
                        request.addRequestHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
                        request.addRequestHeader("Accept-Encoding", "gzip, deflate, br");
                        request.addRequestHeader("Connection", "keep-alive");
                        request.addRequestHeader("Upgrade-Insecure-Requests", "1");
                        request.addRequestHeader("Sec-Fetch-Dest", "document");
                        request.addRequestHeader("Sec-Fetch-Mode", "navigate");
                        request.addRequestHeader("Sec-Fetch-Site", "none");
                        request.addRequestHeader("Sec-Fetch-User", "?1");
                        request.addRequestHeader("Cache-Control", "max-age=0");
                        if (cookie != null) {
                            request.addRequestHeader("Cookie", cookie);
                        }
                        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);
                        Toast.makeText(MainActivity.this, "开始下载: " + fileName, Toast.LENGTH_SHORT).show();
                        com.xuanfeng.browser.DownloadManager.getInstance(this).addDownloadRecord(
    url, fileName, downloadFile.getAbsolutePath(), downloadFile.length());
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "下载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "下载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private void setupListeners() {
        etUrl.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    String input = etUrl.getText().toString().trim();
                    handleUrlInput(input);
                    return true;
                }
                return false;
            }
        });
        
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCurrentWebView().canGoBack()) getCurrentWebView().goBack();
            }
        });
        
        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCurrentWebView().canGoForward()) getCurrentWebView().goForward();
            }
        });
        
        btnRefresh.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if (getCurrentWebView() != null && progressBar.getVisibility() == View.VISIBLE) {
            getCurrentWebView().stopLoading();
            progressBar.setVisibility(View.GONE);
            btnRefresh.setImageResource(R.drawable.ic_refresh);
        } else {
            getCurrentWebView().reload();
            btnRefresh.setImageResource(R.drawable.ic_stop);
        }
    }
});
        
        btnDesktop.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        String current = webView.getSettings().getUserAgentString();
        
        if (current.contains("Windows NT") || current.contains("Mac OS") || current.contains("X11")) {
            //PC-PE
            String mobileUA = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";
            getCurrentWebView().getSettings().setUserAgentString(mobileUA);
            btnDesktop.setImageResource(R.drawable.ic_mobile);
        } else {
            //PE-PC
            String desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0";
            getCurrentWebView().getSettings().setUserAgentString(desktopUA);
            btnDesktop.setImageResource(R.drawable.ic_desktop);
        }
        getCurrentWebView().reload();
    }
});
        
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ToolboxActivity.class);
                startActivity(intent);
            }
        });
        
        btnTabs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TabsActivity.class);
                String[] titles = new String[webViewList.size()];
                String[] urls = new String[webViewList.size()];
                for (int i = 0; i < webViewList.size(); i++) {
                    WebView wv = webViewList.get(i);
                    titles[i] = wv.getTitle() != null ? wv.getTitle() : "新标签页";
                    urls[i] = wv.getUrl() != null ? wv.getUrl() : "";
                }
                intent.putExtra("titles", titles);
                intent.putExtra("urls", urls);
                intent.putExtra("current", currentTabIndex);
                startActivityForResult(intent, 1001);
            }
        });
    }
    
    private void handleUrlInput(String input) {
    if (input.isEmpty()) return;
    
    String networkMode = getCurrentNetworkMode();
    
    // Xuanfeng Network 模式：所有输入都走 XF 协议
    if ("xf".equals(networkMode)) {
        handleXuanfengUrl("xuanfeng://" + input);
        return;
    }
    
    // Internet 模式：原有逻辑
    if (input.matches("^(https?://)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(/.*)?$") || 
        input.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+.*$")) {
        String url = input;
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }
        HistoryManager.getInstance(this).addHistory(url);
        etUrl.clearFocus();
        historyPage.setVisibility(View.GONE);
        loadWithSafeBrowsing(url);
        return;
    }
    
    // 搜索
    SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
    String engine = prefs.getString("search_engine", "baidu");
    try {
        String encoded = URLEncoder.encode(input, "UTF-8");
        String searchUrl = "";
        switch (engine) {
            case "baidu": searchUrl = "https://www.baidu.com/s?wd=" + encoded; break;
            case "google": searchUrl = "https://www.google.com/search?q=" + encoded; break;
            case "bing": searchUrl = "https://www.bing.com/search?q=" + encoded; break;
            case "duckduckgo": searchUrl = "https://duckduckgo.com/?q=" + encoded; break;
            case "sogou": searchUrl = "https://www.sogou.com/web?query=" + encoded; break;
            case "360": searchUrl = "https://www.so.com/s?q=" + encoded; break;
            default: searchUrl = "https://www.baidu.com/s?wd=" + encoded;
        }
        loadWithSafeBrowsing(searchUrl);
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    //更多
    private void showMenuDialog() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        
        // 构建图标+文字的自定义列表
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 8, 16, 8);
        
        String[][] menuItems = {
            {"自动刷新", prefs.getBoolean("auto_refresh", true) ? "开" : "关", "ic_settings"},
            {"阻止音视频自动播放", prefs.getBoolean("block_autoplay", false) ? "开" : "关", "ic_stop"},
            {"文本模式", prefs.getBoolean("text_mode", false) ? "开" : "关", "ic_source"},
            {"禁用JavaScript", prefs.getBoolean("js_disabled", false) ? "开" : "关", "ic_close"},
            {"编辑网页源码", "", "ic_source"},
            {"页内查找", "", "ic_find_in_page"},
            {"爬取", "", "ic_crawl"},
            {"下载管理", "", "ic_download_mgr"},
            {"PingHub", "", "ic_ping"}
        };
        
        for (int i = 0; i < menuItems.length; i++) {
            String label = menuItems[i][0];
            String status = menuItems[i][1];
            String icon = menuItems[i][2];
            
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(8, 12, 8, 12);
            row.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            row.setClickable(true);
            row.setFocusable(true);
            row.setBackgroundResource(android.R.drawable.list_selector_background);
            
            // Icon
            ImageView iconView = new ImageView(this);
            int iconRes = getResources().getIdentifier(icon, "drawable", getPackageName());
            if (iconRes != 0) iconView.setImageResource(iconRes);
            iconView.setColorFilter(0xFF757575);
            iconView.setLayoutParams(new LinearLayout.LayoutParams(40, 40));
            iconView.setPadding(0, 0, 16, 0);
            row.addView(iconView);
            
            // Label
            TextView labelView = new TextView(this);
            labelView.setText(label);
            labelView.setTextSize(16);
            labelView.setTextColor(0xFF212121);
            labelView.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
            row.addView(labelView);
            
            // Status toggle
            if (!status.isEmpty()) {
                TextView statusView = new TextView(this);
                statusView.setText(status);
                statusView.setTextSize(14);
                statusView.setTextColor(0xFF1565C0);
                statusView.setPadding(8, 0, 0, 0);
                row.addView(statusView);
            } else {
                // 右箭头
                ImageView arrowView = new ImageView(this);
                arrowView.setImageResource(R.drawable.ic_forward);
                arrowView.setColorFilter(0xFFBDBDBD);
                arrowView.setLayoutParams(new LinearLayout.LayoutParams(24, 24));
                row.addView(arrowView);
            }
            
            final int index = i;
            row.setOnClickListener(v -> {
                switch (index) {
                    case 0: {
                        boolean auto = !prefs.getBoolean("auto_refresh", true);
                        prefs.edit().putBoolean("auto_refresh", auto).apply();
                        break;
                    }
                    case 1: {
                        boolean blockAutoplay = !prefs.getBoolean("block_autoplay", false);
                        prefs.edit().putBoolean("block_autoplay", blockAutoplay).apply();
                        setBlockAutoplay(blockAutoplay);
                        Toast.makeText(MainActivity.this, "阻止音视频自动播放:" + (blockAutoplay ? "开" : "关"), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case 2: {
                        boolean text = !prefs.getBoolean("text_mode", false);
                        prefs.edit().putBoolean("text_mode", text).apply();
                        setTextMode(text);
                        break;
                    }
                    case 3: {
                        boolean js = !prefs.getBoolean("js_disabled", false);
                        prefs.edit().putBoolean("js_disabled", js).apply();
                        setJavaScriptEnabled(!js);
                        break;
                    }
                    case 4:
                        showEditSourceDialog();
                        break;
                    case 5:
                        toggleSearchFloat();
                        break;
                    case 6:
                        showCrawlDialog();
                        break;
                    case 7: {
                        Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case 8: {
                        Intent intent = new Intent(MainActivity.this, ProtocolConnectActivity.class);
                        startActivity(intent);
                        break;
                    }
                }
            });
            
            layout.addView(row);
            
            // 分隔线
            if (i < menuItems.length - 1) {
                View divider = new View(this);
                divider.setBackgroundColor(0xFFE0E0E0);
                divider.setLayoutParams(new LinearLayout.LayoutParams(-1, 1));
                divider.setPadding(48, 0, 0, 0);
                layout.addView(divider);
            }
        }
        
        new AlertDialog.Builder(MainActivity.this)
            .setTitle("更多")
            .setView(layout)
            .setNegativeButton("关闭", null)
            .show();
    }
    
    private void showCrawlDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 8, 16, 8);
        
        String[][] items = {
            {"查看网页源码", "ic_source"},
            {"下载网页源码", "ic_download_mgr"},
            {"递归爬取网站", "ic_crawl"}
        };
        
        for (int i = 0; i < items.length; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(12, 16, 12, 16);
            row.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            row.setClickable(true);
            row.setFocusable(true);
            row.setBackgroundResource(android.R.drawable.list_selector_background);
            
            ImageView iconView = new ImageView(this);
            int iconRes = getResources().getIdentifier(items[i][1], "drawable", getPackageName());
            if (iconRes != 0) iconView.setImageResource(iconRes);
            iconView.setColorFilter(0xFF757575);
            iconView.setLayoutParams(new LinearLayout.LayoutParams(36, 36));
            iconView.setPadding(0, 0, 16, 0);
            row.addView(iconView);
            
            TextView labelView = new TextView(this);
            labelView.setText(items[i][0]);
            labelView.setTextSize(16);
            labelView.setTextColor(0xFF212121);
            labelView.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
            row.addView(labelView);
            
            final int index = i;
            row.setOnClickListener(v -> {
                switch (index) {
                    case 0:
                        loadWithSafeBrowsing("javascript:XF.showSource(document.documentElement.outerHTML);");
                        break;
                    case 1:
                        String url = getCurrentWebView().getUrl();
                        String domain = "unknown";
                        try {
                            domain = new java.net.URL(url).getHost().replace("www.", "");
                        } catch (Exception e) {}
                        loadWithSafeBrowsing("javascript:XF.downloadSource('" + domain + "', document.documentElement.outerHTML);");
                        break;
                    case 2:
                        showCrawlSettingsDialog();
                        break;
                }
            });
            layout.addView(row);
            
            if (i < items.length - 1) {
                View divider = new View(this);
                divider.setBackgroundColor(0xFFE0E0E0);
                divider.setLayoutParams(new LinearLayout.LayoutParams(-1, 1));
                divider.setPadding(52, 0, 0, 0);
                layout.addView(divider);
            }
        }
        
        new AlertDialog.Builder(MainActivity.this)
            .setTitle("爬取")
            .setView(layout)
            .setNegativeButton("返回", null)
            .show();
    }
    
    private void showCrawlSettingsDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("爬取网站设置");
    
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(50, 30, 50, 30);
    
    String currentUrl = getCurrentWebView().getUrl();
    if (currentUrl == null) currentUrl = "";
    TextView currentUrlLabel = new TextView(this);
    currentUrlLabel.setText("目标网站: " + currentUrl);
    currentUrlLabel.setTextSize(14);
    currentUrlLabel.setPadding(0, 0, 0, 20);
    layout.addView(currentUrlLabel);
    
    // 爬取深度
    LinearLayout depthLayout = new LinearLayout(this);
    depthLayout.setOrientation(LinearLayout.HORIZONTAL);
    depthLayout.setPadding(0, 10, 0, 10);
    TextView depthLabel = new TextView(this);
    depthLabel.setText("爬取深度: ");
    depthLabel.setTextSize(14);
    depthLayout.addView(depthLabel);
    EditText depthInput = new EditText(this);
    depthInput.setHint("留空=最深");
    depthInput.setText("");
    depthInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
    depthInput.setLayoutParams(new LinearLayout.LayoutParams(200, -2));
    depthLayout.addView(depthInput);
    layout.addView(depthLayout);
    
    // 请求延迟
    LinearLayout delayLayout = new LinearLayout(this);
    delayLayout.setOrientation(LinearLayout.HORIZONTAL);
    delayLayout.setPadding(0, 10, 0, 10);
    TextView delayLabel = new TextView(this);
    delayLabel.setText("延迟(ms): ");
    delayLabel.setTextSize(14);
    delayLayout.addView(delayLabel);
    EditText delayInput = new EditText(this);
    delayInput.setHint("0=无延迟, 建议≥500");
    delayInput.setText("1000");
    delayInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
    delayInput.setLayoutParams(new LinearLayout.LayoutParams(250, -2));
    delayLayout.addView(delayInput);
    layout.addView(delayLayout);
    
    layout.addView(createDivider());
    
    // 仅限当前域名
    CheckBox chkSameDomain = new CheckBox(this);
    chkSameDomain.setText("仅限当前域名");
    chkSameDomain.setChecked(true);
    layout.addView(chkSameDomain);
    
    // 使用浏览器头
    CheckBox chkUseBrowserHeader = new CheckBox(this);
    chkUseBrowserHeader.setText("使用XFbrowser浏览器头");
    chkUseBrowserHeader.setChecked(true);
    layout.addView(chkUseBrowserHeader);
    
    // 续爬
    CheckBox chkResume = new CheckBox(this);
    chkResume.setText("续爬（跳过已爬取的页面）");
    chkResume.setChecked(false);
    layout.addView(chkResume);
    
    layout.addView(createDivider());
    
    // 白名单
    TextView whitelistLabel = new TextView(this);
    whitelistLabel.setText("下载白名单（支持通配符 * ?，留空=不限）:");
    whitelistLabel.setTextSize(12);
    whitelistLabel.setTextColor(0xFF888888);
    layout.addView(whitelistLabel);
    EditText whitelistInput = new EditText(this);
    whitelistInput.setHint("例: *.css,*.js,*.png");
    whitelistInput.setText("");
    whitelistInput.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
    layout.addView(whitelistInput);
    
    // 黑名单
    TextView blacklistLabel = new TextView(this);
    blacklistLabel.setText("下载黑名单（与白名单互斥）:");
    blacklistLabel.setTextSize(12);
    blacklistLabel.setTextColor(0xFF888888);
    layout.addView(blacklistLabel);
    EditText blacklistInput = new EditText(this);
    blacklistInput.setHint("例: *.mp4,*.zip");
    blacklistInput.setText("");
    blacklistInput.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
    layout.addView(blacklistInput);
    
    layout.addView(createDivider());
    
    // 进程数
    LinearLayout threadLayout = new LinearLayout(this);
    threadLayout.setOrientation(LinearLayout.HORIZONTAL);
    threadLayout.setPadding(0, 10, 0, 10);
    TextView threadLabel = new TextView(this);
    threadLabel.setText("进程数: ");
    threadLabel.setTextSize(14);
    threadLayout.addView(threadLabel);
    EditText threadInput = new EditText(this);
    threadInput.setHint("留空=auto, 默认1");
    threadInput.setText("");
    threadInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
    threadInput.setLayoutParams(new LinearLayout.LayoutParams(200, -2));
    threadLayout.addView(threadInput);
    layout.addView(threadLayout);
    
    // 内存占用
    LinearLayout memLayout = new LinearLayout(this);
    memLayout.setOrientation(LinearLayout.HORIZONTAL);
    memLayout.setPadding(0, 10, 0, 10);
    TextView memLabel = new TextView(this);
    memLabel.setText("内存占用: ");
    memLabel.setTextSize(14);
    memLayout.addView(memLabel);
    EditText memInput = new EditText(this);
    memInput.setHint("留空=auto");
    memInput.setText("");
    memInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
    memInput.setLayoutParams(new LinearLayout.LayoutParams(200, -2));
    memLayout.addView(memInput);
    layout.addView(memLayout);
    
    builder.setView(layout);
    builder.setPositiveButton("开始爬取", (dialog, which) -> {
        String url = getCurrentWebView().getUrl();
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "请先打开要爬取的网站", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int depth;
        String depthText = depthInput.getText().toString().trim();
        depth = depthText.isEmpty() ? -1 : Integer.parseInt(depthText);
        
        int delay;
String delayText = delayInput.getText().toString().trim();
if (delayText.isEmpty()) {
    delay = 1000;
} else {
    delay = Integer.parseInt(delayText);
}

        
        // 低延迟警示
        if (delay <= 5) {
            new AlertDialog.Builder(this)
                .setTitle("警告")
                .setMessage("延迟过低可能对目标服务器造成较大压力，是否继续？")
                .setPositiveButton("确定继续", (d, w) -> {
                    startCrawl(url, depth, delay, chkSameDomain.isChecked(),
                        chkUseBrowserHeader.isChecked(), chkResume.isChecked(),
                        whitelistInput.getText().toString().trim(),
                        blacklistInput.getText().toString().trim(),
                        threadInput.getText().toString().trim(),
                        memInput.getText().toString().trim());
                })
                .setNegativeButton("取消", null)
                .show();
            return;
        }
        
        startCrawl(url, depth, delay, chkSameDomain.isChecked(),
            chkUseBrowserHeader.isChecked(), chkResume.isChecked(),
            whitelistInput.getText().toString().trim(),
            blacklistInput.getText().toString().trim(),
            threadInput.getText().toString().trim(),
            memInput.getText().toString().trim());
    });
    builder.setNegativeButton("取消", null);
    builder.show();
}

private View createDivider() {
    View v = new View(this);
    v.setBackgroundColor(0xFF444444);
    v.setLayoutParams(new LinearLayout.LayoutParams(-1, 1));
    v.setPadding(0, 10, 0, 10);
    return v;
}
    
    private void startCrawl(String startUrl, int maxDepth, int delay,
        boolean sameDomainOnly, boolean useBrowserHeader, boolean resume,
        String whitelist, String blacklist, String threadCountStr, String memLimitStr) {
    if (isCrawling) {
        Toast.makeText(this, "爬取进行中，请稍后", Toast.LENGTH_SHORT).show();
        return;
    }
    
    crawledUrls.clear();
    urlQueue.clear();
    crawledCount = 0;
    isCrawling = true;
    
    crawlMaxDepth = (maxDepth < 0) ? Integer.MAX_VALUE : maxDepth;
    crawlDelay = delay;
    crawlBaseDomain = "";
    
    try {
        java.net.URL url = new java.net.URL(startUrl);
        crawlBaseDomain = url.getHost();
    } catch (Exception e) {
        Toast.makeText(this, "无效的URL", Toast.LENGTH_SHORT).show();
        isCrawling = false;
        return;
    }
    
    String dateStr = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    String dirName = crawlBaseDomain.replace(".", "_") + "_" + dateStr;
    crawlSaveDir = Environment.getExternalStorageDirectory() + "/Download/xfbrowser/爬取网站/" + dirName + "/";
    File dir = new File(crawlSaveDir);
    if (!dir.exists()) dir.mkdirs();
    
    saveLogToFile("爬取启动", "目标: " + startUrl + " 域名: " + crawlBaseDomain +
        " 深度: " + (maxDepth < 0 ? "不限" : String.valueOf(maxDepth)) +
        " 延迟: " + delay + "ms" +
        " 仅限域名: " + sameDomainOnly +
        " 浏览器头: " + useBrowserHeader +
        " 续爬: " + resume +
        " 白名单: " + (whitelist.isEmpty() ? "无" : whitelist) +
        " 黑名单: " + (blacklist.isEmpty() ? "无" : blacklist) +
        " 进程数: " + (threadCountStr.isEmpty() ? "auto" : threadCountStr) +
        " 内存: " + (memLimitStr.isEmpty() ? "auto" : memLimitStr));
    
    Set<String> resumeUrls = new HashSet<>();
    if (resume) {
        resumeUrls = loadCrawledUrls(crawlSaveDir);
        crawledUrls.addAll(resumeUrls);
        saveLogToFile("续爬", "已加载 " + resumeUrls.size() + " 个已爬取URL");
    }
    
    int threadCount = calculateThreadCount(threadCountStr, memLimitStr);
    saveLogToFile("配置", "实际进程数: " + threadCount);
    
    final int fThreadCount = threadCount;
    final boolean fSameDomain = sameDomainOnly;
    final boolean fUseHeader = useBrowserHeader;
    final String fWhitelist = whitelist;
    final String fBlacklist = blacklist;
    final String fStartUrl = startUrl;
    
    new Thread(() -> bfsCrawlWebsite(fStartUrl, fThreadCount, fSameDomain, fUseHeader, fWhitelist, fBlacklist)).start();
}
    private void bfsCrawlWebsite(String startUrl, int threadCount,
        boolean sameDomainOnly, boolean useBrowserHeader,
        String whitelist, String blacklist) {
    Queue<String> queue = new LinkedList<>();
    Set<String> visited = new HashSet<>(crawledUrls);
    visited.add(startUrl);
    queue.add(startUrl);
    int currentDepth = 0;
    
    while (!queue.isEmpty() && isCrawling) {
        int levelSize = queue.size();
        if (currentDepth > crawlMaxDepth) {
            saveLogToFile("完成", "达到最大深度 " + crawlMaxDepth);
            break;
        }
        
        for (int i = 0; i < levelSize && isCrawling; i++) {
            String url = queue.poll();
            if (url == null) continue;
            
            try {
                String html = downloadPage(url, useBrowserHeader);
                if (html == null || html.isEmpty()) {
                    saveLogToFile("跳过", "无法下载: " + url);
                    continue;
                }
                
                saveCrawledPage(url, html);
                crawledCount++;
                
                final int count = crawledCount;
                saveLogToFile("爬取", "[" + count + "] " + url);
                
                List<String> links = parseLinks(html, url, sameDomainOnly);
                for (String link : links) {
                    if (!visited.contains(link) && !queue.contains(link)) {
                        visited.add(link);
                        queue.add(link);
                    }
                }
                
                if (crawlDelay > 0) {
                    Thread.sleep(crawlDelay);
                }
                
            } catch (Exception e) {
                saveLogToFile("错误", url + " - " + e.getMessage());
            }
        }
        currentDepth++;
    }
    
    saveCrawledUrls(crawlSaveDir, visited);
    isCrawling = false;
    
    final int totalCount = crawledCount;
    final String savePath = crawlSaveDir;
    runOnUiThread(() -> {
        Toast.makeText(MainActivity.this,
            "爬取完成! 共 " + totalCount + " 个页面\n保存路径: " + savePath, Toast.LENGTH_LONG).show();
    });
}

private String downloadPage(String url, boolean useBrowserHeader) {
    try {
        saveLogToFile("DEBUG", "开始连接: " + url);
        
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setInstanceFollowRedirects(true);
        
        if (useBrowserHeader) {
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.6422.165 Mobile Safari/537.36");
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
        } else {
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; XFCrawler/1.0)");
        }
        
        String cookie = CookieManager.getInstance().getCookie(url);
        if (cookie != null) {
            conn.setRequestProperty("Cookie", cookie);
        }
        
        saveLogToFile("DEBUG", "开始connect");
        conn.connect();
        saveLogToFile("DEBUG", "connect成功");
        
        int responseCode = conn.getResponseCode();
        saveLogToFile("DEBUG", "responseCode: " + responseCode);
        
        if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
            responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
            responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
            String newUrl = conn.getHeaderField("Location");
            saveLogToFile("DEBUG", "重定向到: " + newUrl);
            if (newUrl != null) {
                return downloadPage(newUrl, useBrowserHeader);
            }
        }
        
        if (responseCode != 200) {
            saveLogToFile("DEBUG", "非200响应码，return null");
            return null;
        }
        
        saveLogToFile("DEBUG", "开始读取输入流");
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        conn.disconnect();
        saveLogToFile("DEBUG", "下载完成，长度: " + sb.length());
        return sb.toString();
        
    } catch (Exception e) {
        saveLogToFile("DEBUG", "异常: " + e.getClass().getName() + " - " + e.getMessage());
        return null;
    }
}

private void saveCrawledPage(String url, String html) {
    try {
        String fileName = urlToFileName(url);
        String filePath = crawlSaveDir + fileName;
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        
        if (url.contains(".php") || url.contains(".asp") || url.contains(".jsp") || url.contains(".aspx")) {
            String ext = url.contains(".php") ? ".php" : url.contains(".asp") ? ".asp" : url.contains(".jsp") ? ".jsp" : ".aspx";
            if (!fileName.endsWith(ext)) {
                file = new File(filePath + ext);
            }
            FileWriter writer = new FileWriter(file);
            writer.write("<?php\n//无法爬取服务端内容，占位。\n?>\n");
            writer.write(html);
            writer.close();
        } else {
            if (!fileName.endsWith(".html")) {
                file = new File(filePath + ".html");
            }
            FileWriter writer = new FileWriter(file);
            writer.write(html);
            writer.close();
        }
    } catch (Exception e) {
        saveLogToFile("保存失败", url + " - " + e.getMessage());
    }
}

private List<String> parseLinks(String html, String baseUrl, boolean sameDomainOnly) {
    List<String> links = new ArrayList<>();
    try {
        java.net.URL base = new java.net.URL(baseUrl);
        String baseDomain = base.getHost();
        
        java.util.regex.Pattern hrefPattern = java.util.regex.Pattern.compile("href=\"([^\"]+)\"", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher hrefMatcher = hrefPattern.matcher(html);
        
        java.util.regex.Pattern hrefPattern2 = java.util.regex.Pattern.compile("href=\'([^\']+)\'", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher hrefMatcher2 = hrefPattern2.matcher(html);
        
        while (hrefMatcher.find()) {
            String link = processLink(hrefMatcher.group(1), baseUrl, baseDomain, sameDomainOnly);
            if (link != null) links.add(link);
        }
        while (hrefMatcher2.find()) {
            String link = processLink(hrefMatcher2.group(1), baseUrl, baseDomain, sameDomainOnly);
            if (link != null) links.add(link);
        }
    } catch (Exception e) {}
    return links;
}

private String processLink(String link, String baseUrl, String baseDomain, boolean sameDomainOnly) {
    if (link == null || link.isEmpty()) return null;
    if (link.startsWith("#")) return null;
    if (link.startsWith("javascript:")) return null;
    if (link.startsWith("mailto:")) return null;
    if (link.startsWith("tel:")) return null;
    if (link.startsWith("data:")) return null;
    if (link.startsWith("file:")) return null;
    
    if (link.contains("base64") || link.matches(".*[A-Za-z0-9+/]{40,}.*")) {
        return null;
    }
    
    try {
        java.net.URL absolute = new java.net.URL(new URL(baseUrl), link);
        String absUrl = absolute.toString();
        
        if (!absUrl.startsWith("http://") && !absUrl.startsWith("https://")) return null;
        
        int hashIdx = absUrl.indexOf("#");
        if (hashIdx > 0) absUrl = absUrl.substring(0, hashIdx);
        
        if (sameDomainOnly) {
            String linkDomain = absolute.getHost();
            if (!linkDomain.equals(baseDomain) && !linkDomain.endsWith("." + baseDomain)) return null;
        }
        
        return absUrl;
    } catch (Exception e) {
        return null;
    }
}

private String urlToFileName(String url) {
    try {
        java.net.URL u = new java.net.URL(url);
        String path = u.getPath();
        if (path == null || path.isEmpty() || path.equals("/")) {
            return "index.html";
        }
        if (path.endsWith("/")) {
            path += "index.html";
        }
        if (path.startsWith("/")) path = path.substring(1);
        path = path.replace("?", "_qm_").replace("&", "_and_").replace("=", "_eq_");
        return path;
    } catch (Exception e) {
        String safe = url.replace("https://", "").replace("http://", "").replace("/", "_")
            .replace("?", "_qm_").replace("&", "_and_").replace("=", "_eq_");
        if (safe.length() > 150) safe = safe.substring(0, 150);
        return safe + ".html";
    }
}

private void saveLogToFile(String tag, String msg) {
    try {
        File logDir = new File(crawlSaveDir, "log");
        if (!logDir.exists()) logDir.mkdirs();
        String logFile = crawlSaveDir + "log/crawl_" +
            new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) + ".log";
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String line = "[" + time + "] [" + tag + "] " + msg + "\n";
        FileWriter writer = new FileWriter(logFile, true);
        writer.write(line);
        writer.close();
    } catch (Exception e) {}
}

private Set<String> loadCrawledUrls(String dirPath) {
    Set<String> urls = new HashSet<>();
    try {
        File listFile = new File(dirPath, "crawled.list");
        if (listFile.exists()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new java.io.FileInputStream(listFile), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) urls.add(line);
            }
            reader.close();
        }
    } catch (Exception e) {}
    return urls;
}

private void saveCrawledUrls(String dirPath, Set<String> urls) {
    try {
        File listFile = new File(dirPath, "crawled.list");
        FileWriter writer = new FileWriter(listFile);
        for (String url : urls) {
            writer.write(url + "\n");
        }
        writer.close();
    } catch (Exception e) {}
}

private boolean shouldDownloadByFilter(String url, String whitelist, String blacklist) {
    if (whitelist.isEmpty() && blacklist.isEmpty()) return true;
    
    if (!whitelist.isEmpty()) {
        String[] patterns = whitelist.split(",");
        for (String p : patterns) {
            p = p.trim();
            if (p.isEmpty()) continue;
            String regex = p.replace(".", "\\.").replace("*", ".*").replace("?", ".");
            if (url.matches("(?i).*" + regex + ".*")) return true;
        }
        return false;
    }
    
    if (!blacklist.isEmpty()) {
        String[] patterns = blacklist.split(",");
        for (String p : patterns) {
            p = p.trim();
            if (p.isEmpty()) continue;
            String regex = p.replace(".", "\\.").replace("*", ".*").replace("?", ".");
            if (url.matches("(?i).*" + regex + ".*")) return false;
        }
    }
    return true;
}

    
private void showEditSourceDialog() {
    getCurrentWebView().evaluateJavascript(
        "document.documentElement.outerHTML",
        new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String html) {
                String source = html;
                if (source != null && source.length() > 1) {
                    source = source.substring(1, source.length() - 1);
                    source = source.replace("\\n", "\n")
                                   .replace("\\t", "\t")
                                   .replace("\\\"", "\"")
                                   .replace("\\\\", "\\");
                    source = decodeUnicodeEscapes(source);
                }
                
                final String finalSource = source;
                
                // 创建对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("编辑网页源码");
                
                // 主布局
                LinearLayout mainLayout = new LinearLayout(MainActivity.this);
                mainLayout.setOrientation(LinearLayout.VERTICAL);
                mainLayout.setPadding(10, 10, 10, 10);
                
                // 工具栏
                LinearLayout toolbar = new LinearLayout(MainActivity.this);
                toolbar.setOrientation(LinearLayout.HORIZONTAL);
                toolbar.setPadding(0, 0, 0, 10);
                
                Button btnJump = new Button(MainActivity.this);
                btnJump.setText("跳转行");
                btnJump.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
                
                Button btnFind = new Button(MainActivity.this);
                btnFind.setText("查找");
                btnFind.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
                
                Button btnApply = new Button(MainActivity.this);
                btnApply.setText("应用");
                btnApply.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
                
                toolbar.addView(btnJump);
                toolbar.addView(btnFind);
                toolbar.addView(btnApply);
                mainLayout.addView(toolbar);
                
                // 水平滚动容器
                HorizontalScrollView hScrollView = new HorizontalScrollView(MainActivity.this);
                hScrollView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
                hScrollView.setLayoutParams(new LinearLayout.LayoutParams(-1, 500));
                
                // 行号 + 代码 横向布局
                LinearLayout editorLayout = new LinearLayout(MainActivity.this);
                editorLayout.setOrientation(LinearLayout.HORIZONTAL);
                
                // 行号 TextView
                final TextView lineNumberView = new TextView(MainActivity.this);
                lineNumberView.setTextColor(0xFF888888);
                lineNumberView.setTextSize(12);
                lineNumberView.setTypeface(Typeface.MONOSPACE);
                lineNumberView.setPadding(10, 10, 10, 10);
                lineNumberView.setBackgroundColor(0xFF222222);
             
                
                // 代码 EditText
                final EditText codeEditor = new EditText(MainActivity.this);
                codeEditor.setText(finalSource);
                codeEditor.setTextSize(12);
                codeEditor.setTypeface(Typeface.MONOSPACE);
                codeEditor.setBackgroundColor(0xFF333333);
                codeEditor.setTextColor(0xFF00FF00);
                codeEditor.setPadding(10, 10, 10, 10);
                codeEditor.setMinWidth(800);
                codeEditor.setHorizontallyScrolling(true);
                
                codeEditor.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
    lineNumberView.setScrollY(scrollY);
});
                
                codeEditor.setVerticalScrollBarEnabled(true);
                codeEditor.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
                
                editorLayout.addView(lineNumberView);
                editorLayout.addView(codeEditor);
                hScrollView.addView(editorLayout);
                mainLayout.addView(hScrollView);
                
                builder.setView(mainLayout);
                
                final AlertDialog dialog = builder.create();
                
                // 更新行号
                updateLineNumbers(codeEditor, lineNumberView);
                
                // 监听文本变化，更新行号
                codeEditor.addTextChangedListener(new android.text.TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override
                    public void afterTextChanged(android.text.Editable s) {
                        updateLineNumbers(codeEditor, lineNumberView);
                    }
                });
                
                // 跳转行
                btnJump.setOnClickListener(v -> {
                    showJumpToLineDialog(codeEditor, hScrollView);
                });
                
                // 查找
                btnFind.setOnClickListener(v -> {
                    showFindInEditorDialog(codeEditor, hScrollView);
                });
                
                // 应用到网页
                btnApply.setOnClickListener(v -> {
                    String newHtml = codeEditor.getText().toString();
                    applyHtmlToWebView(newHtml);
                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, "源码已应用", Toast.LENGTH_SHORT).show();
                });
                
                dialog.show();
            }
        }
    );
}

// 更新行号
private void updateLineNumbers(EditText editText, TextView lineNumberView) {
    String text = editText.getText().toString();
    int lines = text.split("\n", -1).length;
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i <= lines; i++) {
        sb.append(i).append("\n");
    }
    lineNumberView.setText(sb.toString());
}

// 跳转行弹窗
private void showJumpToLineDialog(final EditText editText, final HorizontalScrollView scrollView) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("跳转到行");
    
    final EditText input = new EditText(this);
    input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
    input.setHint("输入行号");
    builder.setView(input);
    
    builder.setPositiveButton("跳转", (dialog, which) -> {
        try {
            int line = Integer.parseInt(input.getText().toString());
            jumpToLine(editText, scrollView, line);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入数字", Toast.LENGTH_SHORT).show();
        }
    });
    builder.setNegativeButton("取消", null);
    builder.show();
}

// 跳转实现
private void jumpToLine(EditText editText, HorizontalScrollView scrollView, int line) {
    if (line < 1) line = 1;
    String text = editText.getText().toString();
    String[] lines = text.split("\n", -1);
    if (line > lines.length) line = lines.length;
    
    // 计算位置
    int position = 0;
    for (int i = 0; i < line - 1; i++) {
        position += lines[i].length() + 1;
    }
    
    editText.requestFocus();
    editText.setSelection(position, position + lines[line - 1].length());
    
    // 不需要滚动 ScrollView，因为 EditText 本身会处理
}

// 查找弹窗
private void showFindInEditorDialog(final EditText editText, final HorizontalScrollView scrollView) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("查找");
    
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(50, 30, 50, 30);
    
    final EditText input = new EditText(this);
    input.setHint("查找内容（支持 * ?）");
    layout.addView(input);
    
    builder.setView(layout);
    
    builder.setPositiveButton("查找", (dialog, which) -> {
        String keyword = input.getText().toString();
        if (keyword.isEmpty()) return;
        findInEditor(editText, keyword);
    });
    builder.setNegativeButton("取消", null);
    builder.show();
}

// 查找实现（支持通配符 * 和 ?）
private void findInEditor(EditText editText, String keyword) {
    String text = editText.getText().toString();
    if (keyword == null || keyword.isEmpty()) {
        Toast.makeText(this, "请输入查找内容", Toast.LENGTH_SHORT).show();
        return;
    }
    
    try {
        // 通配符转正则：先转义，再把 \* 和 \? 替换成 .* 和 .
        String regex = java.util.regex.Pattern.quote(keyword);
        regex = regex.replace("\\*", ".*");
        regex = regex.replace("\\?", ".");
        
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            editText.requestFocus();
            editText.setSelection(start, end);
            Toast.makeText(this, "已定位到第一个匹配", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "未找到", Toast.LENGTH_SHORT).show();
        }
    } catch (Exception e) {
        // 降级为普通字符串查找
        int index = text.indexOf(keyword);
        if (index != -1) {
            editText.requestFocus();
            editText.setSelection(index, index + keyword.length());
            Toast.makeText(this, "已定位到第一个匹配", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "未找到", Toast.LENGTH_SHORT).show();
        }
    }
}
    
    private String decodeUnicodeEscapes(String input) {
        if (input == null) return null;
        StringBuilder result = new StringBuilder();
        int i = 0;
        int len = input.length();
        while (i < len) {
            char c = input.charAt(i);
            if (c == '\\' && i + 5 < len && input.charAt(i + 1) == 'u') {
                String hex = input.substring(i + 2, i + 6);
                try {
                    int code = Integer.parseInt(hex, 16);
                    result.append((char) code);
                    i += 6;
                    continue;
                } catch (NumberFormatException e) {}
            }
            result.append(c);
            i++;
        }
        return result.toString();
    }
    
    private boolean isHeadModified(String original, String modified) {
        if (original == null || modified == null) return false;
        String originalHead = extractTagContent(original, "head");
        String modifiedHead = extractTagContent(modified, "head");
        if (originalHead == null && modifiedHead == null) return false;
        if (originalHead == null || modifiedHead == null) return true;
        return !originalHead.equals(modifiedHead);
    }
    
    private String extractTagContent(String html, String tagName) {
        if (html == null || tagName == null) return null;
        String openTag = "<" + tagName;
        String closeTag = "</" + tagName + ">";
        int start = html.indexOf(openTag);
        if (start == -1) return null;
        int tagEnd = html.indexOf(">", start);
        if (tagEnd == -1) return null;
        int end = html.indexOf(closeTag, tagEnd);
        if (end == -1) return null;
        return html.substring(tagEnd + 1, end);
    }
    
    private void applyHtmlToWebView(String html) {
        String currentUrl = getCurrentWebView().getUrl();
        if (currentUrl != null && currentUrl.startsWith("data:")) {
            android.webkit.WebBackForwardList list = getCurrentWebView().copyBackForwardList();
            if (list.getSize() > 0) {
                for (int i = list.getCurrentIndex(); i >= 0; i--) {
                    String url = list.getItemAtIndex(i).getUrl();
                    if (url != null && !url.startsWith("data:")) {
                        currentUrl = url;
                        break;
                    }
                }
            }
            if (currentUrl == null || currentUrl.startsWith("data:")) {
                currentUrl = "about:blank";
            }
        }
        String baseUrl = currentUrl != null ? currentUrl : "about:blank";
        getCurrentWebView().loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", baseUrl);
    }
    
    private void setBlockAutoplay(boolean block) {
        for (WebView wv : webViewList) {
            WebSettings settings = wv.getSettings();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                settings.setMediaPlaybackRequiresUserGesture(block);
            } else {
                settings.setMediaPlaybackRequiresUserGesture(block);
            }
        }
    }
    
    private void createSearchFloat() {
        searchFloat = new FrameLayout(this);
        searchFloat.setBackgroundColor(0xDD1E1E1E);
        searchFloat.setPadding(10, 10, 10, 10);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(600, -2);
        params.leftMargin = 100;
        params.topMargin = 300;
        searchFloat.setLayoutParams(params);
        
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(15, 15, 15, 15);
        
        LinearLayout titleBar = new LinearLayout(this);
        titleBar.setOrientation(LinearLayout.HORIZONTAL);
        titleBar.setBackgroundColor(0xFF444444);
        titleBar.setPadding(10, 10, 10, 10);
        TextView title = new TextView(this);
        title.setText("页内查找");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(16);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
        Button closeBtn = new Button(this);
        closeBtn.setText("X");
        closeBtn.setTextColor(0xFFFFFFFF);
        closeBtn.setBackgroundColor(0x00000000);
        closeBtn.setOnClickListener(v -> toggleSearchFloat());
        titleBar.addView(title);
        titleBar.addView(closeBtn);
        
        etSearchInput = new EditText(this);
        etSearchInput.setHint("输入搜索内容... 支持 * ?");
        etSearchInput.setTextColor(0xFFFFFFFF);
        etSearchInput.setHintTextColor(0xFF888888);
        etSearchInput.setBackgroundColor(0xFF333333);
        etSearchInput.setPadding(15, 15, 15, 15);
        etSearchInput.setSingleLine(true);
        
        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setPadding(0, 15, 0, 15);
        Button btnSearch = new Button(this);
        btnSearch.setText("开始搜索");
        btnSearch.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
        btnSearch.setOnClickListener(v -> performPageSearch());
        ImageButton btnRefresh = new ImageButton(this);
        btnRefresh.setImageResource(R.drawable.ic_refresh);
        btnRefresh.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
        
        btnRefresh.setOnClickListener(v -> {
            etSearchInput.setText("");
            tvSearchResult.setText("等待输入");
            clearHighlights();
        });
        
        buttonRow.addView(btnSearch);
        buttonRow.addView(btnRefresh);
        
        LinearLayout enhancedRow = new LinearLayout(this);
        enhancedRow.setOrientation(LinearLayout.HORIZONTAL);
        enhancedRow.setPadding(0, 5, 0, 5);
        TextView enhancedLabel = new TextView(this);
        enhancedLabel.setText("加强算法");
        enhancedLabel.setTextColor(0xFFFFFFFF);
        enhancedLabel.setTextSize(14);
        CheckBox enhancedCheck = new CheckBox(this);
        enhancedCheck.setChecked(enhancedMode);
        enhancedCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            enhancedMode = isChecked;
        });
        TextView enhancedHint = new TextView(this);
        enhancedHint.setText("(搜索动态加载内容)");
        enhancedHint.setTextColor(0xFF888888);
        enhancedHint.setTextSize(10);
        enhancedHint.setPadding(10, 0, 0, 0);
        enhancedRow.addView(enhancedLabel);
        enhancedRow.addView(enhancedCheck);
        enhancedRow.addView(enhancedHint);
        
        tvSearchResult = new TextView(this);
        tvSearchResult.setText("等待输入");
        tvSearchResult.setTextColor(0xFF00FF00);
        tvSearchResult.setTextSize(12);
        tvSearchResult.setPadding(0, 15, 0, 0);
        tvSearchResult.setTypeface(Typeface.MONOSPACE);
        
        mainLayout.addView(titleBar);
        mainLayout.addView(etSearchInput);
        mainLayout.addView(buttonRow);
        mainLayout.addView(enhancedRow);
        mainLayout.addView(tvSearchResult);
        searchFloat.addView(mainLayout);
        
        titleBar.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) event.getRawX() - lastX;
                    int dy = (int) event.getRawY() - lastY;
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) searchFloat.getLayoutParams();
                    lp.leftMargin += dx;
                    lp.topMargin += dy;
                    searchFloat.setLayoutParams(lp);
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    return true;
            }
            return false;
        });
        
        addContentView(searchFloat, params);
        searchFloat.setVisibility(View.GONE);
    }
    
    private void toggleSearchFloat() {
        if (searchFloat == null) {
            createSearchFloat();
        }
        searchVisible = !searchVisible;
        searchFloat.setVisibility(searchVisible ? View.VISIBLE : View.GONE);
        if (searchVisible && etSearchInput != null) {
            etSearchInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etSearchInput, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }
    
    private void clearHighlights() {
        getCurrentWebView().evaluateJavascript(
            "var els = document.querySelectorAll('[data-xf-highlight]');" +
            "els.forEach(function(el) { el.outerHTML = el.innerHTML; });", null);
    }
    
    private void performPageSearch() {
        String keyword = etSearchInput.getText().toString().trim();
        if (keyword.isEmpty()) {
            tvSearchResult.setText("输入为空");
            return;
        }
        clearHighlights();
        if (enhancedMode) {
            searchWithEnhancedMode(keyword);
        } else {
            searchInCurrentPage(keyword);
        }
    }
    
    private void searchInCurrentPage(String keyword) {
        String js = "(function() {" +
            "var keyword = '" + escapeJs(keyword) + "';" +
            "var count = 0;" +
            "function wildcardMatch(text, pattern) {" +
            "    var regexStr = pattern.replace(/\\*/g, '.*').replace(/\\?/g, '.');" +
            "    try { var regex = new RegExp(regexStr, 'i'); return regex.test(text); }" +
            "    catch(e) { return text.indexOf(pattern) !== -1; }" +
            "}" +
            "function searchNode(node) {" +
            "    if (node.nodeType === 3) {" +
            "        var text = node.textContent;" +
            "        if (wildcardMatch(text, keyword)) {" +
            "            count++;" +
            "            var span = document.createElement('span');" +
            "            span.style.backgroundColor = 'yellow';" +
            "            span.style.color = 'black';" +
            "            span.setAttribute('data-xf-highlight', 'true');" +
            "            span.textContent = node.textContent;" +
            "            node.parentNode.replaceChild(span, node);" +
            "        }" +
            "    } else if (node.nodeType === 1 && !['SCRIPT','STYLE','TEXTAREA'].includes(node.tagName)) {" +
            "        node.childNodes.forEach(function(c) { searchNode(c); });" +
            "    }" +
            "}" +
            "searchNode(document.body);" +
            "return count;" +
            "})();";
        getCurrentWebView().evaluateJavascript(js, value -> {
            runOnUiThread(() -> {
                tvSearchResult.setText("找到 " + value + " 处匹配");
            });
        });
    }
    
    private void searchWithEnhancedMode(String keyword) {
        tvSearchResult.setText("加强模式搜索中...");
        String js = "var result = { dom: 0, dynamic: 0 };" +
            "function wildcardMatch(text, pattern) {" +
            "    var regexStr = pattern.replace(/\\*/g, '.*').replace(/\\?/g, '.');" +
            "    try { var regex = new RegExp(regexStr, 'i'); return regex.test(text); }" +
            "    catch(e) { return text.indexOf(pattern) !== -1; }" +
            "}" +
            "function searchNode(node) {" +
            "    if (node.nodeType === 3) {" +
            "        var text = node.textContent;" +
            "        if (wildcardMatch(text, '" + escapeJs(keyword) + "')) {" +
            "            result.dom++;" +
            "            var span = document.createElement('span');" +
            "            span.style.backgroundColor = 'yellow';" +
            "            span.style.color = 'black';" +
            "            span.setAttribute('data-xf-highlight', 'true');" +
            "            span.textContent = node.textContent;" +
            "            node.parentNode.replaceChild(span, node);" +
            "        }" +
            "    } else if (node.nodeType === 1 && !['SCRIPT','STYLE','TEXTAREA'].includes(node.tagName)) {" +
            "        node.childNodes.forEach(function(c) { searchNode(c); });" +
            "    }" +
            "}" +
            "searchNode(document.body);" +
            "if (typeof jQuery !== 'undefined') {" +
            "    var dynText = document.body.innerText;" +
            "    var regex = new RegExp('" + escapeJs(keyword).replace("\\*", ".*").replace("\\?", ".") + "', 'gi');" +
            "    var dynMatches = dynText.match(regex);" +
            "    if (dynMatches) result.dynamic = dynMatches.length;" +
            "}" +
            "JSON.stringify(result);";
        getCurrentWebView().evaluateJavascript(js, value -> {
            runOnUiThread(() -> {
                try {
                    int dom = extractNumber(value, "dom");
                    int dynamic = extractNumber(value, "dynamic");
                    tvSearchResult.setText("DOM: " + dom + " 处, 动态: " + dynamic + " 处");
                } catch (Exception e) {
                    tvSearchResult.setText("加强模式完成");
                }
            });
        });
    }
    
    private String escapeJs(String s) {
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"");
    }
    
    private int extractNumber(String json, String key) {
        int idx = json.indexOf("\"" + key + "\":");
        if (idx == -1) return 0;
        int start = json.indexOf(":", idx) + 1;
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        try {
            return Integer.parseInt(json.substring(start, end).trim());
        } catch (Exception e) {
            return 0;
        }
    }
    
    private void loadUrl() {
        String url = etUrl.getText().toString().trim();
        if (!url.isEmpty()) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            HistoryManager.getInstance(this).addHistory(url);
            
            etUrl.clearFocus();
historyPage.setVisibility(View.GONE);
            
            loadWithSafeBrowsing(url);
        }
    }
    
    private void setJavaScriptEnabled(boolean enable) {
        WebSettings settings = getCurrentWebView().getSettings();
        settings.setJavaScriptEnabled(enable);
    }
    
    private void setTextMode(boolean enable) {
        WebSettings settings = getCurrentWebView().getSettings();
        if (enable) {
            settings.setBlockNetworkImage(true);
            settings.setLoadsImagesAutomatically(false);
            settings.setJavaScriptEnabled(false);
            String textUA = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";
            settings.setUserAgentString(textUA);
        } else {
            settings.setBlockNetworkImage(false);
            settings.setLoadsImagesAutomatically(true);
            settings.setJavaScriptEnabled(true);
            String normalUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0";
            settings.setUserAgentString(normalUA);
        }
    }
    /*
    private void createF12Float() {
        f12Float = new FrameLayout(this);
        f12Float.setBackgroundColor(0xDD1E1E1E);
        f12Float.setPadding(5, 5, 5, 5);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(floatWidth, floatHeight);
        params.leftMargin = 100;
        params.topMargin = 200;
        f12Float.setLayoutParams(params);
        
        LinearLayout titleBar = new LinearLayout(this);
        titleBar.setOrientation(LinearLayout.HORIZONTAL);
        titleBar.setBackgroundColor(0xFF333333);
        titleBar.setPadding(10, 10, 10, 10);
        TextView title = new TextView(this);
        title.setText("F12 调试器 (按F12关闭)");
        title.setTextColor(0xFF00FF00);
        title.setTextSize(14);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
        Button closeBtn = new Button(this);
        closeBtn.setText("×");
        closeBtn.setTextColor(0xFFFFFFFF);
        closeBtn.setBackgroundColor(0x00000000);
        closeBtn.setOnClickListener(v -> toggleF12());
        titleBar.addView(title);
        titleBar.addView(closeBtn);
        
        LinearLayout tabBar = new LinearLayout(this);
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setBackgroundColor(0xFF444444);
        Button btnElements = new Button(this);
        btnElements.setText("Elements");
        btnElements.setTextColor(0xFFFFFFFF);
        btnElements.setBackgroundColor(0x00000000);
        btnElements.setPadding(15, 5, 15, 5);
        Button btnConsole = new Button(this);
        btnConsole.setText("Console");
        btnConsole.setTextColor(0xFFFFFFFF);
        btnConsole.setBackgroundColor(0x00000000);
        btnConsole.setPadding(15, 5, 15, 5);
        Button btnNetwork = new Button(this);
        btnNetwork.setText("Network");
        btnNetwork.setTextColor(0xFFFFFFFF);
        btnNetwork.setBackgroundColor(0x00000000);
        btnNetwork.setPadding(15, 5, 15, 5);
        tabBar.addView(btnElements);
        tabBar.addView(btnConsole);
        tabBar.addView(btnNetwork);
        
        FrameLayout contentArea = new FrameLayout(this);
        contentArea.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        LinearLayout elementsPanel = createElementsPanel();
        contentArea.addView(elementsPanel);
        LinearLayout consolePanel = createConsolePanel();
        consolePanel.setVisibility(View.GONE);
        contentArea.addView(consolePanel);
        LinearLayout networkPanel = createNetworkPanel();
        networkPanel.setVisibility(View.GONE);
        contentArea.addView(networkPanel);
        
        btnElements.setOnClickListener(v -> {
            elementsPanel.setVisibility(View.VISIBLE);
            consolePanel.setVisibility(View.GONE);
            networkPanel.setVisibility(View.GONE);
        });
        btnConsole.setOnClickListener(v -> {
            elementsPanel.setVisibility(View.GONE);
            consolePanel.setVisibility(View.VISIBLE);
            networkPanel.setVisibility(View.GONE);
        });
        btnNetwork.setOnClickListener(v -> {
            elementsPanel.setVisibility(View.GONE);
            consolePanel.setVisibility(View.GONE);
            networkPanel.setVisibility(View.VISIBLE);
        });
        
        LinearLayout mainContent = new LinearLayout(this);
        mainContent.setOrientation(LinearLayout.VERTICAL);
        mainContent.addView(titleBar);
        mainContent.addView(tabBar);
        mainContent.addView(contentArea, new LinearLayout.LayoutParams(-1, -1, 1));
        f12Float.addView(mainContent);
        
        titleBar.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) event.getRawX() - lastX;
                    int dy = (int) event.getRawY() - lastY;
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) f12Float.getLayoutParams();
                    lp.leftMargin += dx;
                    lp.topMargin += dy;
                    f12Float.setLayoutParams(lp);
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    return true;
            }
            return false;
        });
        
        addContentView(f12Float, params);
        f12Float.setVisibility(View.GONE);
    }
    */
    /*
    private LinearLayout createElementsPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(10, 10, 10, 10);
        
        final TextView htmlView = new TextView(this);
        htmlView.setTextColor(0xFF00FF00);
        htmlView.setTextSize(12);
        htmlView.setTypeface(Typeface.MONOSPACE);
        htmlView.setMaxLines(15);
        
        Button getHtmlBtn = new Button(this);
        getHtmlBtn.setText("获取当前源码");
        getHtmlBtn.setOnClickListener(v -> {
            getCurrentWebView().evaluateJavascript("document.documentElement.outerHTML", value -> {
                runOnUiThread(() -> {
                    htmlView.setText(value);
                });
            });
        });
        
        Button pickBtn = new Button(this);
        pickBtn.setText("选择页面元素");
        pickBtn.setOnClickListener(v -> {
            getCurrentWebView().evaluateJavascript(
                "(function(){" +
                "var oldCursor=document.body.style.cursor;" +
                "document.body.style.cursor='crosshair';" +
                "document.addEventListener('click',function(e){" +
                "   e.preventDefault();" +
                "   document.body.style.cursor=oldCursor;" +
                "   XF.showSource(e.target.outerHTML);" +
                "},{once:true});" +
                "})()", null);
        });
        
        panel.addView(getHtmlBtn);
        panel.addView(pickBtn);
        panel.addView(htmlView);
        return panel;
    }
    */
    private LinearLayout createConsolePanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(10, 10, 10, 10);
        
        final TextView output = new TextView(this);
        output.setTextColor(0xFF00FF00);
        output.setTextSize(12);
        output.setTypeface(Typeface.MONOSPACE);
        output.setMaxLines(10);
        
        LinearLayout inputLine = new LinearLayout(this);
        inputLine.setOrientation(LinearLayout.HORIZONTAL);
        TextView prompt = new TextView(this);
        prompt.setText("> ");
        prompt.setTextColor(0xFF00FF00);
        prompt.setTextSize(12);
        prompt.setTypeface(Typeface.MONOSPACE);
        final EditText input = new EditText(this);
        input.setTextColor(0xFF00FF00);
        input.setTextSize(12);
        input.setTypeface(Typeface.MONOSPACE);
        input.setBackgroundColor(0xFF222222);
        input.setSingleLine(true);
        input.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
        Button runBtn = new Button(this);
        runBtn.setText("执行");
        inputLine.addView(prompt);
        inputLine.addView(input);
        inputLine.addView(runBtn);
        
        panel.addView(output);
        panel.addView(inputLine);
        
        runBtn.setOnClickListener(v -> {
            String js = input.getText().toString();
            if (js.isEmpty()) return;
            output.append("> " + js + "\n");
            input.setText("");
            getCurrentWebView().evaluateJavascript(js, result -> {
                runOnUiThread(() -> {
                    output.append("← " + result + "\n");
                });
            });
        });
        return panel;
    }
    
    private LinearLayout createNetworkPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(10, 10, 10, 10);
        TextView hint = new TextView(this);
        hint.setText("Network功能开发中\n将在后续版本完善");
        hint.setTextColor(0xFF666666);
        hint.setTextSize(14);
        panel.addView(hint);
        return panel;
    }
    /*
    private void toggleF12() {
        if (f12Float == null) {
            createF12Float();
        }
        f12Visible = !f12Visible;
        f12Float.setVisibility(f12Visible ? View.VISIBLE : View.GONE);
    }
    */
    private void writeLog(String tag, String msg) {
        if (!alwaysLog) return;
        try {
            String dirPath = getExternalFilesDir(null) + "/log/normal/";
            File dir = new File(dirPath);
            if (!dir.exists()) dir.mkdirs();
            String fileName = "browser_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".log";
            File file = new File(dir, fileName);
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            String logLine = "[" + time + "] [" + tag + "] " + msg + "\n";
            FileWriter writer = new FileWriter(file, true);
            writer.write(logLine);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void startLogCapture() {
        if (!alwaysLog) return;
        new Thread(() -> {
            try {
                String dirPath = Environment.getExternalStorageDirectory() + "/xfbrowser/log/normal/";
                File dir = new File(dirPath);
                if (!dir.exists()) dir.mkdirs();
                String fileName = "logcat_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".log";
                File outputFile = new File(dir, fileName);
                Process process = Runtime.getRuntime().exec("logcat -v time -f " + outputFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private String getCustomErrorPage(String failingUrl, int errorCode) {
    if (errorCode != -2) {
        return null;
    }
    
    String domain = "";
    try {
        android.net.Uri uri = android.net.Uri.parse(failingUrl);
        domain = uri.getHost();
    } catch (Exception e) {
        domain = failingUrl;
    }
    
    return "<!DOCTYPE html>" +
        "<html>" +
        "<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
        "<title>域名不存在</title>" +
        "<style>" +
        "body{background:transparent;color:#000;font-family:system-ui,sans-serif;padding:20px;text-align:center;margin:0;}" +
        "h1{font-size:24px;font-weight:normal;margin:50px 0 20px;}" +
        "p{color:#666;margin:20px 0;word-break:break-all;font-size:14px;}" +
        ".domain{color:#e67e22;font-size:16px;}" +
        ".btn-group{margin-top:40px;}" +
        "button{background:#f0f0f0;color:#333;border:1px solid #ccc;padding:10px 20px;margin:8px;font-size:14px;border-radius:4px;cursor:pointer;}" +
        "button:hover{background:#e0e0e0;}" +
        "</style>" +
        "</head>" +
        "<body>" +
        "<h1>此域名不存在</h1>" +
        "<p class='domain'>" + domain + "</p>" +
        "<p>看看有没有拼写错误或后缀不对</p>" +
        "<div class='btn-group'>" +
        "<button onclick='window.location.href=\"" + failingUrl + "\"'>重试</button>" +
        "<button onclick='window.history.back()'>返回</button>" +
        "<button id='gnameBtn'>在 Gname 查看</button>" +
        "</div>" +
        "<script>" +
        "document.getElementById('gnameBtn').onclick = function() {" +
        "   window.location.href = 'https://gname.vip';" +
        "};" +
        "</script>" +
        "</body>" +
        "</html>";
}

    private void handleXuanfengUrl(String url) {
    currentXfUrl = url;
    
    String withoutProtocol = url.replace("xuanfeng://", "");
    
    String addr;
    String path;
    int slashIndex = withoutProtocol.indexOf('/');
    if (slashIndex != -1) {
        addr = withoutProtocol.substring(0, slashIndex);
        path = withoutProtocol.substring(slashIndex);
    } else {
        addr = withoutProtocol;
        path = "/";
    }
    
    final String finalAddr = addr;
    final String finalPath = path;
    
    // 更新网址栏
    runOnUiThread(() -> {
        etUrl.setText(currentXfUrl);
        historyPage.setVisibility(View.GONE);
        etUrl.clearFocus();
    });
    
    new Thread(() -> {
        try {
            String[] result = queryResolver(finalAddr);
            if (result == null) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "解析失败: " + finalAddr, Toast.LENGTH_SHORT).show());
                return;
            }
            String ip = result[0];
            int port = Integer.parseInt(result[1]);
            String content = xfGet(ip, port, finalPath);
            runOnUiThread(() -> showXfContent(content));
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "错误: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }).start();
}

private String[] queryResolver(String address) {
    try {
        Socket socket = new Socket("154.64.252.250", 12316);
        socket.setSoTimeout(5000);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeShort(1);
        dos.writeByte((0x08 << 4) | 0);
        dos.writeShort(1);
        dos.writeShort(0);
        dos.writeByte(64);
        byte[] addrBytes = address.getBytes("UTF-8");
        dos.writeShort(addrBytes.length);
        dos.writeShort(0);
        dos.write(addrBytes);
        
        byte[] data = baos.toByteArray();
        
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeShort(data.length);
        out.write(data);
        out.flush();
        
        DataInputStream in = new DataInputStream(socket.getInputStream());
        int respLen = in.readUnsignedShort();
        byte[] respData = new byte[respLen];
        in.readFully(respData);
        
        socket.close();
        
        String result = new String(respData, 12, respData.length - 12, "UTF-8");
        if (result.contains("|")) {
            String[] parts = result.split("\\|");
            return new String[]{parts[0], parts[1]};
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}

    private String xfGet(String ip, int port, String path) {
    try {
        Socket socket = new Socket(ip, port);
        socket.setSoTimeout(10000);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // 构建 payload: "GET /path"
        String payload = "GET " + path;
        byte[] payloadBytes = payload.getBytes("UTF-8");
        
        // 按照 Python 的顺序: struct.pack('>H', 1)
        dos.writeShort(1);
        // xf.append((0x01 << 4) | 0)
        dos.writeByte((0x01 << 4) | 0);
        // xf.extend(struct.pack('>H', 1))
        dos.writeShort(1);
        // xf.extend(struct.pack('>H', 0))
        dos.writeShort(0);
        // xf.append(64)
        dos.writeByte(64);
        // xf.extend(struct.pack('>H', len(payload)))
        dos.writeShort(payloadBytes.length);
        // xf.extend(struct.pack('>H', 0))
        dos.writeShort(0);
        // xf.extend(payload.encode())
        dos.write(payloadBytes);
        
        byte[] data = baos.toByteArray();
        
        // 发送长度 + 数据
        ByteArrayOutputStream finalBaos = new ByteArrayOutputStream();
        DataOutputStream finalDos = new DataOutputStream(finalBaos);
        finalDos.writeShort(data.length);
        finalDos.write(data);
        
        socket.getOutputStream().write(finalBaos.toByteArray());
        socket.getOutputStream().flush();
        
        // 接收响应
        DataInputStream in = new DataInputStream(socket.getInputStream());
        int respLen = in.readUnsignedShort();
        byte[] respData = new byte[respLen];
        in.readFully(respData);
        
        socket.close();
        
        // 解析响应
        String response = new String(respData, 12, respData.length - 12, "UTF-8");
        String[] parts = response.split("\\|", 3);
        if (parts.length >= 3 && "200".equals(parts[0])) {
            byte[] decoded = android.util.Base64.decode(parts[2], android.util.Base64.DEFAULT);
            return new String(decoded, "UTF-8");
        } else {
            return "Error: " + response;
        }
    } catch (Exception e) {
        return "XF GET error: " + e.getMessage();
    }
}

    private void showXfContent(String content) {
    String html = "<html><body><pre>" + content + "</pre></body></html>";
    webView.loadDataWithBaseURL("xuanfeng://" + currentXfUrl + "/", html, "text/html", "UTF-8", currentXfUrl);
    etUrl.setText(currentXfUrl);
    historyPage.setVisibility(View.GONE);
    etUrl.clearFocus();
}
    
    
    @Override
public void onBackPressed() {
    if (historyPage.getVisibility() == View.VISIBLE) {
        historyPage.setVisibility(View.GONE);
        etUrl.clearFocus();
    } else if (getCurrentWebView().canGoBack()) {
        getCurrentWebView().goBack();
    } else {
        super.onBackPressed();
    }
}
    
    private int calculateThreadCount(String cpuInput, String memInput) {
    int cpuCores = Runtime.getRuntime().availableProcessors();
    long totalMem = Runtime.getRuntime().totalMemory();
    long freeMem = Runtime.getRuntime().freeMemory();
    long maxMem = Runtime.getRuntime().maxMemory();
    
    int cpuThreads;
    if (cpuInput == null || cpuInput.trim().isEmpty()) {
        cpuThreads = Math.max(1, cpuCores / 2);
    } else {
        try {
            cpuThreads = Math.max(1, Integer.parseInt(cpuInput.trim()));
        } catch (NumberFormatException e) {
            cpuThreads = Math.max(1, cpuCores / 2);
        }
    }
    
    int memThreads;
    if (memInput == null || memInput.trim().isEmpty()) {
        long availableMem = freeMem + (maxMem - totalMem);
        long availableMB = availableMem / (1024 * 1024);
        if (availableMB > 512) memThreads = 4;
        else if (availableMB > 256) memThreads = 3;
        else if (availableMB > 128) memThreads = 2;
        else memThreads = 1;
    } else {
        try {
            int targetMB = Integer.parseInt(memInput.trim());
            memThreads = Math.max(1, targetMB / 128);
        } catch (NumberFormatException e) {
            memThreads = 2;
        }
    }
    
    return Math.min(cpuThreads, memThreads);
}
    
    private void loadWithSafeBrowsing(String url) {
    if (safeBrowsingEnabled) {
        String proxyUrl;
        try {
            proxyUrl = "http://154.12.55.135:12316/?url=" + URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            proxyUrl = url;
        }
        getCurrentWebView().loadUrl(proxyUrl);
        //etUrl.setText(url);
    } else {
        getCurrentWebView().loadUrl(url);
    }
}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            String action = data.getStringExtra("action");
            switch (action) {
                case "new_tab":
                    addNewTab("https://xfbrowser.fwh.is/home.html");
                    break;
                case "switch":
                    int pos = data.getIntExtra("position", 0);
                    switchToTab(pos);
                    break;
                case "delete":
                    int delPos = data.getIntExtra("position", 0);
                    closeTab(delPos);
                    break;
                case "delete_and_new":
                    int delPos2 = data.getIntExtra("position", 0);
                    closeTab(delPos2);
                    addNewTab("https://xfbrowser.fwh.is/home.html");
                    break;
            }
        }
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (uploadMessage == null) return;
            Uri result = data != null && resultCode == RESULT_OK ? data.getData() : null;
            uploadMessage.onReceiveValue(new Uri[]{result});
            uploadMessage = null;
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    }

    // ==================== 颜色设置应用 ====================

    private void applyColors() {
        try {
            // 读取颜色设置
            String primaryHex = prefs.getString("xf_primary", "0xFF1565C0");
            String bgHex = prefs.getString("xf_background", "0xFFF5F5F5");
            String iconHex = prefs.getString("xf_toolbar_icon", "0xFF333333");

            int primaryColor = safeParseColor(primaryHex);
            int bgColor = safeParseColor(bgHex);
            int iconColor = safeParseColor(iconHex);

            // 根布局背景
            LinearLayout rootView = findViewById(R.id.root_layout);
            if (rootView == null) {
                // 如果布局没有 root_layout ID，直接用 activity_main 的最外层
                rootView = (LinearLayout) findViewById(android.R.id.content).getRootView();
            }
            // 使用窗口背景方式设置
            getWindow().getDecorView().setBackgroundColor(bgColor);

            // 标题栏背景
            TextView tvTitle = findViewById(R.id.tv_title);
            if (tvTitle != null) {
                tvTitle.setBackgroundColor(bgColor);
            }

            // 顶部 URL 栏区域背景 (LinearLayout 容器)
            LinearLayout urlBarContainer = findViewById(R.id.url_bar_container);
            if (urlBarContainer != null) {
                urlBarContainer.setBackgroundColor(bgColor);
            }

            // 底部工具栏背景
            LinearLayout bottomToolbar = findViewById(R.id.bottom_toolbar);
            if (bottomToolbar != null) {
                bottomToolbar.setBackgroundColor(bgColor);
            }

            // 工具栏图标颜色
            int[] iconButtons = {
                    R.id.btn_back, R.id.btn_forward, R.id.btn_refresh,
                    R.id.btn_desktop, R.id.btn_settings, R.id.btn_toolbox, R.id.btn_tabs
            };
            for (int id : iconButtons) {
                ImageButton btn = findViewById(id);
                if (btn != null) {
                    btn.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                }
            }

            // 进度条颜色
            if (progressBar != null) {
                progressBar.setProgressTintList(ColorStateList.valueOf(primaryColor));
                progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(bgColor));
            }

            // 底部工具栏分隔线
            View divider = findViewById(R.id.toolbar_divider);
            if (divider != null) {
                divider.setBackgroundColor(primaryColor);
            }

        } catch (Exception e) {
            // 忽略，不阻断主流程
        }
    }

    private int safeParseColor(String colorStr) {
        if (colorStr == null) colorStr = "#FF1565C0";
        if (colorStr.startsWith("0x") || colorStr.startsWith("0X")) {
            colorStr = "#" + colorStr.substring(2);
        }
        return Color.parseColor(colorStr);
    }
}