package com.example.mathkid.activities;

import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.os.Bundle;
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
import java.util.Map;

public class QuizActivity extends AppCompatActivity {

    private ProgressBar quizProgressBar;
    private TextView txtQuestionIndex, txtQuestionText;
    private ImageView imgQuestion;
    private View btnExit;
    private FrameLayout[] optionViews = new FrameLayout[4];
    private TextView[] optionTexts = new TextView[4];

    // Drag & Drop
    private View dropZone;
    private TextView txtDroppedValue;
    private LinearLayout dragContainer;

    // Matching
    private LinearLayout layoutMatchingPairs;
    private Map<String, String> matchingData = new HashMap<>();
    private int matchingCorrectCount = 0;
    private View selectedLeftView = null;
    private String selectedLeftValue = "";

    // Comparison
    private LinearLayout comparisonContainer, compareDragArea;
    private TextView txtCompareLeft, txtCompareRight, txtDroppedCompare;
    private View dropZoneCompare;

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int correctAnswersCount = 0;
    private String correctAnswer;
    private int activityId;

    private UserDAO userDAO;
    private boolean isAnswered = false;
    private boolean currentQuestionFirstTry = true; // Theo dõi xem có đúng ngay lần đầu không

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        userDAO = new UserDAO(this);
        activityId = getIntent().getIntExtra("activity_id", -1);

        initViews();
        loadData();
        displayQuestion();

        btnExit.setOnClickListener(v -> finish());
    }

    private void initViews() {
        quizProgressBar = findViewById(R.id.quizProgressBar);
        txtQuestionIndex = findViewById(R.id.txtQuestionIndex);
        txtQuestionText = findViewById(R.id.txtQuestionText);
        imgQuestion = findViewById(R.id.imgQuestion);
        btnExit = findViewById(R.id.btnExit);

        // Quiz
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

        // Drag & Drop
        dropZone = findViewById(R.id.dropZone);
        txtDroppedValue = findViewById(R.id.txtDroppedValue);
        dragContainer = findViewById(R.id.dragContainer);
        setupDropZone(dropZone);

        // Matching
        layoutMatchingPairs = findViewById(R.id.layoutMatchingPairs);

        // Comparison
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
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.animate().scaleX(1.2f).scaleY(1.2f).alpha(0.8f).setDuration(200).start();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                case DragEvent.ACTION_DRAG_ENDED:
                    v.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(200).start();
                    return true;
                case DragEvent.ACTION_DROP:
                    if (isAnswered) return false;
                    ClipData data = event.getClipData();
                    if (data != null && data.getItemCount() > 0) {
                        String value = data.getItemAt(0).getText().toString();
                        checkAnswer(value);
                    }
                    return true;
            }
            return false;
        });
    }

    private void loadData() {
        if (activityId != -1) questionList = userDAO.getQuestions(activityId);
        if (questionList == null || questionList.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayQuestion() {
        isAnswered = false;
        currentQuestionFirstTry = true; // Reset cho câu mới
        
        if (currentQuestionIndex < questionList.size()) {
            Question q = questionList.get(currentQuestionIndex);
            
            int progress = (int) (((float) (currentQuestionIndex) / questionList.size()) * 100);
            ObjectAnimator.ofInt(quizProgressBar, "progress", quizProgressBar.getProgress(), progress).setDuration(600).start();

            txtQuestionIndex.setText((currentQuestionIndex + 1) + "/" + questionList.size());
            txtQuestionText.setText(q.getText());
            correctAnswer = q.getAnswer();

            if (q.getImage() != null && !q.getImage().isEmpty()) {
                int resId = getResources().getIdentifier(q.getImage(), "drawable", getPackageName());
                imgQuestion.setImageResource(resId != 0 ? resId : R.drawable.panda);
                imgQuestion.setVisibility(View.VISIBLE);
            } else imgQuestion.setVisibility(View.GONE);

            if ("drag".equals(q.getType())) showDragLayout(q.getOptions());
            else if ("matching".equals(q.getType())) showMatchingLayout(q.getOptions());
            else if ("comparison".equals(q.getType())) showComparisonLayout(q.getOptions());
            else showQuizLayout(q.getOptions());
        } else {
            quizProgressBar.setProgress(100);
            Intent intent = new Intent(this, QuizResult.class);
            intent.putExtra("correct_count", correctAnswersCount);
            intent.putExtra("total_questions", questionList.size());
            intent.putExtra("xp_earned", correctAnswersCount * 10);
            intent.putExtra("activity_id", activityId);
            startActivity(intent);
            finish();
        }
    }

    private void showQuizLayout(List<String> options) {
        findViewById(R.id.optionsGrid).setVisibility(View.VISIBLE);
        findViewById(R.id.dragContainer).setVisibility(View.GONE);
        findViewById(R.id.matchingContainer).setVisibility(View.GONE);
        comparisonContainer.setVisibility(View.GONE);
        dropZone.setVisibility(View.GONE);
        for (int i = 0; i < 4; i++) {
            if (i < options.size()) {
                optionTexts[i].setText(options.get(i));
                optionViews[i].setVisibility(View.VISIBLE);
            } else optionViews[i].setVisibility(View.GONE);
        }
    }

    private void showDragLayout(List<String> options) {
        findViewById(R.id.optionsGrid).setVisibility(View.GONE);
        findViewById(R.id.dragContainer).setVisibility(View.VISIBLE);
        findViewById(R.id.matchingContainer).setVisibility(View.GONE);
        comparisonContainer.setVisibility(View.GONE);
        dropZone.setVisibility(View.VISIBLE);
        txtDroppedValue.setText("?");
        dragContainer.removeAllViews();
        for (String option : options) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_drag_option, dragContainer, false);
            ((TextView) view.findViewById(R.id.txtDragOption)).setText(option);
            view.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN && !isAnswered) {
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    ClipData data = ClipData.newPlainText("value", option);
                    v.startDragAndDrop(data, new View.DragShadowBuilder(v), null, 0);
                    return true;
                }
                return false;
            });
            dragContainer.addView(view);
        }
    }

    private void showMatchingLayout(List<String> optionsJson) {
        findViewById(R.id.optionsGrid).setVisibility(View.GONE);
        findViewById(R.id.dragContainer).setVisibility(View.GONE);
        findViewById(R.id.matchingContainer).setVisibility(View.VISIBLE);
        comparisonContainer.setVisibility(View.GONE);
        dropZone.setVisibility(View.GONE);
        layoutMatchingPairs.removeAllViews();
        matchingData.clear();
        matchingCorrectCount = 0;
        selectedLeftView = null;

        List<String> leftItems = new ArrayList<>();
        List<String> rightItems = new ArrayList<>();

        try {
            JSONArray arr = new JSONArray(optionsJson.get(0));
            for (int i = 0; i < arr.length(); i++) {
                String pair = arr.getString(i);
                String[] split = pair.split(":");
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
            View cardL = view.findViewById(R.id.leftItem);
            View cardR = view.findViewById(R.id.rightItem);

            txtL.setText(leftItems.get(i));
            txtR.setText(rightItems.get(i));

            final String lVal = leftItems.get(i);
            final String rVal = rightItems.get(i);

            cardL.setOnClickListener(v -> {
                if (selectedLeftView != null) selectedLeftView.setAlpha(1.0f);
                selectedLeftView = v;
                selectedLeftValue = lVal;
                v.setAlpha(0.5f);
            });

            cardR.setOnClickListener(v -> {
                if (selectedLeftView == null) {
                    Toast.makeText(this, "Hãy chọn ô bên trái trước!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (matchingData.get(selectedLeftValue).equals(rVal)) {
                    v.setVisibility(View.INVISIBLE);
                    selectedLeftView.setVisibility(View.INVISIBLE);
                    selectedLeftView = null;
                    matchingCorrectCount++;
                    if (matchingCorrectCount == matchingData.size()) checkAnswer("correct_matching");
                } else {
                    Toast.makeText(this, "Sai rồi, thử lại nhé!", Toast.LENGTH_SHORT).show();
                    selectedLeftView.setAlpha(1.0f);
                    selectedLeftView = null;
                    currentQuestionFirstTry = false; // Đánh dấu là đã làm sai cặp này
                }
            });
            layoutMatchingPairs.addView(view);
        }
    }

    private void showComparisonLayout(List<String> options) {
        findViewById(R.id.optionsGrid).setVisibility(View.GONE);
        findViewById(R.id.dragContainer).setVisibility(View.GONE);
        findViewById(R.id.matchingContainer).setVisibility(View.GONE);
        comparisonContainer.setVisibility(View.VISIBLE);
        dropZone.setVisibility(View.GONE);
        txtDroppedCompare.setText("?");

        if (options.size() >= 2) {
            txtCompareLeft.setText(options.get(0));
            txtCompareRight.setText(options.get(1));
        }

        compareDragArea.removeAllViews();
        String[] signs = {">", "<", "="};
        for (String sign : signs) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_drag_option, compareDragArea, false);
            TextView txtSign = view.findViewById(R.id.txtDragOption);
            txtSign.setText(sign);
            txtSign.setTextSize(32);

            view.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN && !isAnswered) {
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    ClipData data = ClipData.newPlainText("value", sign);
                    v.startDragAndDrop(data, new View.DragShadowBuilder(v), null, 0);
                    return true;
                }
                return false;
            });
            compareDragArea.addView(view);
        }
    }

    private void checkAnswer(String selectedAnswer) {
        if (isAnswered) return;
        
        boolean isCorrect = false;
        if ("correct_matching".equals(selectedAnswer)) isCorrect = true;
        else if (selectedAnswer.equals(correctAnswer)) isCorrect = true;

        if (isCorrect) {
            // Chỉ cộng điểm nếu đúng ngay lần đầu để tính sao chính xác
            if (currentQuestionFirstTry) {
                correctAnswersCount++;
            }
            
            if (dropZone.getVisibility() == View.VISIBLE) txtDroppedValue.setText(selectedAnswer);
            if (comparisonContainer.getVisibility() == View.VISIBLE) txtDroppedCompare.setText(selectedAnswer);

            Toast.makeText(this, "Chính xác! ✨", Toast.LENGTH_SHORT).show();
            isAnswered = true; // Đúng rồi thì khóa câu này
            
            txtQuestionText.postDelayed(() -> {
                currentQuestionIndex++;
                displayQuestion();
            }, 1000);
        } else {
            // SAI: Cho phép chọn lại nhưng đánh dấu không được điểm hoàn hảo
            Toast.makeText(this, "Chưa đúng rồi, bé thử lại nhé! ❤️", Toast.LENGTH_SHORT).show();
            currentQuestionFirstTry = false; 
            
            if (dropZone.getVisibility() == View.VISIBLE) txtDroppedValue.setText("?");
            if (comparisonContainer.getVisibility() == View.VISIBLE) txtDroppedCompare.setText("?");
            
            // Lưu ý: isAnswered vẫn là false, cho phép chọn tiếp
        }
    }
}
