package com.example.mathkid.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.mathkid.database.DatabaseContract.*;
import com.example.mathkid.model.Achievement;
import com.example.mathkid.model.Lesson;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    private void openWrite() { db = dbHelper.getWritableDatabase(); }
    private void openRead() { db = dbHelper.getReadableDatabase(); }

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
        return result != -1;
    }

    public boolean checkLogin(String username, String password) {
        openRead();
        Cursor cursor = db.query(UserEntry.TABLE_NAME, null, 
                UserEntry.COLUMN_USERNAME + "=? AND " + UserEntry.COLUMN_PASSWORD + "=?", 
                new String[]{username, password}, null, null, null);
        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        return isValid;
    }

    public boolean isUsernameExists(String username) {
        openRead();
        Cursor cursor = db.query(UserEntry.TABLE_NAME, null,
                UserEntry.COLUMN_USERNAME + "=?",
                new String[]{username}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    @SuppressLint("Range")
    public UserData getUserData(String username) {
        openRead();
        UserData userData = null;
        Cursor cursor = db.query(UserEntry.TABLE_NAME, null, 
                UserEntry.COLUMN_USERNAME + "=? ", new String[]{username}, null, null, null);
        
        if (cursor.moveToFirst()) {
            userData = new UserData();
            userData.id = cursor.getInt(cursor.getColumnIndex(UserEntry._ID));
            userData.username = cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_USERNAME));
            userData.avatar = cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_AVATAR));
            userData.level = cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_LEVEL));
            userData.exp = cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_EXP));
            userData.streak = cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_STREAK));
            userData.totalStars = cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_TOTAL_STARS));
        }
        cursor.close();
        return userData;
    }

    @SuppressLint("Range")
    public List<Achievement> getAchievements(int userId) {
        openRead();
        List<Achievement> list = new ArrayList<>();
        // LEFT JOIN để lấy tất cả huy hiệu có trong game, xem user đã đạt được cái nào chưa
        String sql = "SELECT a.*, ua." + UserAchievementEntry.COLUMN_EARNED_DATE + 
                     " FROM " + AchievementEntry.TABLE_NAME + " a " +
                     " LEFT JOIN " + UserAchievementEntry.TABLE_NAME + " ua ON a." + AchievementEntry._ID + " = ua." + UserAchievementEntry.COLUMN_ACHIEVEMENT_ID +
                     " AND ua." + UserAchievementEntry.COLUMN_USER_ID + " = ?";
        
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(AchievementEntry._ID));
                String title = cursor.getString(cursor.getColumnIndex(AchievementEntry.COLUMN_TITLE));
                String desc = cursor.getString(cursor.getColumnIndex(AchievementEntry.COLUMN_DESCRIPTION));
                String icon = cursor.getString(cursor.getColumnIndex(AchievementEntry.COLUMN_ICON));
                String type = cursor.getString(cursor.getColumnIndex(AchievementEntry.COLUMN_TYPE));
                int val = cursor.getInt(cursor.getColumnIndex(AchievementEntry.COLUMN_REQUIRED_VALUE));
                long date = cursor.getLong(cursor.getColumnIndex(UserAchievementEntry.COLUMN_EARNED_DATE));
                
                list.add(new Achievement(id, title, desc, icon, type, val, date > 0, date));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void earnAchievement(int userId, int achievementId) {
        openWrite();
        // Kiểm tra xem đã đạt được chưa để tránh trùng lặp
        Cursor cursor = db.query(UserAchievementEntry.TABLE_NAME, null,
                UserAchievementEntry.COLUMN_USER_ID + "=? AND " + UserAchievementEntry.COLUMN_ACHIEVEMENT_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(achievementId)}, null, null, null);
        
        if (cursor.getCount() == 0) {
            ContentValues cv = new ContentValues();
            cv.put(UserAchievementEntry.COLUMN_USER_ID, userId);
            cv.put(UserAchievementEntry.COLUMN_ACHIEVEMENT_ID, achievementId);
            cv.put(UserAchievementEntry.COLUMN_EARNED_DATE, System.currentTimeMillis());
            db.insert(UserAchievementEntry.TABLE_NAME, null, cv);
        }
        cursor.close();
    }

    // Các hàm Helper cũ giữ nguyên...
    @SuppressLint("Range")
    public List<Lesson> getLessonsWithProgress(int userId) {
        openRead();
        List<Lesson> lessons = new ArrayList<>();
        try {
            String sql = "SELECT a.*, p." + ProgressEntry.COLUMN_STARS_EARNED + ", p." + ProgressEntry.COLUMN_IS_COMPLETE + 
                         " FROM " + ActivitiesEntry.TABLE_NAME + " a " +
                         " LEFT JOIN " + ProgressEntry.TABLE_NAME + " p ON a." + ActivitiesEntry._ID + " = p." + ProgressEntry.COLUMN_ACTIVITY_ID + 
                         " AND p." + ProgressEntry.COLUMN_USER_ID + " = ?" +
                         " ORDER BY a." + ActivitiesEntry.COLUMN_ORDER_INDEX + " ASC";
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)});
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(ActivitiesEntry._ID));
                    String title = cursor.getString(cursor.getColumnIndex(ActivitiesEntry.COLUMN_TITLE));
                    String icon = cursor.getString(cursor.getColumnIndex(ActivitiesEntry.COLUMN_GAME_TYPE));
                    int stars = cursor.getInt(cursor.getColumnIndex(ProgressEntry.COLUMN_STARS_EARNED));
                    boolean isLocked = cursor.getInt(cursor.getColumnIndex(ActivitiesEntry.COLUMN_IS_LOCKED)) == 1;
                    boolean isComplete = cursor.getInt(cursor.getColumnIndex(ProgressEntry.COLUMN_IS_COMPLETE)) == 1;
                    lessons.add(new Lesson(id, title, icon, stars, isLocked, isComplete, 0));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {}
        return lessons;
    }

    public void seedDataIfNeeded() {
        // Logic seed bài học của bạn...
    }

    public static class UserData {
        public int id;
        public String username;
        public String avatar;
        public int level;
        public int exp;
        public int streak;
        public int totalStars;
    }
}
