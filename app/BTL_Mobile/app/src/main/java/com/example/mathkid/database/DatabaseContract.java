package com.example.mathkid.database;

import android.provider.BaseColumns;

public final class DatabaseContract {

    private DatabaseContract() {}

    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_DISPLAY_NAME = "display_name";
        public static final String COLUMN_AVATAR = "avatar";
        public static final String COLUMN_LEVEL = "level";
        public static final String COLUMN_EXP = "exp";
        public static final String COLUMN_STREAK = "streak";
        public static final String COLUMN_TOTAL_STARS = "total_stars";
        public static final String COLUMN_LAST_LOGIN = "last_login_date";
    }

    public static class LevelEntry implements BaseColumns {
        public static final String TABLE_NAME = "levels";
        public static final String COLUMN_LEVEL = "level";
        public static final String COLUMN_XP = "xp";
    }

    public static class XpHistoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "xp_history";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_REASON = "reason";
        public static final String COLUMN_DATE = "date";
    }

    public static class TopicEntry implements BaseColumns {
        public static final String TABLE_NAME = "topics";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ICON = "icon_res";
        public static final String COLUMN_INDEX = "order_index";
    }

    public static class ActivitiesEntry implements BaseColumns {
        public static final String TABLE_NAME = "activities";
        public static final String COLUMN_TOPIC_ID = "topic_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_GAME_TYPE = "game_type";
        public static final String COLUMN_XP_REWARD = "xp_reward";
        public static final String COLUMN_IS_LOCKED = "is_locked";
        public static final String COLUMN_ORDER_INDEX  = "order_index";
    }

    public static class QuestionsEntry implements BaseColumns {
        public static final String TABLE_NAME = "questions";
        public static final String COLUMN_ACTIVITY_ID = "activity_id";
        public static final String COLUMN_QUESTION_TYPE = "question_type";
        public static final String COLUMN_QUESTION_TEXT = "question_text";
        public static final String COLUMN_AUDIO = "audio";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_ANSWER_TEXT = "answer_text";
        public static final String COLUMN_OPTION_JSON = "option_json";
    }

    public static class ProgressEntry implements BaseColumns {
        public static final String TABLE_NAME = "learning_progress";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_ACTIVITY_ID = "activity_id";
        public static final String COLUMN_STARS_EARNED = "stars_earned";
        public static final String COLUMN_IS_COMPLETE = "is_complete";
        public static final String COLUMN_BEST_SCORE = "best_score";
        public static final String COLUMN_LAST_PLAYED = "last_played";
    }

}
