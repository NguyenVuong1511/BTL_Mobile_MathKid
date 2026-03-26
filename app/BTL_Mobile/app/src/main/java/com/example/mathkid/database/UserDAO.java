package com.example.mathkid.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.mathkid.database.DatabaseContract.UserEntry;

public class UserDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Đăng ký người dùng mới
    public boolean registerUser(String username, String password, String avatar) {
        open();
        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_USERNAME, username);
        values.put(UserEntry.COLUMN_PASSWORD, password);
        values.put(UserEntry.COLUMN_AVATAR, avatar);

        long result = db.insert(UserEntry.TABLE_NAME, null, values);
        close();
        return result != -1;
    }

    // Kiểm tra đăng nhập
    public boolean checkLogin(String username, String password) {
        open();
        String selection = UserEntry.COLUMN_USERNAME + " = ?" + " AND " + UserEntry.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(UserEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        close();
        return isValid;
    }

    // Kiểm tra trùng username
    public boolean isUsernameExists(String username) {
        open();
        Cursor cursor = db.query(UserEntry.TABLE_NAME, null, UserEntry.COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        close();
        return exists;
    }
}
