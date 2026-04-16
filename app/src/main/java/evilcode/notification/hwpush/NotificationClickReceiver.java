package evilcode.notification.hwpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

public class NotificationClickReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationClick";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && "evilcode.notification.hwpush.NOTIFICATION_CLICK".equals(intent.getAction())) {
            String title = intent.getStringExtra("title");
            String content = intent.getStringExtra("content");
            String data = intent.getStringExtra("data");
            
            Log.i(TAG, "通知被点击: " + title);
            
            if (!TextUtils.isEmpty(title) || !TextUtils.isEmpty(content)) {
                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                MessageRecord record = new MessageRecord(
                        title != null ? title : "",
                        content != null ? content : "",
                        data,
                        System.currentTimeMillis()
                );
                databaseHelper.insertMessage(record);
                Log.i(TAG, "消息已保存到数据库");
            }
            
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(launchIntent);
            }
        }
    }
}
