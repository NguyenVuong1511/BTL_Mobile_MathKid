package com.example.mathkid.database;

import android.provider.BaseColumns;

public final class DatabaseContract {

    private DatabaseContract() {}

    /* Định nghĩa bảng Users */
    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_AVATAR = "avatar";
    }

    /* Định nghĩa bảng Scores (Ví dụ bảng mới) */
    public static class ScoreEntry implements BaseColumns {
        public static final String TABLE_NAME = "scores";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_SCORE = "score";
        public static final String COLUMN_CATEGORY = "category"; // cộng, trừ, nhân, chia
        public static final String COLUMN_DATE = "timestamp";
    }
}
