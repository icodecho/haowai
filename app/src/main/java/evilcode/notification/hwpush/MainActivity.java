package evilcode.notification.hwpush;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.push.HmsMessaging;



public class MainActivity extends AppCompatActivity {
    private static final String TAG = "HaoWaiHWPush";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String ACTION_UPDATE_UI = "evilcode.notification.hwpush.UPDATE_UI";

    private TextView tvToken;
    private TextView tvLog;
    private Switch switchNotification;
    private Button btnGetToken;
    private Button btnDeleteToken;
    private Button btnClearLog;
    private Button btnShowToken;
    private Button btnCopyToken;
    private Button btnMessageList;

    private boolean isNotificationEnabled = true;
    private String pushToken;
    private boolean isTokenVisible = false;
    private Handler tokenHideHandler = new Handler(Looper.getMainLooper());
    private Runnable tokenHideRunnable;

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_UPDATE_UI.equals(intent.getAction())) {
                String log = intent.getStringExtra("log");
                if (log != null) {
                    appendLog(log);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
            }
        } catch (Exception e) {
            Log.e(TAG, "设置Toolbar失败", e);
        }

        initViews();
        checkPermissions();
        registerReceiver();

        // 自动获取Token
        getToken();
    }

    private void initViews() {
        tvToken = findViewById(R.id.tv_token);
        tvLog = findViewById(R.id.tv_log);
        switchNotification = findViewById(R.id.switch_notification);
        btnGetToken = findViewById(R.id.btn_get_token);
        btnDeleteToken = findViewById(R.id.btn_delete_token);
        btnClearLog = findViewById(R.id.btn_clear_log);
        btnShowToken = findViewById(R.id.btn_show_token);
        btnCopyToken = findViewById(R.id.btn_copy_token);
        btnMessageList = findViewById(R.id.btn_message_list);

        btnGetToken.setOnClickListener(v -> getToken());
        btnDeleteToken.setOnClickListener(v -> deleteToken());
        btnClearLog.setOnClickListener(v -> tvLog.setText(""));
        btnShowToken.setOnClickListener(v -> toggleTokenVisibility());
        btnCopyToken.setOnClickListener(v -> copyTokenToClipboard());
        btnMessageList.setOnClickListener(v -> openMessageList());

        switchNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setNotificationEnabled(isChecked);
            }
        });
    }

    private void openMessageList() {
        Intent intent = new Intent(this, MessageListActivity.class);
        startActivity(intent);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                appendLog("通知权限已授予");
            } else {
                appendLog("通知权限被拒绝");
            }
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_UPDATE_UI);
        registerReceiver(updateReceiver, filter);
    }

    private void getToken() {
        appendLog("正在获取推送Token...");
        new Thread() {
            @Override
            public void run() {
                try {
                    String appId = AGConnectServicesConfig.fromContext(MainActivity.this)
                            .getString("client/app_id");
                    pushToken = HmsInstanceId.getInstance(MainActivity.this).getToken(appId, "HCM");
                    
                    if (!TextUtils.isEmpty(pushToken)) {
                        Log.i(TAG, "获取Token成功: " + pushToken);
                        runOnUiThread(() -> {
                            isTokenVisible = false;
                            updateTokenDisplay();
                            appendLog("获取Token成功");
                        });
                    }
                } catch (ApiException e) {
                    Log.e(TAG, "获取Token失败: " + e);
                    runOnUiThread(() -> {
                        appendLog("获取Token失败: " + e.getMessage());
                    });
                }
            }
        }.start();
    }

    private void deleteToken() {
        appendLog("正在注销推送Token...");
        new Thread() {
            @Override
            public void run() {
                try {
                    String appId = AGConnectServicesConfig.fromContext(MainActivity.this)
                            .getString("client/app_id");
                    HmsInstanceId.getInstance(MainActivity.this).deleteToken(appId, "HCM");
                    
                    Log.i(TAG, "注销Token成功");
                    runOnUiThread(() -> {
                        cancelTokenHideRunnable();
                        pushToken = null;
                        isTokenVisible = false;
                        tvToken.setText("等待获取...");
                        btnShowToken.setText("显示TOKEN");
                        appendLog("注销Token成功");
                    });
                } catch (ApiException e) {
                    Log.e(TAG, "注销Token失败: " + e);
                    runOnUiThread(() -> {
                        appendLog("注销Token失败: " + e.getMessage());
                    });
                }
            }
        }.start();
    }

    private void updateTokenDisplay() {
        if (TextUtils.isEmpty(pushToken)) {
            tvToken.setText("等待获取...");
            return;
        }
        
        if (isTokenVisible) {
            tvToken.setText(pushToken);
            btnShowToken.setText("隐藏TOKEN");
        } else {
            tvToken.setText(maskToken(pushToken));
            btnShowToken.setText("显示TOKEN");
        }
    }

    private String maskToken(String token) {
        if (TextUtils.isEmpty(token) || token.length() <= 8) {
            return token;
        }
        int startLen = 4;
        int endLen = 4;
        String start = token.substring(0, startLen);
        String end = token.substring(token.length() - endLen);
        return start + "********" + end;
    }

    private void toggleTokenVisibility() {
        if (TextUtils.isEmpty(pushToken)) {
            Toast.makeText(this, "请先获取Token", Toast.LENGTH_SHORT).show();
            return;
        }
        
        cancelTokenHideRunnable();
        
        isTokenVisible = !isTokenVisible;
        updateTokenDisplay();
        
        if (isTokenVisible) {
            tokenHideRunnable = new Runnable() {
                @Override
                public void run() {
                    isTokenVisible = false;
                    updateTokenDisplay();
                }
            };
            tokenHideHandler.postDelayed(tokenHideRunnable, 10000);
        }
    }

    private void cancelTokenHideRunnable() {
        if (tokenHideRunnable != null) {
            tokenHideHandler.removeCallbacks(tokenHideRunnable);
            tokenHideRunnable = null;
        }
    }

    private void copyTokenToClipboard() {
        if (TextUtils.isEmpty(pushToken)) {
            Toast.makeText(this, "请先获取Token", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Push Token", pushToken);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Token已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }

    private void setNotificationEnabled(boolean enabled) {
        isNotificationEnabled = enabled;
        if (enabled) {
            HmsMessaging.getInstance(this).turnOnPush().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    if (task.isSuccessful()) {
                        appendLog("通知栏消息已开启");
                    } else {
                        appendLog("开启通知栏消息失败: " + task.getException().getMessage());
                        switchNotification.setChecked(false);
                    }
                }
            });
        } else {
            HmsMessaging.getInstance(this).turnOffPush().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    if (task.isSuccessful()) {
                        appendLog("通知栏消息已关闭");
                    } else {
                        appendLog("关闭通知栏消息失败: " + task.getException().getMessage());
                        switchNotification.setChecked(true);
                    }
                }
            });
        }
    }

    private void appendLog(String log) {
        String currentLog = tvLog.getText().toString();
        String newLog = log + "\n" + currentLog;
        tvLog.setText(newLog);
    }

    public static void sendUpdateUIBroadcast(Context context, String log) {
        Intent intent = new Intent(ACTION_UPDATE_UI);
        intent.putExtra("log", log);
        context.sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(updateReceiver);
        cancelTokenHideRunnable();
    }
}
