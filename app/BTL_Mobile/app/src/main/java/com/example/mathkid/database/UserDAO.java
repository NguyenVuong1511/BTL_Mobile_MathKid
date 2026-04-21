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
        // Vá lỗi: Đặt lại trạng thái khóa cho các bài học (trừ bài đầu tiên) nếu chúng đã bị mở khóa nhầm trên toàn hệ thống
        try {
            openWrite();
            db.execSQL("UPDATE " + ActivitiesEntry.TABLE_NAME + " SET " + ActivitiesEntry.COLUMN_IS_LOCKED + " = 1 WHERE " + ActivitiesEntry.COLUMN_ORDER_INDEX + " > 1");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public List<UserData> getTopUsers(int limit) {
        openRead();
        List<UserData> list = new ArrayList<>();
        Cursor cursor = db.query(UserEntry.TABLE_NAME, null, null, null, null, null, 
                UserEntry.COLUMN_EXP + " DESC", String.valueOf(limit));
        
        if (cursor.moveToFirst()) {
            do {
                UserData userData = new UserData();
                userData.id = cursor.getInt(cursor.getColumnIndex(UserEntry._ID));
                userData.username = cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_USERNAME));
                userData.avatar = cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_AVATAR));
                userData.level = cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_LEVEL));
                userData.exp = cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_EXP));
                userData.streak = cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_STREAK));
                userData.totalStars = cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_TOTAL_STARS));
                list.add(userData);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @SuppressLint("Range")
    public List<UserData> getAllUsers() {
        openRead();
        List<UserData> list = new ArrayList<>();
        Cursor cursor = db.query(UserEntry.TABLE_NAME, null, null, null, null, null, UserEntry.COLUMN_USERNAME + " ASC");
        if (cursor.moveToFirst()) {
            do {
                UserData userData = new UserData();
                userData.id = cursor.getInt(cursor.getColumnIndex(UserEntry._ID));
                userData.username = cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_USERNAME));
                userData.avatar = cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_AVATAR));
                userData.level = cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_LEVEL));
                userData.exp = cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_EXP));
                userData.streak = cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_STREAK));
                userData.totalStars = cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_TOTAL_STARS));
                list.add(userData);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean deleteUser(int userId) {
        openWrite();
        db.delete(ProgressEntry.TABLE_NAME, ProgressEntry.COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.delete(UserAchievementEntry.TABLE_NAME, UserAchievementEntry.COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
        int result = db.delete(UserEntry.TABLE_NAME, UserEntry._ID + "=?", new String[]{String.valueOf(userId)});
        return result > 0;
    }

    @SuppressLint("Range")
    public List<Question> getAllQuestions() {
        openRead();
        List<Question> list = new ArrayList<>();
        Cursor cursor = db.query(QuestionsEntry.TABLE_NAME, null, null, null, null, null, QuestionsEntry._ID + " DESC");
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(QuestionsEntry._ID));
                int activityId = cursor.getInt(cursor.getColumnIndex(QuestionsEntry.COLUMN_ACTIVITY_ID));
                String type = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_QUESTION_TYPE));
                String text = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_QUESTION_TEXT));
                String image = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_IMAGE));
                String answer = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_ANSWER_TEXT));
                String optionsJson = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_OPTION_JSON));
                
                List<String> options = new ArrayList<>();
                try {
                    if (optionsJson != null && !optionsJson.isEmpty()) {
                        JSONArray jsonArray = new JSONArray(optionsJson);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            options.add(jsonArray.getString(i));
                        }
                    }
                } catch (JSONException e) { e.printStackTrace(); }
                
                list.add(new Question(id, activityId, type, text, null, image, answer, options));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @SuppressLint("Range")
    public List<Question> getUnassignedQuestions() {
        openRead();
        List<Question> list = new ArrayList<>();
        Cursor cursor = db.query(QuestionsEntry.TABLE_NAME, null, 
                QuestionsEntry.COLUMN_ACTIVITY_ID + " <= 0", null, null, null, QuestionsEntry._ID + " DESC");
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(QuestionsEntry._ID));
                int activityId = cursor.getInt(cursor.getColumnIndex(QuestionsEntry.COLUMN_ACTIVITY_ID));
                String type = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_QUESTION_TYPE));
                String text = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_QUESTION_TEXT));
                String image = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_IMAGE));
                String answer = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_ANSWER_TEXT));
                String optionsJson = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_OPTION_JSON));
                
                List<String> options = new ArrayList<>();
                try {
                    if (optionsJson != null && !optionsJson.isEmpty()) {
                        JSONArray jsonArray = new JSONArray(optionsJson);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            options.add(jsonArray.getString(i));
                        }
                    }
                } catch (JSONException e) { e.printStackTrace(); }
                
                list.add(new Question(id, activityId, type, text, null, image, answer, options));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean addQuestion(int activityId, String type, String text, String image, String answer, String optionsJson) {
        openWrite();
        ContentValues cv = new ContentValues();
        cv.put(QuestionsEntry.COLUMN_ACTIVITY_ID, activityId);
        cv.put(QuestionsEntry.COLUMN_QUESTION_TYPE, type);
        cv.put(QuestionsEntry.COLUMN_QUESTION_TEXT, text);
        cv.put(QuestionsEntry.COLUMN_IMAGE, image);
        cv.put(QuestionsEntry.COLUMN_ANSWER_TEXT, answer);
        cv.put(QuestionsEntry.COLUMN_OPTION_JSON, optionsJson);
        return db.insert(QuestionsEntry.TABLE_NAME, null, cv) != -1;
    }

    public boolean updateQuestion(int id, int activityId, String type, String text, String image, String answer, String optionsJson) {
        openWrite();
        ContentValues cv = new ContentValues();
        cv.put(QuestionsEntry.COLUMN_ACTIVITY_ID, activityId);
        cv.put(QuestionsEntry.COLUMN_QUESTION_TYPE, type);
        cv.put(QuestionsEntry.COLUMN_QUESTION_TEXT, text);
        cv.put(QuestionsEntry.COLUMN_IMAGE, image);
        cv.put(QuestionsEntry.COLUMN_ANSWER_TEXT, answer);
        cv.put(QuestionsEntry.COLUMN_OPTION_JSON, optionsJson);
        return db.update(QuestionsEntry.TABLE_NAME, cv, QuestionsEntry._ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteQuestion(int id) {
        openWrite();
        return db.delete(QuestionsEntry.TABLE_NAME, QuestionsEntry._ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    @SuppressLint("Range")
    public List<Lesson> getAllActivitiesForAdmin() {
        openRead();
        List<Lesson> lessons = new ArrayList<>();
        Cursor cursor = db.query(ActivitiesEntry.TABLE_NAME, null, null, null, null, null, ActivitiesEntry.COLUMN_ORDER_INDEX + " ASC");
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(ActivitiesEntry._ID));
                String title = cursor.getString(cursor.getColumnIndex(ActivitiesEntry.COLUMN_TITLE));
                String gameType = cursor.getString(cursor.getColumnIndex(ActivitiesEntry.COLUMN_GAME_TYPE));
                int orderIndex = cursor.getInt(cursor.getColumnIndex(ActivitiesEntry.COLUMN_ORDER_INDEX));
                lessons.add(new Lesson(id, title, gameType, 0, false, false, orderIndex));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lessons;
    }

    public long addActivityAdmin(String title, String type, int orderIndex) {
        openWrite();
        ContentValues cv = new ContentValues();
        cv.put(ActivitiesEntry.COLUMN_TITLE, title);
        cv.put(ActivitiesEntry.COLUMN_GAME_TYPE, type);
        cv.put(ActivitiesEntry.COLUMN_ORDER_INDEX, orderIndex);
        cv.put(ActivitiesEntry.COLUMN_TOPIC_ID, 1);
        cv.put(ActivitiesEntry.COLUMN_IS_LOCKED, 1);
        cv.put(ActivitiesEntry.COLUMN_XP_REWARD, 50);
        return db.insert(ActivitiesEntry.TABLE_NAME, null, cv);
    }

    public boolean updateActivityAdmin(int id, String title, String type, int orderIndex) {
        openWrite();
        ContentValues cv = new ContentValues();
        cv.put(ActivitiesEntry.COLUMN_TITLE, title);
        cv.put(ActivitiesEntry.COLUMN_GAME_TYPE, type);
        cv.put(ActivitiesEntry.COLUMN_ORDER_INDEX, orderIndex);
        return db.update(ActivitiesEntry.TABLE_NAME, cv, ActivitiesEntry._ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteActivityAdmin(int id) {
        openWrite();
        ContentValues cv = new ContentValues();
        cv.put(QuestionsEntry.COLUMN_ACTIVITY_ID, 0);
        db.update(QuestionsEntry.TABLE_NAME, cv, QuestionsEntry.COLUMN_ACTIVITY_ID + "=?", new String[]{String.valueOf(id)});
        db.delete(ProgressEntry.TABLE_NAME, ProgressEntry.COLUMN_ACTIVITY_ID + "=?", new String[]{String.valueOf(id)});
        return db.delete(ActivitiesEntry.TABLE_NAME, ActivitiesEntry._ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean updateQuestionsForActivity(int activityId, List<Integer> selectedQuestionIds) {
        openWrite();
        db.beginTransaction();
        try {
            ContentValues unassignCv = new ContentValues();
            unassignCv.put(QuestionsEntry.COLUMN_ACTIVITY_ID, 0);
            db.update(QuestionsEntry.TABLE_NAME, unassignCv, QuestionsEntry.COLUMN_ACTIVITY_ID + "=?", new String[]{String.valueOf(activityId)});

            ContentValues assignCv = new ContentValues();
            assignCv.put(QuestionsEntry.COLUMN_ACTIVITY_ID, activityId);
            for (int qId : selectedQuestionIds) {
                db.update(QuestionsEntry.TABLE_NAME, assignCv, QuestionsEntry._ID + "=?", new String[]{String.valueOf(qId)});
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
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
                    if (optionsJson != null && !optionsJson.isEmpty()) {
                        JSONArray jsonArray = new JSONArray(optionsJson);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            options.add(jsonArray.getString(i));
                        }
                    }
                } catch (JSONException e) { e.printStackTrace(); }
                
                list.add(new Question(id, activityId, type, text, null, image, answer, options));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @SuppressLint("Range")
    public List<Question> getRandomQuestions(int limit) {
        openRead();
        List<Question> list = new ArrayList<>();
        Cursor cursor = db.query(QuestionsEntry.TABLE_NAME, null, null, null, null, null, "RANDOM()", String.valueOf(limit));
        
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(QuestionsEntry._ID));
                int activityId = cursor.getInt(cursor.getColumnIndex(QuestionsEntry.COLUMN_ACTIVITY_ID));
                String type = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_QUESTION_TYPE));
                String text = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_QUESTION_TEXT));
                String image = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_IMAGE));
                String answer = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_ANSWER_TEXT));
                String optionsJson = cursor.getString(cursor.getColumnIndex(QuestionsEntry.COLUMN_OPTION_JSON));
                
                List<String> options = new ArrayList<>();
                try {
                    if (optionsJson != null && !optionsJson.isEmpty()) {
                        JSONArray jsonArray = new JSONArray(optionsJson);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            options.add(jsonArray.getString(i));
                        }
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

        // BỎ: Cập nhật global table ActivitiesEntry. 
        // Logic mở khóa giờ đây được tính toán động theo từng user trong getLessonsWithProgress.
        
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
    public List<Lesson> getLessonsWithProgress(int userId) {
        openRead();
        List<Lesson> lessons = new ArrayList<>();
        // Sắp xếp theo topic và thứ tự để logic mở khóa tuần tự hoạt động chính xác
        String sql = "SELECT a.*, p." + ProgressEntry.COLUMN_STARS_EARNED + ", p." + ProgressEntry.COLUMN_IS_COMPLETE + 
                     " FROM " + ActivitiesEntry.TABLE_NAME + " a " +
                     " LEFT JOIN " + ProgressEntry.TABLE_NAME + " p ON a." + ActivitiesEntry._ID + " = p." + ProgressEntry.COLUMN_ACTIVITY_ID + 
                     " AND p." + ProgressEntry.COLUMN_USER_ID + " = ?" +
                     " ORDER BY a." + ActivitiesEntry.COLUMN_TOPIC_ID + " ASC, a." + ActivitiesEntry.COLUMN_ORDER_INDEX + " ASC";
        
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)});
        
        boolean previousComplete = true; // Bài đầu tiên của mỗi topic mặc định được mở
        int currentTopicId = -1;

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(ActivitiesEntry._ID));
                int topicId = cursor.getInt(cursor.getColumnIndex(ActivitiesEntry.COLUMN_TOPIC_ID));
                String title = cursor.getString(cursor.getColumnIndex(ActivitiesEntry.COLUMN_TITLE));
                String icon = cursor.getString(cursor.getColumnIndex(ActivitiesEntry.COLUMN_GAME_TYPE));
                int stars = cursor.getInt(cursor.getColumnIndex(ProgressEntry.COLUMN_STARS_EARNED));
                int globalLocked = cursor.getInt(cursor.getColumnIndex(ActivitiesEntry.COLUMN_IS_LOCKED));
                boolean isComplete = cursor.getInt(cursor.getColumnIndex(ProgressEntry.COLUMN_IS_COMPLETE)) == 1;
                int orderIndex = cursor.getInt(cursor.getColumnIndex(ActivitiesEntry.COLUMN_ORDER_INDEX));

                // Nếu chuyển chủ đề mới, reset trạng thái mở khóa cho bài đầu tiên
                if (topicId != currentTopicId) {
                    currentTopicId = topicId;
                    previousComplete = true;
                }

                // Bài học bị khóa nếu: 
                // 1. Admin đặt khóa mặc định (globalLocked == 1) 
                // 2. VÀ bài học trước đó chưa hoàn thành (!previousComplete)
                // Lưu ý: Nếu globalLocked == 0 thì luôn mở.
                boolean isLocked = (globalLocked == 1) && !previousComplete;

                lessons.add(new Lesson(id, title, icon, stars, isLocked, isComplete, orderIndex));
                
                previousComplete = isComplete;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lessons;
    }

    @SuppressLint("Range")
    public ProgressSummary getUserProgressSummary(int userId) {
        openRead();
        ProgressSummary summary = new ProgressSummary();
        summary.topicStats = new ArrayList<>();
        
        try {
            Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM " + ActivitiesEntry.TABLE_NAME, null);
            if (c1.moveToFirst()) summary.totalActivities = c1.getInt(0);
            c1.close();
            
            Cursor c2 = db.rawQuery("SELECT COUNT(*) FROM " + ProgressEntry.TABLE_NAME + " WHERE " + ProgressEntry.COLUMN_USER_ID + " = ? AND " + ProgressEntry.COLUMN_IS_COMPLETE + " = 1", new String[]{String.valueOf(userId)});
            if (c2.moveToFirst()) summary.completedActivities = c2.getInt(0);
            c2.close();
            
            summary.totalStarsPossible = summary.totalActivities * 3;
            
            Cursor c3 = db.rawQuery("SELECT SUM(" + ProgressEntry.COLUMN_STARS_EARNED + ") FROM " + ProgressEntry.TABLE_NAME + " WHERE " + ProgressEntry.COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
            if (c3.moveToFirst()) summary.totalStarsEarned = c3.getInt(0);
            c3.close();

            String sqlTopic = "SELECT t." + TopicEntry.COLUMN_TITLE + ", COUNT(a." + ActivitiesEntry._ID + ") as total, " +
                    "SUM(CASE WHEN p." + ProgressEntry.COLUMN_IS_COMPLETE + " = 1 THEN 1 ELSE 0 END) as done " +
                    "FROM " + TopicEntry.TABLE_NAME + " t " +
                    "JOIN " + ActivitiesEntry.TABLE_NAME + " a ON t." + TopicEntry._ID + " = a." + ActivitiesEntry.COLUMN_TOPIC_ID + " " +
                    "LEFT JOIN " + ProgressEntry.TABLE_NAME + " p ON a." + ActivitiesEntry._ID + " = p." + ProgressEntry.COLUMN_ACTIVITY_ID + " AND p." + ProgressEntry.COLUMN_USER_ID + " = ? " +
                    "GROUP BY t." + TopicEntry._ID;
            
            Cursor c4 = db.rawQuery(sqlTopic, new String[]{String.valueOf(userId)});
            if (c4.moveToFirst()) {
                do {
                    TopicProgress tp = new TopicProgress();
                    tp.title = c4.getString(0);
                    tp.total = c4.getInt(1);
                    tp.completed = c4.getInt(2);
                    summary.topicStats.add(tp);
                } while (c4.moveToNext());
            }
            c4.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return summary;
    }

    @SuppressLint("Range")
    public void checkAndUnlockAchievements(int userId) {
        openRead();
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

    @SuppressLint("Range")
    public List<Achievement> getAllAchievementsAdmin() {
        openRead();
        List<Achievement> list = new ArrayList<>();
        Cursor cursor = db.query(AchievementEntry.TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new Achievement(
                    cursor.getInt(cursor.getColumnIndex(AchievementEntry._ID)),
                    cursor.getString(cursor.getColumnIndex(AchievementEntry.COLUMN_TITLE)),
                    cursor.getString(cursor.getColumnIndex(AchievementEntry.COLUMN_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndex(AchievementEntry.COLUMN_ICON)),
                    cursor.getString(cursor.getColumnIndex(AchievementEntry.COLUMN_TYPE)),
                    cursor.getInt(cursor.getColumnIndex(AchievementEntry.COLUMN_REQUIRED_VALUE)),
                    false,
                    0
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean addAchievementAdmin(String title, String description, String icon, String type, int requiredValue) {
        openWrite();
        ContentValues cv = new ContentValues();
        cv.put(AchievementEntry.COLUMN_TITLE, title);
        cv.put(AchievementEntry.COLUMN_DESCRIPTION, description);
        cv.put(AchievementEntry.COLUMN_ICON, icon);
        cv.put(AchievementEntry.COLUMN_TYPE, type);
        cv.put(AchievementEntry.COLUMN_REQUIRED_VALUE, requiredValue);
        return db.insert(AchievementEntry.TABLE_NAME, null, cv) != -1;
    }

    public boolean updateAchievementAdmin(int id, String title, String description, String icon, String type, int requiredValue) {
        openWrite();
        ContentValues cv = new ContentValues();
        cv.put(AchievementEntry.COLUMN_TITLE, title);
        cv.put(AchievementEntry.COLUMN_DESCRIPTION, description);
        cv.put(AchievementEntry.COLUMN_ICON, icon);
        cv.put(AchievementEntry.COLUMN_TYPE, type);
        cv.put(AchievementEntry.COLUMN_REQUIRED_VALUE, requiredValue);
        return db.update(AchievementEntry.TABLE_NAME, cv, AchievementEntry._ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteAchievementAdmin(int id) {
        openWrite();
        db.delete(UserAchievementEntry.TABLE_NAME, UserAchievementEntry.COLUMN_ACHIEVEMENT_ID + "=?", new String[]{String.valueOf(id)});
        return db.delete(AchievementEntry.TABLE_NAME, AchievementEntry._ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public void seedDataIfNeeded() {
        openRead();
        Cursor cursor = db.query(ActivitiesEntry.TABLE_NAME, null, null, null, null, null, null);
        if (cursor.getCount() == 0) {
            openWrite();
            
            ContentValues tc = new ContentValues();
            tc.put(TopicEntry.COLUMN_TITLE, "Toán cơ bản");
            tc.put(TopicEntry.COLUMN_INDEX, 1);
            long topicId = db.insert(TopicEntry.TABLE_NAME, null, tc);

            long act1 = addActivityInternal((int)topicId, "Làm quen số 1-5", "quiz", 50, 0, 1);
            addQuestionInternal((int)act1, "Có bao nhiêu Gấu Trúc nào?", "panda", "1", "[\"1\", \"2\", \"3\", \"5\"]");
            addQuestionInternal((int)act1, "Đếm xem có bao nhiêu Chú Thỏ?", "rabbit", "3", "[\"2\", \"3\", \"4\", \"1\"]");
            addQuestionInternal((int)act1, "Có mấy Chú Chó ở đây nhỉ?", "dog", "2", "[\"1\", \"2\", \"4\", \"3\"]");

            long act2 = addActivityInternal((int)topicId, "Bé tập đếm đến 10", "quiz", 60, 1, 2);
            addQuestionInternal((int)act2, "Số 'Bảy' viết như thế nào?", "fox", "7", "[\"5\", \"6\", \"7\", \"8\"]");
            addQuestionInternal((int)act2, "Đâu là số 'Mười'?", "panda", "10", "[\"1\", \"0\", \"10\", \"100\"]");
        }
        cursor.close();
    }

    private long addActivityInternal(int topicId, String title, String type, int xp, int locked, int index) {
        ContentValues cv = new ContentValues();
        cv.put(ActivitiesEntry.COLUMN_TOPIC_ID, topicId);
        cv.put(ActivitiesEntry.COLUMN_TITLE, title);
        cv.put(ActivitiesEntry.COLUMN_GAME_TYPE, type);
        cv.put(ActivitiesEntry.COLUMN_XP_REWARD, xp);
        cv.put(ActivitiesEntry.COLUMN_IS_LOCKED, locked);
        cv.put(ActivitiesEntry.COLUMN_ORDER_INDEX, index);
        return db.insert(ActivitiesEntry.TABLE_NAME, null, cv);
    }

    private void addQuestionInternal(int actId, String text, String img, String ans, String optJson) {
        ContentValues cv = new ContentValues();
        cv.put(QuestionsEntry.COLUMN_ACTIVITY_ID, actId);
        cv.put(QuestionsEntry.COLUMN_QUESTION_TYPE, "quiz");
        cv.put(QuestionsEntry.COLUMN_QUESTION_TEXT, text);
        cv.put(QuestionsEntry.COLUMN_IMAGE, img);
        cv.put(QuestionsEntry.COLUMN_ANSWER_TEXT, ans);
        cv.put(QuestionsEntry.COLUMN_OPTION_JSON, optJson);
        db.insert(QuestionsEntry.TABLE_NAME, null, cv);
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
    
    public static class ProgressSummary {
        public int totalActivities = 0;
        public int completedActivities = 0;
        public int totalStarsEarned = 0;
        public int totalStarsPossible = 0;
        public List<TopicProgress> topicStats = new ArrayList<>();
    }
    
    public static class TopicProgress {
        public String title;
        public int total;
        public int completed;
    }
}
