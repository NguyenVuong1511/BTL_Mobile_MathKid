package com.example.mathkid.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.mathkid.database.DatabaseContract.*;
import com.example.mathkid.model.Achievement;
import com.example.mathkid.model.Lesson;
import com.example.mathkid.model.Question;

import org.json.JSONArray;
import org.json.JSONException;

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
    public List<Question> getQuestions(int activityId) {
        openRead();
        List<Question> list = new ArrayList<>();
        Cursor cursor = db.query(QuestionsEntry.TABLE_NAME, null,
                QuestionsEntry.COLUMN_ACTIVITY_ID + "=?",
                new String[]{String.valueOf(activityId)}, null, null, null);
        
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(QuestionsEntry._ID));
                String type = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_QUESTION_TYPE));
                String text = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_QUESTION_TEXT));
                String image = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_IMAGE));
                String answer = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_ANSWER_TEXT));
                String optionsJson = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_OPTION_JSON));
                
                List<String> options = new ArrayList<>();
                try {
                    JSONArray jsonArray = new JSONArray(optionsJson);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        options.add(jsonArray.getString(i));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
                
                list.add(new Question(id, activityId, type, text, null, image, answer, options));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void updateProgress(int userId, int activityId, int stars, int score, boolean isComplete) {
        openWrite();
        ContentValues cv = new ContentValues();
        cv.put(ProgressEntry.COLUMN_USER_ID, userId);
        cv.put(ProgressEntry.COLUMN_ACTIVITY_ID, activityId);
        cv.put(ProgressEntry.COLUMN_STARS_EARNED, stars);
        cv.put(ProgressEntry.COLUMN_BEST_SCORE, score);
        cv.put(ProgressEntry.COLUMN_IS_COMPLETE, isComplete ? 1 : 0);
        cv.put(ProgressEntry.COLUMN_LAST_PLAYED, System.currentTimeMillis());
        db.replace(ProgressEntry.TABLE_NAME, null, cv);

        // Mở khóa bài tiếp theo nếu hoàn thành bài này
        if (isComplete) {
            // Lấy order_index của bài hiện tại
            Cursor c = db.query(ActivitiesEntry.TABLE_NAME, new String[]{ActivitiesEntry.COLUMN_ORDER_INDEX},
                    ActivitiesEntry._ID + "=?", new String[]{String.valueOf(activityId)}, null, null, null);
            if (c.moveToFirst()) {
                @SuppressLint("Range") int currentIndex = c.getInt(c.getColumnIndex(ActivitiesEntry.COLUMN_ORDER_INDEX));
                db.execSQL("UPDATE " + ActivitiesEntry.TABLE_NAME + " SET " + 
                    ActivitiesEntry.COLUMN_IS_LOCKED + " = 0 WHERE " + 
                    ActivitiesEntry.COLUMN_ORDER_INDEX + " = " + (currentIndex + 1));
            }
            c.close();
        }
        
        updateTotalStars(userId);
        checkAndUnlockAchievements(userId);
    }

    private void updateTotalStars(int userId) {
        openWrite();
        db.execSQL("UPDATE " + UserEntry.TABLE_NAME + " SET " + 
                UserEntry.COLUMN_TOTAL_STARS + " = (SELECT SUM(" + ProgressEntry.COLUMN_STARS_EARNED + 
                ") FROM " + ProgressEntry.TABLE_NAME + " WHERE " + ProgressEntry.COLUMN_USER_ID + " = " + userId + ")" +
                " WHERE " + UserEntry._ID + " = " + userId);
    }

    public void addXP(int userId, int amount) {
        openWrite();
        db.execSQL("UPDATE " + UserEntry.TABLE_NAME + " SET " + 
                UserEntry.COLUMN_EXP + " = " + UserEntry.COLUMN_EXP + " + " + amount + 
                " WHERE " + UserEntry._ID + " = " + userId);
        checkAndUnlockAchievements(userId);
    }

    @SuppressLint("Range")
    public void checkAndUnlockAchievements(int userId) {
        openRead();
        // 1. Lấy thông tin người dùng
        int totalStars = 0;
        int totalXP = 0;
        int completedLessons = 0;

        Cursor userCursor = db.query(UserEntry.TABLE_NAME, new String[]{UserEntry.COLUMN_TOTAL_STARS, UserEntry.COLUMN_EXP},
                UserEntry._ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        if (userCursor.moveToFirst()) {
            totalStars = userCursor.getInt(userCursor.getColumnIndex(UserEntry.COLUMN_TOTAL_STARS));
            totalXP = userCursor.getInt(userCursor.getColumnIndex(UserEntry.COLUMN_EXP));
        }
        userCursor.close();

        Cursor lessonCursor = db.query(ProgressEntry.TABLE_NAME, null,
                ProgressEntry.COLUMN_USER_ID + "=? AND " + ProgressEntry.COLUMN_IS_COMPLETE + "=1",
                new String[]{String.valueOf(userId)}, null, null, null);
        completedLessons = lessonCursor.getCount();
        lessonCursor.close();

        // 2. Duyệt danh sách thành tích
        Cursor achCursor = db.query(AchievementEntry.TABLE_NAME, null, null, null, null, null, null);
        if (achCursor.moveToFirst()) {
            do {
                int achId = achCursor.getInt(achCursor.getColumnIndex(AchievementEntry._ID));
                String type = achCursor.getString(achCursor.getColumnIndex(AchievementEntry.COLUMN_TYPE));
                int reqVal = achCursor.getInt(achCursor.getColumnIndex(AchievementEntry.COLUMN_REQUIRED_VALUE));

                boolean reached = false;
                if ("lesson_count".equals(type) && completedLessons >= reqVal) reached = true;
                else if ("total_stars".equals(type) && totalStars >= reqVal) reached = true;
                else if ("xp_milestone".equals(type) && totalXP >= reqVal) reached = true;

                if (reached) {
                    openWrite();
                    ContentValues cv = new ContentValues();
                    cv.put(UserAchievementEntry.COLUMN_USER_ID, userId);
                    cv.put(UserAchievementEntry.COLUMN_ACHIEVEMENT_ID, achId);
                    cv.put(UserAchievementEntry.COLUMN_EARNED_DATE, System.currentTimeMillis());
                    db.insertWithOnConflict(UserAchievementEntry.TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
                }
            } while (achCursor.moveToNext());
        }
        achCursor.close();
    }

    @SuppressLint("Range")
    public List<Lesson> getLessonsWithProgress(int userId) {
        openRead();
        List<Lesson> lessons = new ArrayList<>();
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
        return lessons;
    }

    public void seedDataIfNeeded() {
        openRead();
        Cursor cursor = db.query(ActivitiesEntry.TABLE_NAME, null, null, null, null, null, null);
        if (cursor.getCount() == 0) {
            openWrite();
            // Bài 1: Làm quen số 1-5
            long act1 = addActivity("Làm quen số 1-5", "quiz", 50, 0, 1);
            addQuestion((int)act1, "Có bao nhiêu Gấu Trúc nào?", "panda", "1", "[\"1\", \"2\", \"3\", \"5\"]");
            addQuestion((int)act1, "Đếm xem có bao nhiêu Chú Thỏ?", "rabbit", "3", "[\"2\", \"3\", \"4\", \"1\"]");
            addQuestion((int)act1, "Có mấy Chú Chó ở đây nhỉ?", "dog", "2", "[\"1\", \"2\", \"4\", \"3\"]");

            // Bài 2: Bé tập đếm đến 10
            long act2 = addActivity("Bé tập đếm đến 10", "quiz", 60, 1, 2);
            addQuestion((int)act2, "Số 'Bảy' viết như thế nào?", "fox", "7", "[\"5\", \"6\", \"7\", \"8\"]");
            addQuestion((int)act2, "Đâu là số 'Mười'?", "panda", "10", "[\"1\", \"0\", \"10\", \"100\"]");

            // Bài 3: So sánh Lớn - Bé
            long act3 = addActivity("So sánh Lớn - Bé", "quiz", 70, 1, 3);
            addQuestion((int)act3, "3 quả táo so với 5 quả táo thì bên nào ÍT hơn?", "cat", "3", "[\"3\", \"5\", \"Bằng nhau\"]");
            addQuestion((int)act3, "Số nào LỚN hơn trong hai số này?", "pig", "9", "[\"4\", \"9\", \"7\"]");

            // Bài 4: Phép cộng vui nhộn
            long act4 = addActivity("Phép cộng vui nhộn", "quiz", 80, 1, 4);
            addQuestion((int)act4, "1 + 2 bằng mấy bé ơi?", "dog", "3", "[\"2\", \"3\", \"4\", \"5\"]");
            addQuestion((int)act4, "Nếu có 2 cái kẹo, mẹ cho thêm 2 cái nữa thì có mấy cái?", "rabbit", "4", "[\"3\", \"4\", \"5\"]");

            // Bài 5: Thử thách kéo thả
            long act5 = addActivity("Thử thách kéo thả", "drag", 100, 1, 5);
            addQuestionDrag((int)act5, "Hãy kéo số '5' vào ô trống bên dưới", "rabbit", "5", "[\"3\", \"5\", \"8\"]");
            addQuestionDrag((int)act5, "Số nào là số 'Hai' nào?", "dog", "2", "[\"1\", \"2\", \"4\"]");
            addQuestionDrag((int)act5, "Bé kéo số lượng Gấu Trúc (1) vào nhé", "panda", "1", "[\"1\", \"3\", \"0\"]");

            // Bài 6: Bé tập nối cặp
            long act6 = addActivity("Bé tập nối cặp", "matching", 120, 1, 6);
            addQuestionMatching((int)act6, "Bé hãy nối số với chữ tương ứng nhé", "[\"1:One\", \"2:Two\", \"3:Three\", \"4:Four\"]");
            addQuestionMatching((int)act6, "Nối động vật với thức ăn nào", "[\"Gấu:Mật ong\", \"Thỏ:Cà rốt\", \"Chó:Xương\"]");

            // Bài 7: Bé tập so sánh
            long act7 = addActivity("Bé tập so sánh", "comparison", 150, 1, 7);
            addQuestionComparison((int)act7, "Bé hãy chọn dấu thích hợp nhé", "5", "8", "<");
            addQuestionComparison((int)act7, "Số nào lớn hơn nhỉ?", "10", "3", ">");
            addQuestionComparison((int)act7, "Bé xem hai số này thế nào với nhau?", "4", "4", "=");
        }
        cursor.close();
    }

    private long addActivity(String title, String type, int xp, int locked, int index) {
        ContentValues cv = new ContentValues();
        cv.put(ActivitiesEntry.COLUMN_TOPIC_ID, 1);
        cv.put(ActivitiesEntry.COLUMN_TITLE, title);
        cv.put(ActivitiesEntry.COLUMN_GAME_TYPE, type);
        cv.put(ActivitiesEntry.COLUMN_XP_REWARD, xp);
        cv.put(ActivitiesEntry.COLUMN_IS_LOCKED, locked);
        cv.put(ActivitiesEntry.COLUMN_ORDER_INDEX, index);
        return db.insert(ActivitiesEntry.TABLE_NAME, null, cv);
    }

    private void addQuestion(int actId, String text, String img, String ans, String optJson) {
        ContentValues cv = new ContentValues();
        cv.put(QuestionsEntry.COLUMN_ACTIVITY_ID, actId);
        cv.put(QuestionsEntry.COLUMN_QUESTION_TYPE, "quiz");
        cv.put(QuestionsEntry.COLUMN_QUESTION_TEXT, text);
        cv.put(QuestionsEntry.COLUMN_IMAGE, img);
        cv.put(QuestionsEntry.COLUMN_ANSWER_TEXT, ans);
        cv.put(QuestionsEntry.COLUMN_OPTION_JSON, optJson);
        db.insert(QuestionsEntry.TABLE_NAME, null, cv);
    }

    private void addQuestionDrag(int actId, String text, String img, String ans, String optJson) {
        ContentValues cv = new ContentValues();
        cv.put(QuestionsEntry.COLUMN_ACTIVITY_ID, actId);
        cv.put(QuestionsEntry.COLUMN_QUESTION_TYPE, "drag");
        cv.put(QuestionsEntry.COLUMN_QUESTION_TEXT, text);
        cv.put(QuestionsEntry.COLUMN_IMAGE, img);
        cv.put(QuestionsEntry.COLUMN_ANSWER_TEXT, ans);
        cv.put(QuestionsEntry.COLUMN_OPTION_JSON, optJson);
        db.insert(QuestionsEntry.TABLE_NAME, null, cv);
    }

    private void addQuestionMatching(int actId, String text, String pairsJson) {
        ContentValues cv = new ContentValues();
        cv.put(QuestionsEntry.COLUMN_ACTIVITY_ID, actId);
        cv.put(QuestionsEntry.COLUMN_QUESTION_TYPE, "matching");
        cv.put(QuestionsEntry.COLUMN_QUESTION_TEXT, text);
        cv.put(QuestionsEntry.COLUMN_OPTION_JSON, "[\"" + pairsJson.replace("\"", "\\\"") + "\"]");
        db.insert(QuestionsEntry.TABLE_NAME, null, cv);
    }

    private void addQuestionComparison(int actId, String text, String num1, String num2, String correctSign) {
        ContentValues cv = new ContentValues();
        cv.put(QuestionsEntry.COLUMN_ACTIVITY_ID, actId);
        cv.put(QuestionsEntry.COLUMN_QUESTION_TYPE, "comparison");
        cv.put(QuestionsEntry.COLUMN_QUESTION_TEXT, text);
        cv.put(QuestionsEntry.COLUMN_ANSWER_TEXT, correctSign);
        cv.put(QuestionsEntry.COLUMN_OPTION_JSON, "[\"" + num1 + "\", \"" + num2 + "\"]");
        db.insert(QuestionsEntry.TABLE_NAME, null, cv);
    }

    @SuppressLint("Range")
    public List<Achievement> getAchievements(int userId) {
        openRead();
        List<Achievement> list = new ArrayList<>();
        String sql = "SELECT a.*, ua." + UserAchievementEntry.COLUMN_EARNED_DATE + 
                     " FROM " + AchievementEntry.TABLE_NAME + " a " +
                     " LEFT JOIN " + UserAchievementEntry.TABLE_NAME + " ua ON a." + AchievementEntry._ID + " = ua." + UserAchievementEntry.COLUMN_ACHIEVEMENT_ID +
                     " AND ua." + UserAchievementEntry.COLUMN_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                list.add(new Achievement(
                    cursor.getInt(cursor.getColumnIndex(AchievementEntry._ID)),
                    cursor.getString(cursor.getColumnIndex(AchievementEntry.COLUMN_TITLE)),
                    cursor.getString(cursor.getColumnIndex(AchievementEntry.COLUMN_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndex(AchievementEntry.COLUMN_ICON)),
                    cursor.getString(cursor.getColumnIndex(AchievementEntry.COLUMN_TYPE)),
                    cursor.getInt(cursor.getColumnIndex(AchievementEntry.COLUMN_REQUIRED_VALUE)),
                    cursor.getLong(cursor.getColumnIndex(UserAchievementEntry.COLUMN_EARNED_DATE)) > 0,
                    cursor.getLong(cursor.getColumnIndex(UserAchievementEntry.COLUMN_EARNED_DATE))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
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
