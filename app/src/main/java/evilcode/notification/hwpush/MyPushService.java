package evilcode.notification.hwpush;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;

import java.util.Arrays;

public class MyPushService extends HmsMessageService {
    private static final String TAG = "MyPushService";
    private static final String CHANNEL_ID = "haowai_push_channel";
    private static final int NOTIFICATION_ID = 1001;
    
    private DatabaseHelper databaseHelper;
    private static int notificationCounter = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        databaseHelper = new DatabaseHelper(this);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "号外号外推送";
            String description = "号外号外应用的推送通知";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.i(TAG, "收到新Token: " + token);
        if (!TextUtils.isEmpty(token)) {
            sendTokenToServer(token);
        }
        MainActivity.sendUpdateUIBroadcast(this, "收到新Token");
    }

    private void sendTokenToServer(String token) {
        Log.i(TAG, "发送Token到服务器: " + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Log.i(TAG, "收到推送消息");
        if (message == null) {
            Log.e(TAG, "消息为空");
            return;
        }

        logMessageDetails(message);

        String title = "";
        String content = "";
        String data = message.getData();

        RemoteMessage.Notification notification = message.getNotification();
        if (notification != null) {
            title = notification.getTitle() != null ? notification.getTitle() : "";
            content = notification.getBody() != null ? notification.getBody() : "";
        }

        if (notification == null && !TextUtils.isEmpty(data)) {
            title = "号外号外";
            content = data;
            showNotification(title, content, data);
        } else if (notification != null) {
            showNotification(title, content, data);
        }

        saveMessageRecord(title, content, data);
        MainActivity.sendUpdateUIBroadcast(this, "收到新消息: " + title);
    }

    private void showNotification(String title, String content, String data) {
        notificationCounter++;
        int notificationId = NOTIFICATION_ID + notificationCounter;

        Intent intent = new Intent(this, NotificationClickReceiver.class);
        intent.setAction("evilcode.notification.hwpush.NOTIFICATION_CLICK");
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putExtra("data", data);
        intent.putExtra("notificationId", notificationId);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, 
            notificationId, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (!TextUtils.isEmpty(data)) {
            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                    .bigText(content + "\n\n数据: " + data);
            builder.setStyle(bigTextStyle);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }

    private void logMessageDetails(RemoteMessage message) {
        Log.i(TAG, "Message ID: " + message.getMessageId());
        Log.i(TAG, "Message Type: " + message.getMessageType());
        Log.i(TAG, "Collapse Key: " + message.getCollapseKey());
        Log.i(TAG, "Data: " + message.getData());
        Log.i(TAG, "From: " + message.getFrom());
        Log.i(TAG, "To: " + message.getTo());
        Log.i(TAG, "Sent Time: " + message.getSentTime());
        Log.i(TAG, "TTL: " + message.getTtl());

        RemoteMessage.Notification notification = message.getNotification();
        if (notification != null) {
            Log.i(TAG, "Title: " + notification.getTitle());
            Log.i(TAG, "Body: " + notification.getBody());
            Log.i(TAG, "Icon: " + notification.getIcon());
            Log.i(TAG, "Sound: " + notification.getSound());
            Log.i(TAG, "Tag: " + notification.getTag());
            Log.i(TAG, "Color: " + notification.getColor());
            Log.i(TAG, "Click Action: " + notification.getClickAction());
            Log.i(TAG, "Channel ID: " + notification.getChannelId());
            Log.i(TAG, "Image URL: " + notification.getImageUrl());
            Log.i(TAG, "Link: " + notification.getLink());
            Log.i(TAG, "Notify ID: " + notification.getNotifyId());
            Log.i(TAG, "Badge Number: " + notification.getBadgeNumber());
            Log.i(TAG, "Importance: " + notification.getImportance());
            Log.i(TAG, "Ticker: " + notification.getTicker());
            Log.i(TAG, "Vibrate Config: " + Arrays.toString(notification.getVibrateConfig()));
            Log.i(TAG, "Visibility: " + notification.getVisibility());
            Log.i(TAG, "When: " + notification.getWhen());
            Log.i(TAG, "Light Settings: " + Arrays.toString(notification.getLightSettings()));
            Log.i(TAG, "Local Only: " + notification.isLocalOnly());
            Log.i(TAG, "Auto Cancel: " + notification.isAutoCancel());
            Log.i(TAG, "Default Sound: " + notification.isDefaultSound());
            Log.i(TAG, "Default Vibrate: " + notification.isDefaultVibrate());
            Log.i(TAG, "Default Light: " + notification.isDefaultLight());
        }
    }

    private void saveMessageRecord(String title, String content, String data) {
        MessageRecord record = new MessageRecord(
                title,
                content,
                data,
                System.currentTimeMillis()
        );
        databaseHelper.insertMessage(record);
        Log.i(TAG, "消息已保存到数据库");
    }

    @Override
    public void onMessageSent(String msgId) {
        Log.i(TAG, "消息已发送: " + msgId);
        MainActivity.sendUpdateUIBroadcast(this, "消息已发送: " + msgId);
    }

    @Override
    public void onSendError(String msgId, Exception exception) {
        Log.e(TAG, "消息发送失败: " + msgId + ", 错误: " + exception.getMessage());
        MainActivity.sendUpdateUIBroadcast(this, "消息发送失败: " + msgId);
    }

    @Override
    public void onTokenError(Exception e) {
        Log.e(TAG, "Token获取失败: " + e.getMessage());
        MainActivity.sendUpdateUIBroadcast(this, "Token获取失败: " + e.getMessage());
    }
}
