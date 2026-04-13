package com.example.mathkid.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.mathkid.database.DatabaseContract.*;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MathKid.db";
    private static final int DATABASE_VERSION = 5; // Tăng version để cập nhật bảng thành tích

    // Các câu lệnh CREATE TABLE (Giữ nguyên các bảng cũ)
    private static final String SQL_CREATE_USERS =
            "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                    UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    UserEntry.COLUMN_USERNAME + " TEXT UNIQUE, " +
                    UserEntry.COLUMN_PASSWORD + " TEXT, " +
                    UserEntry.COLUMN_DISPLAY_NAME + " TEXT, " +
                    UserEntry.COLUMN_AVATAR + " TEXT, " +
                    UserEntry.COLUMN_LEVEL + " INTEGER DEFAULT 1, " +
                    UserEntry.COLUMN_EXP + " INTEGER DEFAULT 0, " +
                    UserEntry.COLUMN_STREAK + " INTEGER DEFAULT 0, " +
                    UserEntry.COLUMN_TOTAL_STARS + " INTEGER DEFAULT 0, " +
                    UserEntry.COLUMN_LAST_LOGIN + " TEXT);";

    // Bảng Thành tích
    private static final String SQL_CREATE_ACHIEVEMENTS =
            "CREATE TABLE " + AchievementEntry.TABLE_NAME + " (" +
                    AchievementEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    AchievementEntry.COLUMN_TITLE + " TEXT, " +
                    AchievementEntry.COLUMN_DESCRIPTION + " TEXT, " +
                    AchievementEntry.COLUMN_ICON + " TEXT, " +
                    AchievementEntry.COLUMN_TYPE + " TEXT, " +
                    AchievementEntry.COLUMN_REQUIRED_VALUE + " INTEGER);";

    // Bảng User_Achievement (Liên kết bé với thành tích)
    private static final String SQL_CREATE_USER_ACHIEVEMENTS =
            "CREATE TABLE " + UserAchievementEntry.TABLE_NAME + " (" +
                    UserAchievementEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    UserAchievementEntry.COLUMN_USER_ID + " INTEGER, " +
                    UserAchievementEntry.COLUMN_ACHIEVEMENT_ID + " INTEGER, " +
                    UserAchievementEntry.COLUMN_EARNED_DATE + " LONG, " +
                    "FOREIGN KEY(" + UserAchievementEntry.COLUMN_USER_ID + ") REFERENCES " + UserEntry.TABLE_NAME + "(" + UserEntry._ID + "), " +
                    "FOREIGN KEY(" + UserAchievementEntry.COLUMN_ACHIEVEMENT_ID + ") REFERENCES " + AchievementEntry.TABLE_NAME + "(" + AchievementEntry._ID + "));";

    // ... (Giữ các SQL_CREATE khác cho Levels, XpHistory, Topics, Activities, Questions, Progress)
    private static final String SQL_CREATE_LEVELS = "CREATE TABLE " + LevelEntry.TABLE_NAME + " (" + LevelEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + LevelEntry.COLUMN_LEVEL + " INTEGER, " + LevelEntry.COLUMN_XP + " INTEGER);";
    private static final String SQL_CREATE_XP_HISTORY = "CREATE TABLE " + XpHistoryEntry.TABLE_NAME + " (" + XpHistoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + XpHistoryEntry.COLUMN_USER_ID + " INTEGER, " + XpHistoryEntry.COLUMN_AMOUNT + " INTEGER, " + XpHistoryEntry.COLUMN_REASON + " TEXT, " + XpHistoryEntry.COLUMN_DATE + " LONG, FOREIGN KEY(" + XpHistoryEntry.COLUMN_USER_ID + ") REFERENCES " + UserEntry.TABLE_NAME + "(" + UserEntry._ID + ") ON DELETE CASCADE);";
    private static final String SQL_CREATE_TOPIC = "CREATE TABLE " + TopicEntry.TABLE_NAME + " (" + TopicEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TopicEntry.COLUMN_TITLE + " TEXT, " + TopicEntry.COLUMN_ICON + " TEXT, " + TopicEntry.COLUMN_INDEX + " INTEGER);";
    private static final String SQL_CREATE_ACTIVITIES = "CREATE TABLE " + ActivitiesEntry.TABLE_NAME + " (" + ActivitiesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ActivitiesEntry.COLUMN_TOPIC_ID + " INTEGER, " + ActivitiesEntry.COLUMN_TITLE + " TEXT, " + ActivitiesEntry.COLUMN_CATEGORY + " TEXT, " + ActivitiesEntry.COLUMN_GAME_TYPE + " TEXT, " + ActivitiesEntry.COLUMN_XP_REWARD + " INTEGER, " + ActivitiesEntry.COLUMN_IS_LOCKED + " INTEGER DEFAULT 1, " + ActivitiesEntry.COLUMN_ORDER_INDEX + " INTEGER, FOREIGN KEY(" + ActivitiesEntry.COLUMN_TOPIC_ID + ") REFERENCES " + TopicEntry.TABLE_NAME + "(" + TopicEntry._ID + ") ON DELETE CASCADE);";
    private static final String SQL_CREATE_QUESTIONS = "CREATE TABLE " + QuestionsEntry.TABLE_NAME + " (" + QuestionsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + QuestionsEntry.COLUMN_ACTIVITY_ID + " INTEGER, " + QuestionsEntry.COLUMN_QUESTION_TYPE + " TEXT, " + QuestionsEntry.COLUMN_QUESTION_TEXT + " TEXT, " + QuestionsEntry.COLUMN_AUDIO + " TEXT, " + QuestionsEntry.COLUMN_IMAGE + " TEXT, " + QuestionsEntry.COLUMN_ANSWER_TEXT + " TEXT, " + QuestionsEntry.COLUMN_OPTION_JSON + " TEXT, FOREIGN KEY(" + QuestionsEntry.COLUMN_ACTIVITY_ID + ") REFERENCES " + ActivitiesEntry.TABLE_NAME + "(" + ActivitiesEntry._ID + ") ON DELETE CASCADE);";
    private static final String SQL_CREATE_PROGRESS = "CREATE TABLE " + ProgressEntry.TABLE_NAME + " (" + ProgressEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ProgressEntry.COLUMN_USER_ID + " INTEGER, " + ProgressEntry.COLUMN_ACTIVITY_ID + " INTEGER, " + ProgressEntry.COLUMN_STARS_EARNED + " INTEGER DEFAULT 0, " + ProgressEntry.COLUMN_BEST_SCORE + " INTEGER DEFAULT 0, " + ProgressEntry.COLUMN_IS_COMPLETE + " INTEGER DEFAULT 0, " + ProgressEntry.COLUMN_LAST_PLAYED + " LONG, FOREIGN KEY(" + ProgressEntry.COLUMN_USER_ID + ") REFERENCES " + UserEntry.TABLE_NAME + "(" + UserEntry._ID + ") ON DELETE CASCADE, FOREIGN KEY(" + ProgressEntry.COLUMN_ACTIVITY_ID + ") REFERENCES " + ActivitiesEntry.TABLE_NAME + "(" + ActivitiesEntry._ID + ") ON DELETE CASCADE);";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_ACHIEVEMENTS);
        db.execSQL(SQL_CREATE_USER_ACHIEVEMENTS);
        db.execSQL(SQL_CREATE_LEVELS);
        db.execSQL(SQL_CREATE_XP_HISTORY);
        db.execSQL(SQL_CREATE_TOPIC);
        db.execSQL(SQL_CREATE_ACTIVITIES);
        db.execSQL(SQL_CREATE_QUESTIONS);
        db.execSQL(SQL_CREATE_PROGRESS);
        
        seedAchievements(db);
    }

    // Tự động thêm huy hiệu vào DB
    private void seedAchievements(SQLiteDatabase db) {
        addAchievement(db, "Tân Binh", "Hoàn thành bài đầu tiên", "ic_star", "lesson", 1);
        addAchievement(db, "Siêu Tốc", "Giải đố cực nhanh", "ic_trophy", "speed", 1);
        addAchievement(db, "Gấu Trúc", "Đạt mốc 500 XP", "panda", "xp", 500);
        addAchievement(db, "Thỏ Con", "Học tập 7 ngày liên tiếp", "rabbit", "streak", 7);
        addAchievement(db, "Thông Thái", "Đạt 100 điểm tuyệt đối", "ic_cup", "score", 100);
        addAchievement(db, "Bí Mật", "Khám phá mọi ngóc ngách", "ic_lock", "hidden", 1);
    }

    private void addAchievement(SQLiteDatabase db, String title, String desc, String icon, String type, int val) {
        ContentValues cv = new ContentValues();
        cv.put(AchievementEntry.COLUMN_TITLE, title);
        cv.put(AchievementEntry.COLUMN_DESCRIPTION, desc);
        cv.put(AchievementEntry.COLUMN_ICON, icon);
        cv.put(AchievementEntry.COLUMN_TYPE, type);
        cv.put(AchievementEntry.COLUMN_REQUIRED_VALUE, val);
        db.insert(AchievementEntry.TABLE_NAME, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            db.execSQL(SQL_CREATE_ACHIEVEMENTS);
            db.execSQL(SQL_CREATE_USER_ACHIEVEMENTS);
            seedAchievements(db);
        }
    }
}
