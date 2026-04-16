package evilcode.notification.hwpush;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "push_messages.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_DATA = "data";
    private static final String COLUMN_RECEIVED_TIME = "received_time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_CONTENT + " TEXT,"
                + COLUMN_DATA + " TEXT,"
                + COLUMN_RECEIVED_TIME + " INTEGER"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    public long insertMessage(MessageRecord message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, message.getTitle());
        values.put(COLUMN_CONTENT, message.getContent());
        values.put(COLUMN_DATA, message.getData());
        values.put(COLUMN_RECEIVED_TIME, message.getReceivedTime());

        long id = db.insert(TABLE_MESSAGES, null, values);
        db.close();
        return id;
    }

    public List<MessageRecord> getAllMessages() {
        List<MessageRecord> messageList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES + " ORDER BY " + COLUMN_RECEIVED_TIME + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                MessageRecord message = new MessageRecord();
                message.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                message.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                message.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
                message.setData(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATA)));
                message.setReceivedTime(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RECEIVED_TIME)));
                messageList.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return messageList;
    }

    public int deleteMessage(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_MESSAGES, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        return rowsDeleted;
    }

    public int deleteAllMessages() {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_MESSAGES, null, null);
        db.close();
        return rowsDeleted;
    }
}
