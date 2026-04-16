package evilcode.notification.hwpush;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private DatabaseHelper databaseHelper;
    private MessageAdapter messageAdapter;
    private List<MessageRecord> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);

        initViews();
        initDatabase();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        messageAdapter.setOnMessageClickListener(new MessageAdapter.OnMessageClickListener() {
            @Override
            public void onMessageClick(MessageRecord message) {
                // 点击消息不做任何操作
            }

            @Override
            public void onMessageLongClick(MessageRecord message) {
                showMessageOptionsDialog(message);
            }
        });

        messageAdapter.setOnMessageDeleteListener(messageId -> {
            databaseHelper.deleteMessage(messageId);
            refreshMessageList();
            Toast.makeText(MessageListActivity.this, "已删除消息记录", Toast.LENGTH_SHORT).show();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
    }

    private void initDatabase() {
        databaseHelper = new DatabaseHelper(this);
        refreshMessageList();
    }

    private void refreshMessageList() {
        messageList.clear();
        messageList.addAll(databaseHelper.getAllMessages());
        messageAdapter.notifyDataSetChanged();

        if (messageList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showMessageOptionsDialog(MessageRecord message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timeStr = sdf.format(new Date(message.getReceivedTime()));
        String fullText = "标题: " + (message.getTitle() != null ? message.getTitle() : "") + "\n" +
                "内容: " + (message.getContent() != null ? message.getContent() : "") + "\n" +
                "数据: " + (message.getData() != null ? message.getData() : "") + "\n" +
                "时间: " + timeStr;

        CharSequence[] options = {"复制消息", "删除消息"};
        new AlertDialog.Builder(this)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        copyToClipboard(fullText);
                        Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
                    } else if (which == 1) {
                        showDeleteConfirmDialog(message.getId());
                    }
                })
                .show();
    }

    private void showDeleteConfirmDialog(long messageId) {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除这条消息记录吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    databaseHelper.deleteMessage(messageId);
                    refreshMessageList();
                    Toast.makeText(MessageListActivity.this, "已删除消息记录", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("推送消息", text);
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_clear_all) {
            showClearAllDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showClearAllDialog() {
        if (messageList.isEmpty()) {
            Toast.makeText(this, "暂无消息记录", Toast.LENGTH_SHORT).show();
            return;
        }
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
}
