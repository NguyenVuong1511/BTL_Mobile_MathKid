package com.example.mathkid.activities;

import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.DragEvent;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mathkid.R;
import com.example.mathkid.database.UserDAO;
import com.example.mathkid.model.Question;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExamActivity extends AppCompatActivity {

    private ProgressBar examProgressBar;
    private TextView txtQuestionIndex, txtQuestionText, txtTimer;
    private ImageView imgQuestion;
    private FrameLayout[] optionViews = new FrameLayout[4];
    private TextView[] optionTexts = new TextView[4];

    private View dropZone;
    private TextView txtDroppedValue;
    private LinearLayout dragContainer;

    private LinearLayout layoutMatchingPairs;
    private Map<String, String> matchingData = new HashMap<>();
    private int matchingCorrectCount = 0;
    private View selectedLeftView = null;
    private String selectedLeftValue = "";

    private LinearLayout comparisonContainer, compareDragArea;
    private TextView txtCompareLeft, txtCompareRight, txtDroppedCompare;
    private View dropZoneCompare;

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int correctAnswersCount = 0;
    private String correctAnswer;

    private UserDAO userDAO;
    private boolean isAnswered = false;
    
    private CountDownTimer countDownTimer;
    private static final long EXAM_TIME_LIMIT = 300000; // 5 phút (ms)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        userDAO = new UserDAO(this);

        initViews();
        loadExamQuestions();
        startTimer();
        displayQuestion();
    }

    private void initViews() {
        examProgressBar = findViewById(R.id.examProgressBar);
        txtQuestionIndex = findViewById(R.id.txtQuestionIndex);
        txtQuestionText = findViewById(R.id.txtQuestionText);
        txtTimer = findViewById(R.id.txtTimer);
        imgQuestion = findViewById(R.id.imgQuestion);

        optionViews[0] = findViewById(R.id.option1);
        optionViews[1] = findViewById(R.id.option2);
        optionViews[2] = findViewById(R.id.option3);
        optionViews[3] = findViewById(R.id.option4);

        for (int i = 0; i < 4; i++) {
            final int index = i;
            optionTexts[i] = (TextView) ((android.view.ViewGroup) optionViews[i]).getChildAt(0);
            optionViews[index].setOnClickListener(v -> {
                if (!isAnswered) checkAnswer(optionTexts[index].getText().toString());
            });
        }

        dropZone = findViewById(R.id.dropZone);
        txtDroppedValue = findViewById(R.id.txtDroppedValue);
        dragContainer = findViewById(R.id.dragContainer);
        setupDropZone(dropZone);

        layoutMatchingPairs = findViewById(R.id.layoutMatchingPairs);

        comparisonContainer = findViewById(R.id.comparisonContainer);
        compareDragArea = findViewById(R.id.compareDragArea);
        txtCompareLeft = findViewById(R.id.txtCompareLeft);
        txtCompareRight = findViewById(R.id.txtCompareRight);
        txtDroppedCompare = findViewById(R.id.txtDroppedCompare);
        dropZoneCompare = findViewById(R.id.dropZoneCompare);
        setupDropZone(dropZoneCompare);
    }

    private void setupDropZone(View zone) {
        zone.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                case DragEvent.ACTION_DROP:
                    if (isAnswered) return false;
                    ClipData data = event.getClipData();
                    if (data != null && data.getItemCount() > 0) {
                        checkAnswer(data.getItemAt(0).getText().toString());
                    }
                    return true;
            }
            return false;
        });
    }

    private void loadExamQuestions() {
        // Bài thi chuẩn gồm 20 câu hỏi ngẫu nhiên
        questionList = userDAO.getRandomQuestions(20);
        if (questionList == null || questionList.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy câu hỏi cho bài thi!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(EXAM_TIME_LIMIT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                txtTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                
                if (millisUntilFinished < 60000) { // Dưới 1 phút đổi sang màu đỏ
                    txtTimer.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                }
            }

            @Override
            public void onFinish() {
                Toast.makeText(ExamActivity.this, "Hết giờ làm bài!", Toast.LENGTH_LONG).show();
                finishExam();
            }
        }.start();
    }

    private void displayQuestion() {
        isAnswered = false;
        if (currentQuestionIndex < questionList.size()) {
            Question q = questionList.get(currentQuestionIndex);
            
            int progress = (int) (((float) (currentQuestionIndex) / questionList.size()) * 100);
            ObjectAnimator.ofInt(examProgressBar, "progress", examProgressBar.getProgress(), progress).setDuration(500).start();

            txtQuestionIndex.setText((currentQuestionIndex + 1) + "/" + questionList.size());
            txtQuestionText.setText(q.getText());
            correctAnswer = q.getAnswer();

            if (q.getImage() != null && !q.getImage().isEmpty()) {
                int resId = getResources().getIdentifier(q.getImage(), "drawable", getPackageName());
                imgQuestion.setImageResource(resId != 0 ? resId : R.drawable.panda);
                imgQuestion.setVisibility(View.VISIBLE);
            } else imgQuestion.setVisibility(View.GONE);

            // Hide all specialized containers first
            findViewById(R.id.optionsGrid).setVisibility(View.GONE);
            findViewById(R.id.dragContainer).setVisibility(View.GONE);
            findViewById(R.id.matchingContainer).setVisibility(View.GONE);
            comparisonContainer.setVisibility(View.GONE);
            dropZone.setVisibility(View.GONE);

            if ("drag".equals(q.getType())) showDragLayout(q.getOptions());
            else if ("matching".equals(q.getType())) showMatchingLayout(q.getOptions());
            else if ("comparison".equals(q.getType())) showComparisonLayout(q.getOptions());
            else showQuizLayout(q.getOptions());
        } else {
            finishExam();
        }
    }

    private void showQuizLayout(List<String> options) {
        findViewById(R.id.optionsGrid).setVisibility(View.VISIBLE);
        for (int i = 0; i < 4; i++) {
            if (i < options.size()) {
                optionTexts[i].setText(options.get(i));
                optionViews[i].setVisibility(View.VISIBLE);
            } else optionViews[i].setVisibility(View.GONE);
        }
    }

    private void showDragLayout(List<String> options) {
        findViewById(R.id.dragContainer).setVisibility(View.VISIBLE);
        dropZone.setVisibility(View.VISIBLE);
        txtDroppedValue.setText("?");
        dragContainer.removeAllViews();
        for (String option : options) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_drag_option, dragContainer, false);
            ((TextView) view.findViewById(R.id.txtDragOption)).setText(option);
            view.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN && !isAnswered) {
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    v.startDragAndDrop(ClipData.newPlainText("value", option), new View.DragShadowBuilder(v), null, 0);
                    return true;
                }
                return false;
            });
            dragContainer.addView(view);
        }
    }

    private void showMatchingLayout(List<String> optionsJson) {
        findViewById(R.id.matchingContainer).setVisibility(View.VISIBLE);
        layoutMatchingPairs.removeAllViews();
        matchingData.clear();
        matchingCorrectCount = 0;
        selectedLeftView = null;
        List<String> leftItems = new ArrayList<>();
        List<String> rightItems = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(optionsJson.get(0));
            for (int i = 0; i < arr.length(); i++) {
                String[] split = arr.getString(i).split(":");
                leftItems.add(split[0]);
                rightItems.add(split[1]);
                matchingData.put(split[0], split[1]);
            }
        } catch (Exception e) { e.printStackTrace(); }
        Collections.shuffle(rightItems);
        for (int i = 0; i < leftItems.size(); i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_matching_pair, layoutMatchingPairs, false);
            TextView txtL = view.findViewById(R.id.txtLeft);
            TextView txtR = view.findViewById(R.id.txtRight);
            txtL.setText(leftItems.get(i));
            txtR.setText(rightItems.get(i));
            final String lVal = leftItems.get(i);
            final String rVal = rightItems.get(i);
            view.findViewById(R.id.leftItem).setOnClickListener(v -> {
                if (selectedLeftView != null) selectedLeftView.setAlpha(1.0f);
                selectedLeftView = v; selectedLeftValue = lVal; v.setAlpha(0.5f);
            });
            view.findViewById(R.id.rightItem).setOnClickListener(v -> {
                if (selectedLeftView == null) return;
                if (matchingData.get(selectedLeftValue).equals(rVal)) {
                    v.setVisibility(View.INVISIBLE); selectedLeftView.setVisibility(View.INVISIBLE);
                    selectedLeftView = null; matchingCorrectCount++;
                    if (matchingCorrectCount == matchingData.size()) checkAnswer("correct_matching");
                } else { selectedLeftView.setAlpha(1.0f); selectedLeftView = null; }
            });
            layoutMatchingPairs.addView(view);
        }
    }

    private void showComparisonLayout(List<String> options) {
        comparisonContainer.setVisibility(View.VISIBLE);
        txtDroppedCompare.setText("?");
        if (options.size() >= 2) {
            txtCompareLeft.setText(options.get(0));
            txtCompareRight.setText(options.get(1));
        }
        compareDragArea.removeAllViews();
        String[] signs = {">", "<", "="};
        for (String sign : signs) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_drag_option, compareDragArea, false);
            ((TextView) view.findViewById(R.id.txtDragOption)).setText(sign);
            view.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN && !isAnswered) {
                    v.startDragAndDrop(ClipData.newPlainText("value", sign), new View.DragShadowBuilder(v), null, 0);
                    return true;
                }
                return false;
            });
            compareDragArea.addView(view);
        }
    }

    private void checkAnswer(String selectedAnswer) {
        if (isAnswered) return;
        isAnswered = true;
        boolean isCorrect = false;
        if ("correct_matching".equals(selectedAnswer)) isCorrect = true;
        else if (selectedAnswer.equals(correctAnswer)) {
            isCorrect = true;
            if (dropZone.getVisibility() == View.VISIBLE) txtDroppedValue.setText(selectedAnswer);
            if (comparisonContainer.getVisibility() == View.VISIBLE) txtDroppedCompare.setText(selectedAnswer);
        }

        if (isCorrect) correctAnswersCount++;
        
        // Trong bài thi, KHÔNG thông báo đúng sai ngay lập tức để tăng độ khó
        // Và luôn tự động chuyển câu sau 0.5s
        txtQuestionText.postDelayed(() -> {
            currentQuestionIndex++;
            displayQuestion();
        }, 500);
    }

    private void finishExam() {
        if (countDownTimer != null) countDownTimer.cancel();
        examProgressBar.setProgress(100);
        
        Intent intent = new Intent(this, QuizResult.class);
        intent.putExtra("correct_count", correctAnswersCount);
        intent.putExtra("total_questions", questionList.size());
        // Bài thi thưởng XP cực cao: 30 XP mỗi câu đúng
        intent.putExtra("xp_earned", correctAnswersCount * 30);
        intent.putExtra("activity_id", -1); // Không ảnh hưởng tiến trình
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
