package evilcode.notification.hwpush;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<MessageRecord> messageList;
    private Context context;
    private OnMessageDeleteListener deleteListener;

    public interface OnMessageDeleteListener {
        void onDelete(long messageId);
    }

    public MessageAdapter(Context context, List<MessageRecord> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    public void setOnMessageDeleteListener(OnMessageDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageRecord message = messageList.get(position);
        holder.tvTitle.setText(message.getTitle() != null ? message.getTitle() : "无标题");
        holder.tvContent.setText(message.getContent() != null ? message.getContent() : "无内容");
        holder.tvData.setText(message.getData() != null ? message.getData() : "");
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timeStr = sdf.format(new Date(message.getReceivedTime()));
        holder.tvTime.setText(timeStr);

        holder.itemView.setOnClickListener(v -> {
            String fullText = "标题: " + (message.getTitle() != null ? message.getTitle() : "") + "\n" +
                    "内容: " + (message.getContent() != null ? message.getContent() : "") + "\n" +
                    "数据: " + (message.getData() != null ? message.getData() : "") + "\n" +
                    "时间: " + timeStr;
            copyToClipboard(fullText);
            Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(message.getId());
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("推送消息", text);
        clipboard.setPrimaryClip(clip);
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvData, tvTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_message_title);
            tvContent = itemView.findViewById(R.id.tv_message_content);
            tvData = itemView.findViewById(R.id.tv_message_data);
            tvTime = itemView.findViewById(R.id.tv_message_time);
        }
    }
}
