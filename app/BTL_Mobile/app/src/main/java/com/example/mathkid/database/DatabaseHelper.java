package com.example.mathkid.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.mathkid.database.DatabaseContract.*;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MathKid.db";
    private static final int DATABASE_VERSION = 12; 

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
        // TOPIC 1: LÀM QUEN VỚI CON SỐ
        ContentValues t1 = new ContentValues();
        t1.put(TopicEntry.COLUMN_TITLE, "Làm quen với con số");
        t1.put(TopicEntry.COLUMN_ICON, "ic_topic_numbers");
        t1.put(TopicEntry.COLUMN_INDEX, 1);
        long topic1Id = db.insert(TopicEntry.TABLE_NAME, null, t1);

        long act1_1 = addActivity(db, topic1Id, "Bé tập đếm 1-5", "quiz", 50, 0, 1);
        addQuestion(db, act1_1, "quiz", "Có bao nhiêu chú Gấu Trúc?", "panda", "1", "[\"1\", \"2\", \"3\", \"4\"]");
        addQuestion(db, act1_1, "quiz", "Đếm xem có bao nhiêu chú Thỏ?", "bacontho", "3", "[\"2\", \"3\", \"5\", \"1\"]");
        addQuestion(db, act1_1, "quiz", "Có bao nhiêu chú Chó ở đây?", "haiconcho", "2", "[\"1\", \"2\", \"4\", \"3\"]");
        addQuestion(db, act1_1, "quiz", "Có bao nhiêu quả táo?", "namquatao", "5", "[\"3\", \"4\", \"5\", \"2\"]");
        addQuestion(db, act1_1, "quiz", "Có bao nhiêu con mèo?", "conmeo", "1", "[\"4\", \"1\", \"3\", \"6\"]");

        long act1_2 = addActivity(db, topic1Id, "Bé tập đếm 6-10", "quiz", 50, 1, 2);
        addQuestion(db, act1_2, "quiz", "Đếm số bông hoa?", "saubonghoa", "6", "[\"5\", \"6\", \"7\", \"8\"]");
        addQuestion(db, act1_2, "quiz", "Có bao nhiêu ngôi sao?", "chinngoisao", "9", "[\"7\", \"8\", \"9\", \"10\"]");
        addQuestion(db, act1_2, "quiz", "Đếm số con cá?", "bayconca", "7", "[\"8\", \"9\", \"10\", \"7\"]");

        long act1_3 = addActivity(db, topic1Id, "Tìm số còn thiếu", "fill_blank", 60, 1, 3);
        addQuestion(db, act1_3, "fill_blank", "1, 2, ?, 4, 5", null, "3", "[\"2\", \"3\", \"4\", \"5\"]");
        addQuestion(db, act1_3, "fill_blank", "6, ?, 8, 9, 10", null, "7", "[\"5\", \"7\", \"8\", \"9\"]");

        // TOPIC 2: PHÉP TÍNH CƠ BẢN
        ContentValues t2 = new ContentValues();
        t2.put(TopicEntry.COLUMN_TITLE, "Phép tính cơ bản");
        t2.put(TopicEntry.COLUMN_ICON, "ic_topic_math");
        t2.put(TopicEntry.COLUMN_INDEX, 2);
        long topic2Id = db.insert(TopicEntry.TABLE_NAME, null, t2);

        long act2_1 = addActivity(db, topic2Id, "Cộng trong phạm vi 5", "math", 70, 1, 1);
        addQuestion(db, act2_1, "math", "1 + 1 = ?", null, "2", "[\"1\", \"2\", \"3\", \"4\"]");
        addQuestion(db, act2_1, "math", "2 + 3 = ?", null, "5", "[\"4\", \"5\", \"6\", \"3\"]");
        addQuestion(db, act2_1, "math", "1 + 4 = ?", null, "5", "[\"3\", \"4\", \"5\", \"6\"]");

        long act2_2 = addActivity(db, topic2Id, "Trừ trong phạm vi 5", "math", 70, 1, 2);
        addQuestion(db, act2_2, "math", "5 - 1 = ?", null, "4", "[\"3\", \"4\", \"5\", \"2\"]");
        addQuestion(db, act2_2, "math", "3 - 2 = ?", null, "1", "[\"1\", \"2\", \"0\", \"3\"]");

        // TOPIC 3: HÌNH KHỐI VÀ MÀU SẮC
        ContentValues t3 = new ContentValues();
        t3.put(TopicEntry.COLUMN_TITLE, "Hình khối & Màu sắc");
        t3.put(TopicEntry.COLUMN_ICON, "ic_topic_shapes");
        t3.put(TopicEntry.COLUMN_INDEX, 3);
        long topic3Id = db.insert(TopicEntry.TABLE_NAME, null, t3);

        long act3_1 = addActivity(db, topic3Id, "Nhận diện hình khối", "quiz", 60, 1, 1);
        addQuestion(db, act3_1, "quiz", "Đây là hình gì?", "circle", "Hình tròn", "[\"Hình tròn\", \"Hình vuông\", \"Hình tam giác\"]");
        addQuestion(db, act3_1, "quiz", "Đây là hình gì?", "square", "Hình vuông", "[\"Hình tròn\", \"Hình vuông\", \"Hình tam giác\"]");

        // TOPIC 4: SO SÁNH & LOGIC
        ContentValues t4 = new ContentValues();
        t4.put(TopicEntry.COLUMN_TITLE, "So sánh & Logic");
        t4.put(TopicEntry.COLUMN_ICON, "ic_topic_logic");
        t4.put(TopicEntry.COLUMN_INDEX, 4);
        long topic4Id = db.insert(TopicEntry.TABLE_NAME, null, t4);

        long act4_1 = addActivity(db, topic4Id, "So sánh Lớn - Bé", "comparison", 60, 1, 1);
        addQuestion(db, act4_1, "comparison", "Số nào lớn hơn?", null, ">", "[\"5\", \"3\"]");
        addQuestion(db, act4_1, "comparison", "Số nào bé hơn?", null, "<", "[\"2\", \"8\"]");
        addQuestion(db, act4_1, "comparison", "Số nào lớn hơn?", null, ">", "[\"9\", \"7\"]");
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
        addAchievement(db, "Bậc Thầy Toán Học", "Hoàn thành 20 bài học", "ic_award_master", "lesson_count", 20);
        addAchievement(db, "Ngôi Sao Tỏa Sáng", "Đạt được tổng cộng 10 sao", "ic_award_star_1", "total_stars", 10);
        addAchievement(db, "Siêu Sao", "Đạt được tổng cộng 50 sao", "ic_award_star_2", "total_stars", 50);
        addAchievement(db, "Thợ Săn Điểm Thưởng", "Đạt mốc 100 XP", "ic_award_xp_1", "xp_milestone", 100);
        addAchievement(db, "Kẻ Chinh Phục", "Đạt mốc 1000 XP", "ic_award_xp_2", "xp_milestone", 1000);
        addAchievement(db, "Kiên Trì", "Đạt chuỗi 7 ngày học tập", "ic_award_streak", "streak", 7);
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

    public int getCount(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getTotalStars() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + UserEntry.COLUMN_TOTAL_STARS + ") FROM " + UserEntry.TABLE_NAME, null);
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        return total;
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
