package evilcode.notification.hwpush;

public class MessageRecord {
    private long id;
    private String title;
    private String content;
    private String data;
    private long receivedTime;

    public MessageRecord() {
    }

    public MessageRecord(String title, String content, String data, long receivedTime) {
        this.title = title;
        this.content = content;
        this.data = data;
        this.receivedTime = receivedTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(long receivedTime) {
        this.receivedTime = receivedTime;
    }
}
