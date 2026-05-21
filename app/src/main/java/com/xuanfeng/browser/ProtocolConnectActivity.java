package com.xuanfeng.browser;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.lang.reflect.Method;
import java.util.*;

public class ProtocolConnectActivity extends Activity {
    
    private Spinner protocolSpinner;
    private LinearLayout configContainer;
    
    private ProtocolManager protocolManager;
    private ProtocolManager.Protocol currentProtocol;
    private Map<String, View> componentRefs = new HashMap<>();
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        protocolManager = new ProtocolManager(this);
        
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);
        layout.setBackgroundColor(0xFF1E1E1E);
        
        // 标题
        TextView title = new TextView(this);
        title.setText("多协议连接器");
        title.setTextSize(24);
        title.setTextColor(0xFF00FF00);
        title.setPadding(0, 0, 0, 30);
        layout.addView(title);
        
        // 协议选择
        TextView protocolLabel = new TextView(this);
        protocolLabel.setText("选择协议:");
        protocolLabel.setTextColor(0xFF00FF00);
        protocolLabel.setTextSize(16);
        layout.addView(protocolLabel);
        
        protocolSpinner = new Spinner(this);
        updateProtocolSpinner();
        layout.addView(protocolSpinner, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));
        
        // 配置容器
        configContainer = new LinearLayout(this);
        configContainer.setOrientation(LinearLayout.VERTICAL);
        layout.addView(configContainer, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));
        
        scrollView.addView(layout);
        setContentView(scrollView);
    }
    
    private void updateProtocolSpinner() {
        List<ProtocolManager.Protocol> protocols = protocolManager.getProtocols();
        String[] protocolNames = new String[protocols.size()];
        for (int i = 0; i < protocols.size(); i++) {
            protocolNames[i] = protocols.get(i).name;
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, protocolNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        protocolSpinner.setAdapter(adapter);
        
        protocolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentProtocol = protocols.get(position);
                renderUI();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        if (protocols.size() > 0) {
            currentProtocol = protocols.get(0);
            renderUI();
        }
    }
    
    private void renderUI() {
        configContainer.removeAllViews();
        componentRefs.clear();
        
        if (currentProtocol == null || currentProtocol.ui == null) return;
        
        try {
            JSONObject ui = currentProtocol.ui;
            
            // 执行 UI 创建指令
            JSONArray instructions = ui.getJSONArray("create");
            executeInstructions(instructions);
            
            // 执行初始化指令
            if (ui.has("init")) {
                executeInstructions(ui.getJSONArray("init"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void executeInstructions(JSONArray instructions) throws Exception {
        for (int i = 0; i < instructions.length(); i++) {
            JSONObject inst = instructions.getJSONObject(i);
            String action = inst.getString("action");
            
            switch(action) {
                case "create":
                    createComponent(inst);
                    break;
                case "set":
                    setProperty(inst);
                    break;
                case "get":
                    getProperty(inst);
                    break;
                case "call":
                    callMethod(inst);
                    break;
                case "if":
                    executeIf(inst);
                    break;
                case "for":
                    executeFor(inst);
                    break;
                case "return":
                return;
            }
        }
    }
    
    private void createComponent(JSONObject inst) throws Exception {
        String type = inst.getString("type");
        String id = inst.getString("id");
        
        View view = null;
        
        switch(type) {
            case "EditText":
                view = new EditText(this);
                break;
            case "Spinner":
                view = new Spinner(this);
                break;
            case "CheckBox":
                view = new CheckBox(this);
                break;
            case "Button":
                view = new Button(this);
                break;
            case "TextView":
                view = new TextView(this);
                break;
            case "LinearLayout":
                view = new LinearLayout(this);
                ((LinearLayout)view).setOrientation(LinearLayout.VERTICAL);
                break;
        }
        
        if (view != null) {
            componentRefs.put(id, view);
            configContainer.addView(view);
        }
    }
    
    private void setProperty(JSONObject inst) throws Exception {
    String target = inst.getString("target");
    String prop = inst.getString("property");
    Object value = inst.get("value");
    
    View view = componentRefs.get(target);
    if (view == null) return;
    
    // 特殊处理 onClick 事件
    if (prop.equals("onClick") && value instanceof JSONObject) {
        JSONObject onClickAction = (JSONObject) value;
        
        if (view instanceof Button) {
            ((Button) view).setOnClickListener(v -> {
                try {
                    if (onClickAction.has("instructions")) {
                        JSONArray instructions = onClickAction.getJSONArray("instructions");
                        executeInstructions(instructions);
                    } else if (onClickAction.has("action")) {
                        // 单个动作
                        JSONArray singleInst = new JSONArray();
                        singleInst.put(onClickAction);
                        executeInstructions(singleInst);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else if (view instanceof TextView) {
            ((TextView) view).setOnClickListener(v -> {
                try {
                    if (onClickAction.has("instructions")) {
                        JSONArray instructions = onClickAction.getJSONArray("instructions");
                        executeInstructions(instructions);
                    } else if (onClickAction.has("action")) {
                        JSONArray singleInst = new JSONArray();
                        singleInst.put(onClickAction);
                        executeInstructions(singleInst);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else if (view instanceof CheckBox) {
            ((CheckBox) view).setOnClickListener(v -> {
                try {
                    if (onClickAction.has("instructions")) {
                        JSONArray instructions = onClickAction.getJSONArray("instructions");
                        executeInstructions(instructions);
                    } else if (onClickAction.has("action")) {
                        JSONArray singleInst = new JSONArray();
                        singleInst.put(onClickAction);
                        executeInstructions(singleInst);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        return;
    }
    
    // 特殊处理长按事件
    if (prop.equals("onLongClick") && value instanceof JSONObject) {
        JSONObject onLongClickAction = (JSONObject) value;
        view.setOnLongClickListener(v -> {
            try {
                if (onLongClickAction.has("instructions")) {
                    JSONArray instructions = onLongClickAction.getJSONArray("instructions");
                    executeInstructions(instructions);
                } else if (onLongClickAction.has("action")) {
                    JSONArray singleInst = new JSONArray();
                    singleInst.put(onLongClickAction);
                    executeInstructions(singleInst);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
        return;
    }
    
    // 特殊处理选项选择事件 (Spinner)
    if (prop.equals("onItemSelected") && value instanceof JSONObject && view instanceof Spinner) {
        JSONObject onItemSelectedAction = (JSONObject) value;
        ((Spinner) view).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    // 将选中的值存入临时变量
                    String selectedValue = parent.getItemAtPosition(position).toString();
                    componentRefs.put("_selected_value", new TextView(ProtocolConnectActivity.this) {
                        @Override
                        public String toString() {
                            return selectedValue;
                        }
                    });
                    
                    if (onItemSelectedAction.has("instructions")) {
                        JSONArray instructions = onItemSelectedAction.getJSONArray("instructions");
                        executeInstructions(instructions);
                    } else if (onItemSelectedAction.has("action")) {
                        JSONArray singleInst = new JSONArray();
                        singleInst.put(onItemSelectedAction);
                        executeInstructions(singleInst);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        return;
    }
    
    // 动态设置属性（通过 setter 方法）
    String methodName = "set" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
    Method[] methods = view.getClass().getMethods();
    
    for (Method method : methods) {
        if (method.getName().equals(methodName)) {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 1) {
                Object convertedValue = convertValue(value, paramTypes[0]);
                method.invoke(view, convertedValue);
            }
            break;
        }
    }
}
    
    private void getProperty(JSONObject inst) throws Exception {
        String target = inst.getString("target");
        String prop = inst.getString("property");
        String store = inst.getString("store");
        
        View view = componentRefs.get(target);
        if (view == null) return;
        
        String methodName = "get" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
        Method method = view.getClass().getMethod(methodName);
        Object result = method.invoke(view);
        
        // 存储结果
        componentRefs.put(store, (View)result);
    }
    
    private void callMethod(JSONObject inst) throws Exception {
        String target = inst.getString("target");
        String method = inst.getString("method");
        JSONArray args = inst.optJSONArray("args");
        
        View view = componentRefs.get(target);
        if (view == null) return;
        
        Method[] methods = view.getClass().getMethods();
        for (Method m : methods) {
            if (m.getName().equals(method)) {
                if (args != null) {
                    Object[] params = new Object[args.length()];
                    Class<?>[] paramTypes = m.getParameterTypes();
                    for (int i = 0; i < args.length(); i++) {
                        params[i] = convertValue(args.get(i), paramTypes[i]);
                    }
                    m.invoke(view, params);
                } else {
                    m.invoke(view);
                }
                break;
            }
        }
    }
    
    private void executeIf(JSONObject inst) throws Exception {
        String left = inst.getString("left");
        String operator = inst.getString("operator");
        Object right = inst.get("right");
        
        Object leftValue = componentRefs.get(left);
        if (leftValue == null) leftValue = left;
        
        boolean condition = false;
        
        switch(operator) {
            case "==":
                condition = leftValue.equals(right);
                break;
            case "!=":
                condition = !leftValue.equals(right);
                break;
            case "contains":
                condition = leftValue.toString().contains(right.toString());
                break;
        }
        
        if (condition) {
            executeInstructions(inst.getJSONArray("then"));
        } else if (inst.has("else")) {
            executeInstructions(inst.getJSONArray("else"));
        }
    }
    
    private void executeFor(JSONObject inst) throws Exception {
        String item = inst.getString("item");
        JSONArray items = inst.getJSONArray("items");
        JSONArray body = inst.getJSONArray("body");
        
        for (int i = 0; i < items.length(); i++) {
            Object value = items.get(i);
            componentRefs.put(item, (View)value);
            executeInstructions(body);
        }
    }
    
    private Object convertValue(Object value, Class<?> targetType) {
        if (targetType == String.class) {
            return value.toString();
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value.toString());
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value.toString());
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value.toString());
        }
        return value;
    }
}