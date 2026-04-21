package com.example.mathkid.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.mathkid.database.DatabaseContract.*;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MathKid.db";
    private static final int DATABASE_VERSION = 9; // Nâng cấp version để thực hiện seeding mới

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

    private static final String SQL_CREATE_ACHIEVEMENTS =
            "CREATE TABLE " + AchievementEntry.TABLE_NAME + " (" +
                    AchievementEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    AchievementEntry.COLUMN_TITLE + " TEXT, " +
                    AchievementEntry.COLUMN_DESCRIPTION + " TEXT, " +
                    AchievementEntry.COLUMN_ICON + " TEXT, " +
                    AchievementEntry.COLUMN_TYPE + " TEXT, " +
                    AchievementEntry.COLUMN_REQUIRED_VALUE + " INTEGER);";

    private static final String SQL_CREATE_USER_ACHIEVEMENTS =
            "CREATE TABLE " + UserAchievementEntry.TABLE_NAME + " (" +
                    UserAchievementEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    UserAchievementEntry.COLUMN_USER_ID + " INTEGER, " +
                    UserAchievementEntry.COLUMN_ACHIEVEMENT_ID + " INTEGER, " +
                    UserAchievementEntry.COLUMN_EARNED_DATE + " LONG, " +
                    "UNIQUE(" + UserAchievementEntry.COLUMN_USER_ID + ", " + UserAchievementEntry.COLUMN_ACHIEVEMENT_ID + "), " +
                    "FOREIGN KEY(" + UserAchievementEntry.COLUMN_USER_ID + ") REFERENCES " + UserEntry.TABLE_NAME + "(" + UserEntry._ID + "), " +
                    "FOREIGN KEY(" + UserAchievementEntry.COLUMN_ACHIEVEMENT_ID + ") REFERENCES " + AchievementEntry.TABLE_NAME + "(" + AchievementEntry._ID + "));";

    private static final String SQL_CREATE_TOPIC = "CREATE TABLE " + TopicEntry.TABLE_NAME + " (" + TopicEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TopicEntry.COLUMN_TITLE + " TEXT, " + TopicEntry.COLUMN_ICON + " TEXT, " + TopicEntry.COLUMN_INDEX + " INTEGER);";
    
    private static final String SQL_CREATE_ACTIVITIES = "CREATE TABLE " + ActivitiesEntry.TABLE_NAME + " (" + 
            ActivitiesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
            ActivitiesEntry.COLUMN_TOPIC_ID + " INTEGER, " + 
            ActivitiesEntry.COLUMN_TITLE + " TEXT, " + 
            ActivitiesEntry.COLUMN_CATEGORY + " TEXT, " + 
            ActivitiesEntry.COLUMN_GAME_TYPE + " TEXT, " + 
            ActivitiesEntry.COLUMN_XP_REWARD + " INTEGER, " + 
            ActivitiesEntry.COLUMN_IS_LOCKED + " INTEGER DEFAULT 1, " + 
            ActivitiesEntry.COLUMN_ORDER_INDEX + " INTEGER);";

    private static final String SQL_CREATE_QUESTIONS = "CREATE TABLE " + QuestionsEntry.TABLE_NAME + " (" + 
            QuestionsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
            QuestionsEntry.COLUMN_ACTIVITY_ID + " INTEGER, " + 
            QuestionsEntry.COLUMN_QUESTION_TYPE + " TEXT, " + 
            QuestionsEntry.COLUMN_QUESTION_TEXT + " TEXT, " + 
            QuestionsEntry.COLUMN_AUDIO + " TEXT, " + 
            QuestionsEntry.COLUMN_IMAGE + " TEXT, " + 
            QuestionsEntry.COLUMN_ANSWER_TEXT + " TEXT, " + 
            QuestionsEntry.COLUMN_OPTION_JSON + " TEXT);";

    private static final String SQL_CREATE_PROGRESS = "CREATE TABLE " + ProgressEntry.TABLE_NAME + " (" + 
            ProgressEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
            ProgressEntry.COLUMN_USER_ID + " INTEGER, " + 
            ProgressEntry.COLUMN_ACTIVITY_ID + " INTEGER, " + 
            ProgressEntry.COLUMN_STARS_EARNED + " INTEGER DEFAULT 0, " + 
            ProgressEntry.COLUMN_BEST_SCORE + " INTEGER DEFAULT 0, " + 
            ProgressEntry.COLUMN_IS_COMPLETE + " INTEGER DEFAULT 0, " + 
            ProgressEntry.COLUMN_LAST_PLAYED + " LONG, " +
            "UNIQUE(" + ProgressEntry.COLUMN_USER_ID + ", " + ProgressEntry.COLUMN_ACTIVITY_ID + "));";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) instance = new DatabaseHelper(context.getApplicationContext());
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
        db.execSQL(SQL_CREATE_TOPIC);
        db.execSQL(SQL_CREATE_ACTIVITIES);
        db.execSQL(SQL_CREATE_QUESTIONS);
        db.execSQL(SQL_CREATE_PROGRESS);
        seedAchievements(db);
        seedInitialData(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        // 1. Thêm Topic
        ContentValues topicValues = new ContentValues();
        topicValues.put(TopicEntry.COLUMN_TITLE, "Toán Cơ Bản");
        topicValues.put(TopicEntry.COLUMN_INDEX, 1);
        long topicId = db.insert(TopicEntry.TABLE_NAME, null, topicValues);

        // 2. Thêm Bài học (Activities)
        long act1 = addActivity(db, topicId, "Bé tập đếm 1-5", "quiz", 50, 0, 1);
        long act2 = addActivity(db, topicId, "So sánh Lớn - Bé", "comparison", 60, 1, 2);

        // 3. Thêm Câu hỏi mẫu cho Bài 1 (Quiz)
        addQuestion(db, act1, "quiz", "Có bao nhiêu chú Gấu Trúc?", "panda", "1", "[\"1\", \"2\", \"3\", \"4\"]");
        addQuestion(db, act1, "quiz", "Đếm xem có bao nhiêu chú Thỏ?", "rabbit", "3", "[\"2\", \"3\", \"5\", \"1\"]");
        addQuestion(db, act1, "quiz", "Có bao nhiêu chú Chó ở đây?", "dog", "2", "[\"1\", \"2\", \"4\", \"3\"]");

        // 4. Thêm Câu hỏi mẫu cho Bài 2 (So sánh - Comparison)
        addQuestion(db, act2, "comparison", "Số nào lớn hơn?", null, ">", "[\"5\", \"3\"]");
        addQuestion(db, act2, "comparison", "Số nào bé hơn?", null, "<", "[\"2\", \"8\"]");
    }

    private long addActivity(SQLiteDatabase db, long topicId, String title, String type, int xp, int locked, int index) {
        ContentValues cv = new ContentValues();
        cv.put(ActivitiesEntry.COLUMN_TOPIC_ID, topicId);
        cv.put(ActivitiesEntry.COLUMN_TITLE, title);
        cv.put(ActivitiesEntry.COLUMN_GAME_TYPE, type);
        cv.put(ActivitiesEntry.COLUMN_XP_REWARD, xp);
        cv.put(ActivitiesEntry.COLUMN_IS_LOCKED, locked);
        cv.put(ActivitiesEntry.COLUMN_ORDER_INDEX, index);
        return db.insert(ActivitiesEntry.TABLE_NAME, null, cv);
    }

    private void addQuestion(SQLiteDatabase db, long actId, String type, String text, String img, String ans, String optJson) {
        ContentValues cv = new ContentValues();
        cv.put(QuestionsEntry.COLUMN_ACTIVITY_ID, actId);
        cv.put(QuestionsEntry.COLUMN_QUESTION_TYPE, type);
        cv.put(QuestionsEntry.COLUMN_QUESTION_TEXT, text);
        cv.put(QuestionsEntry.COLUMN_IMAGE, img);
        cv.put(QuestionsEntry.COLUMN_ANSWER_TEXT, ans);
        cv.put(QuestionsEntry.COLUMN_OPTION_JSON, optJson);
        db.insert(QuestionsEntry.TABLE_NAME, null, cv);
    }

    private void seedAchievements(SQLiteDatabase db) {
        addAchievement(db, "Người Mới Bắt Đầu", "Hoàn thành bài học đầu tiên", "ic_award_beginner", "lesson_count", 1);
        addAchievement(db, "Chuyên Gia Đếm Số", "Hoàn thành 5 bài học", "ic_award_counter", "lesson_count", 5);
        addAchievement(db, "Ngôi Sao Tỏa Sáng", "Đạt được tổng cộng 10 sao", "ic_award_star", "total_stars", 10);
        addAchievement(db, "Thợ Săn Điểm Thưởng", "Đạt mốc 100 XP", "ic_award_xp", "xp_milestone", 100);
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
        db.execSQL("DROP TABLE IF EXISTS " + ProgressEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + QuestionsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ActivitiesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TopicEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserAchievementEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AchievementEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        onCreate(db);
    }
}
