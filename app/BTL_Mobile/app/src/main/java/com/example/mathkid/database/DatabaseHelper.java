package com.example.mathkid.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.mathkid.database.DatabaseContract.*;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MathKid.db";
    private static final int DATABASE_VERSION = 2; // Tăng version để thực hiện upgrade

    // Câu lệnh tạo bảng Users cải tiến
    private static final String SQL_CREATE_USERS =
            "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                    UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    UserEntry.COLUMN_USERNAME + " TEXT UNIQUE, " +
                    UserEntry.COLUMN_PASSWORD + " TEXT, " +
                    UserEntry.COLUMN_AVATAR + " TEXT, " +
                    UserEntry.COLUMN_LEVEL + " INTEGER DEFAULT 1, " +
                    UserEntry.COLUMN_EXP + " INTEGER DEFAULT 0, " +
                    UserEntry.COLUMN_STREAK + " INTEGER DEFAULT 0, " +
                    UserEntry.COLUMN_TOTAL_STARS + " INTEGER DEFAULT 0, " +
                    UserEntry.COLUMN_LAST_LOGIN + " TEXT);";

    // Câu lệnh tạo bảng Progress
    private static final String SQL_CREATE_PROGRESS =
            "CREATE TABLE " + ProgressEntry.TABLE_NAME + " (" +
                    ProgressEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ProgressEntry.COLUMN_USER_ID + " INTEGER, " +
                    ProgressEntry.COLUMN_CATEGORY + " TEXT, " +
                    ProgressEntry.COLUMN_LESSONS_COMPLETED + " INTEGER DEFAULT 0, " +
                    ProgressEntry.COLUMN_BEST_SCORE + " INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(" + ProgressEntry.COLUMN_USER_ID + ") REFERENCES " +
                    UserEntry.TABLE_NAME + "(" + UserEntry._ID + "));";

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
        db.execSQL(SQL_CREATE_PROGRESS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Trong giai đoạn phát triển, ta có thể xóa đi tạo lại
        // Sau này khi có user thật, ta sẽ dùng ALTER TABLE
        db.execSQL("DROP TABLE IF EXISTS " + ProgressEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        onCreate(db);
    }
}
