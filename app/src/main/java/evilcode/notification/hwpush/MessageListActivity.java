package evilcode.notification.hwpush;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MessageListActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private MessageAdapter messageAdapter;
    private List<MessageRecord> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("消息记录");
        }

        initViews();
        initDatabase();
    }

    private void initViews() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_messages);
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        messageAdapter.setOnMessageDeleteListener(messageId -> {
            databaseHelper.deleteMessage(messageId);
            refreshMessageList();
            Toast.makeText(MessageListActivity.this, "已删除消息记录", Toast.LENGTH_SHORT).show();
        });
        messageAdapter.setOnMessageClickListener(message -> {
            copyMessageToClipboard(message);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        findViewById(R.id.fab_clear_all).setOnClickListener(v -> showClearMessagesDialog());
    }

    private void initDatabase() {
        databaseHelper = new DatabaseHelper(this);
        refreshMessageList();
    }

    private void refreshMessageList() {
        messageList.clear();
        messageList.addAll(databaseHelper.getAllMessages());
        messageAdapter.notifyDataSetChanged();
    }

    private void showClearMessagesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除所有消息记录吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    databaseHelper.deleteAllMessages();
                    refreshMessageList();
                    Toast.makeText(MessageListActivity.this, "已清空所有消息记录", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void copyMessageToClipboard(MessageRecord message) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        String textToCopy = "标题: " + message.getTitle() + "\n内容: " + message.getContent() + 
                           (message.getData() != null ? "\n数据: " + message.getData() : "");
        android.content.ClipData clip = android.content.ClipData.newPlainText("推送消息", textToCopy);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "消息已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
