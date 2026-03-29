package com.example.mathkid.database;

import android.annotation.SuppressLint;
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

    private void openWrite() { db = dbHelper.getWritableDatabase(); }
    private void openRead() { db = dbHelper.getReadableDatabase(); }
    private void close() { dbHelper.close(); }

    public boolean registerUser(String username, String password, String avatar) {
        openWrite();
        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_USERNAME, username);
        values.put(UserEntry.COLUMN_PASSWORD, password);
        values.put(UserEntry.COLUMN_AVATAR, avatar);
        values.put(UserEntry.COLUMN_LEVEL, 1);
        values.put(UserEntry.COLUMN_EXP, 0);
        values.put(UserEntry.COLUMN_STREAK, 0);
        values.put(UserEntry.COLUMN_TOTAL_STARS, 0);

        long result = db.insert(UserEntry.TABLE_NAME, null, values);
        close();
        return result != -1;
    }

    public boolean checkLogin(String username, String password) {
        openRead();
        Cursor cursor = db.query(UserEntry.TABLE_NAME, null, 
                UserEntry.COLUMN_USERNAME + "=? AND " + UserEntry.COLUMN_PASSWORD + "=?", 
                new String[]{username, password}, null, null, null);
        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        close();
        return isValid;
    }

    public boolean isUsernameExists(String username) {
        openRead();
        Cursor cursor = db.query(UserEntry.TABLE_NAME, null, UserEntry.COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        close();
        return exists;
    }

    // Lấy toàn bộ thông tin User theo username
    @SuppressLint("Range")
    public UserData getUserData(String username) {
        openRead();
        UserData userData = null;
        Cursor cursor = db.query(UserEntry.TABLE_NAME, null, 
                UserEntry.COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        
        if (cursor.moveToFirst()) {
            userData = new UserData();
            userData.username = cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_USERNAME));
            userData.avatar = cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_AVATAR));
            userData.level = cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_LEVEL));
            userData.exp = cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_EXP));
            userData.streak = cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_STREAK));
            userData.totalStars = cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_TOTAL_STARS));
        }
        cursor.close();
        close();
        return userData;
    }

    public static class UserData {
        public String username;
        public String avatar;
        public int level;
        public int exp;
        public int streak;
        public int totalStars;
    }

    // Cập nhật EXP và Level khi hoàn thành bài học
    public void addExp(String username, int expToAdd) {
        openWrite();
        Cursor cursor = db.query(UserEntry.TABLE_NAME, new String[]{UserEntry.COLUMN_EXP, UserEntry.COLUMN_LEVEL}, 
                UserEntry.COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        if (cursor.moveToFirst()) {
            @SuppressLint("Range") int currentExp = cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_EXP));
            
            int newExp = currentExp + expToAdd;
            int newLevel = (newExp / 100) + 1;

            ContentValues values = new ContentValues();
            values.put(UserEntry.COLUMN_EXP, newExp);
            values.put(UserEntry.COLUMN_LEVEL, newLevel);
            db.update(UserEntry.TABLE_NAME, values, UserEntry.COLUMN_USERNAME + "=?", new String[]{username});
        }
        cursor.close();
        close();
    }
}
