package com.example.mathkid.database;

import android.provider.BaseColumns;

public final class DatabaseContract {

    private DatabaseContract() {}

    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_AVATAR = "avatar";
        
        // Cải tiến mới
        public static final String COLUMN_LEVEL = "level";
        public static final String COLUMN_EXP = "exp";
        public static final String COLUMN_STREAK = "streak";
        public static final String COLUMN_TOTAL_STARS = "total_stars";
        public static final String COLUMN_LAST_LOGIN = "last_login_date";
    }

    public static class ProgressEntry implements BaseColumns {
        public static final String TABLE_NAME = "learning_progress";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_CATEGORY = "category"; // Cộng, Trừ, Nhân, Chia
        public static final String COLUMN_LESSONS_COMPLETED = "lessons_completed";
        public static final String COLUMN_BEST_SCORE = "best_score";
    }
}
