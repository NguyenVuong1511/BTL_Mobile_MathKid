package com.example.mathkid.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.mathkid.database.DatabaseContract.*;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MathKid.db";
    private static final int DATABASE_VERSION = 4; // Tăng lên 4 để đảm bảo onUpgrade được gọi

    // 1. Table Users
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

    // 2. Table Levels
    private static final String SQL_CREATE_LEVELS =
            "CREATE TABLE " + LevelEntry.TABLE_NAME + " (" +
                    LevelEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    LevelEntry.COLUMN_LEVEL + " INTEGER, " +
                    LevelEntry.COLUMN_XP + " INTEGER);";

    // 3. Table XP History
    private static final String SQL_CREATE_XP_HISTORY =
            "CREATE TABLE " + XpHistoryEntry.TABLE_NAME + " (" +
                    XpHistoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    XpHistoryEntry.COLUMN_USER_ID + " INTEGER, " +
                    XpHistoryEntry.COLUMN_AMOUNT + " INTEGER, " +
                    XpHistoryEntry.COLUMN_REASON + " TEXT, " +
                    XpHistoryEntry.COLUMN_DATE + " LONG, " +
                    "FOREIGN KEY(" + XpHistoryEntry.COLUMN_USER_ID + ") REFERENCES " +
                    UserEntry.TABLE_NAME + "(" + UserEntry._ID + ") ON DELETE CASCADE);";

    // 4. Table Topics
    private static final String SQL_CREATE_TOPIC =
            "CREATE TABLE " + TopicEntry.TABLE_NAME + " (" +
                    TopicEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TopicEntry.COLUMN_TITLE + " TEXT, " +
                    TopicEntry.COLUMN_ICON + " TEXT, " +
                    TopicEntry.COLUMN_INDEX + " INTEGER);";

    // 5. Table Activities
    private static final String SQL_CREATE_ACTIVITIES =
            "CREATE TABLE " + ActivitiesEntry.TABLE_NAME + " (" +
                    ActivitiesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ActivitiesEntry.COLUMN_TOPIC_ID + " INTEGER, " +
                    ActivitiesEntry.COLUMN_TITLE + " TEXT, " +
                    ActivitiesEntry.COLUMN_CATEGORY + " TEXT, " +
                    ActivitiesEntry.COLUMN_GAME_TYPE + " TEXT, " +
                    ActivitiesEntry.COLUMN_XP_REWARD + " INTEGER, " +
                    ActivitiesEntry.COLUMN_IS_LOCKED + " INTEGER DEFAULT 1, " +
                    ActivitiesEntry.COLUMN_ORDER_INDEX + " INTEGER, " +
                    "FOREIGN KEY(" + ActivitiesEntry.COLUMN_TOPIC_ID + ") REFERENCES " +
                    TopicEntry.TABLE_NAME + "(" + TopicEntry._ID + ") ON DELETE CASCADE);";

    // 6. Table Questions
    private static final String SQL_CREATE_QUESTIONS =
            "CREATE TABLE " + QuestionsEntry.TABLE_NAME + " (" +
                    QuestionsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    QuestionsEntry.COLUMN_ACTIVITY_ID + " INTEGER, " +
                    QuestionsEntry.COLUMN_QUESTION_TYPE + " TEXT, " +
                    QuestionsEntry.COLUMN_QUESTION_TEXT + " TEXT, " +
                    QuestionsEntry.COLUMN_AUDIO + " TEXT, " +
                    QuestionsEntry.COLUMN_IMAGE + " TEXT, " +
                    QuestionsEntry.COLUMN_ANSWER_TEXT + " TEXT, " +
                    QuestionsEntry.COLUMN_OPTION_JSON + " TEXT, " +
                    "FOREIGN KEY(" + QuestionsEntry.COLUMN_ACTIVITY_ID + ") REFERENCES " +
                    ActivitiesEntry.TABLE_NAME + "(" + ActivitiesEntry._ID + ") ON DELETE CASCADE);";

    // 7. Table Progress
    private static final String SQL_CREATE_PROGRESS =
            "CREATE TABLE " + ProgressEntry.TABLE_NAME + " (" +
                    ProgressEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ProgressEntry.COLUMN_USER_ID + " INTEGER, " +
                    ProgressEntry.COLUMN_ACTIVITY_ID + " INTEGER, " +
                    ProgressEntry.COLUMN_STARS_EARNED + " INTEGER DEFAULT 0, " +
                    ProgressEntry.COLUMN_BEST_SCORE + " INTEGER DEFAULT 0, " +
                    ProgressEntry.COLUMN_IS_COMPLETE + " INTEGER DEFAULT 0, " +
                    ProgressEntry.COLUMN_LAST_PLAYED + " LONG, " +
                    "FOREIGN KEY(" + ProgressEntry.COLUMN_USER_ID + ") REFERENCES " + UserEntry.TABLE_NAME + "(" + UserEntry._ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY(" + ProgressEntry.COLUMN_ACTIVITY_ID + ") REFERENCES " + ActivitiesEntry.TABLE_NAME + "(" + ActivitiesEntry._ID + ") ON DELETE CASCADE);";

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
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_LEVELS);
        db.execSQL(SQL_CREATE_XP_HISTORY);
        db.execSQL(SQL_CREATE_TOPIC);
        db.execSQL(SQL_CREATE_ACTIVITIES);
        db.execSQL(SQL_CREATE_QUESTIONS);
        db.execSQL(SQL_CREATE_PROGRESS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa theo thứ tự ngược lại để tránh lỗi ràng buộc khóa ngoại
        db.execSQL("DROP TABLE IF EXISTS " + ProgressEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + QuestionsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ActivitiesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TopicEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + XpHistoryEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LevelEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        onCreate(db);
    }
}
