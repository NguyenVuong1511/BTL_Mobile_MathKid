package com.example.mathkid.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.mathkid.database.DatabaseContract.*;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MathKid.db";
    private static final int DATABASE_VERSION = 1;

    // Câu lệnh tạo bảng Users
    private static final String SQL_CREATE_USERS =
            "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                    UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    UserEntry.COLUMN_USERNAME + " TEXT UNIQUE, " +
                    UserEntry.COLUMN_PASSWORD + " TEXT, " +
                    UserEntry.COLUMN_AVATAR + " TEXT);";

    // Câu lệnh tạo bảng Scores
    private static final String SQL_CREATE_SCORES =
            "CREATE TABLE " + ScoreEntry.TABLE_NAME + " (" +
                    ScoreEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ScoreEntry.COLUMN_USER_ID + " INTEGER, " +
                    ScoreEntry.COLUMN_SCORE + " INTEGER, " +
                    ScoreEntry.COLUMN_CATEGORY + " TEXT, " +
                    ScoreEntry.COLUMN_DATE + " DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(" + ScoreEntry.COLUMN_USER_ID + ") REFERENCES " +
                    UserEntry.TABLE_NAME + "(" + UserEntry._ID + "));";

    private static DatabaseHelper instance;

    // Singleton Pattern để tránh rò rỉ bộ nhớ
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
        db.execSQL(SQL_CREATE_SCORES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Trong môi trường dev, có thể xóa đi tạo lại. 
        // Trong môi trường production, nên dùng ALTER TABLE
        db.execSQL("DROP TABLE IF EXISTS " + ScoreEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        onCreate(db);
    }
}
