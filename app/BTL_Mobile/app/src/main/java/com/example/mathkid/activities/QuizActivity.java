package com.example.mathkid.activities;

import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Base64;
import android.view.DragEvent;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
    private boolean isPreviewMode = false;

    private UserDAO userDAO;
    private boolean isAnswered = false;
    private boolean currentQuestionFirstTry = true;

    private MediaPlayer correctPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quiz);

        userDAO = new UserDAO(this);
        activityId = getIntent().getIntExtra("activity_id", -1);
        isPreviewMode = getIntent().getBooleanExtra("IS_PREVIEW", false);

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

        // Quiz Options
        optionViews[0] = findViewById(R.id.option1);
        optionViews[1] = findViewById(R.id.option2);
        optionViews[2] = findViewById(R.id.option3);
        optionViews[3] = findViewById(R.id.option4);

        for (int i = 0; i < 4; i++) {
            final int index = i;
            if (optionViews[i] != null) {
                View innerView = ((android.view.ViewGroup) optionViews[i]).getChildAt(0);
                if (innerView instanceof TextView) {
                    optionTexts[i] = (TextView) innerView;
                }
                optionViews[index].setOnClickListener(v -> {
                    if (!isAnswered && optionTexts[index] != null) 
                        checkAnswer(optionTexts[index].getText().toString());
                });
            }
        }

        // Drag & Drop
        dropZone = findViewById(R.id.dropZone);
        txtDroppedValue = findViewById(R.id.txtDroppedValue);
        dragContainer = findViewById(R.id.dragContainer);
        if (dropZone != null) setupDropZone(dropZone);

        // Matching
        layoutMatchingPairs = findViewById(R.id.layoutMatchingPairs);

        // Comparison
        comparisonContainer = findViewById(R.id.comparisonContainer);
        compareDragArea = findViewById(R.id.compareDragArea);
        txtCompareLeft = findViewById(R.id.txtCompareLeft);
        txtCompareRight = findViewById(R.id.txtCompareRight);
        txtDroppedCompare = findViewById(R.id.txtDroppedCompare);
        dropZoneCompare = findViewById(R.id.dropZoneCompare);
        if (dropZoneCompare != null) setupDropZone(dropZoneCompare);
    }

    private void setupDropZone(View zone) {
        zone.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.animate().scaleX(1.1f).scaleY(1.1f).alpha(0.7f).setDuration(200).start();
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
        if (isPreviewMode) {
            int previewId = getIntent().getIntExtra("PREVIEW_QUESTION_ID", -1);
            List<Question> all = userDAO.getAllQuestions();
            questionList = new ArrayList<>();
            for (Question q : all) {
                if (q.getId() == previewId) {
                    questionList.add(q);
                    break;
                }
            }
        } else if (activityId != -1) {
            questionList = userDAO.getQuestions(activityId);
        }

        if (questionList == null || questionList.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu câu hỏi!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayQuestion() {
        isAnswered = false;
        currentQuestionFirstTry = true;

        if (currentQuestionIndex < questionList.size()) {
            Question q = questionList.get(currentQuestionIndex);

            int progress = (int) (((float) (currentQuestionIndex) / questionList.size()) * 100);
            if (quizProgressBar != null) {
                ObjectAnimator.ofInt(quizProgressBar, "progress", quizProgressBar.getProgress(), progress).setDuration(600).start();
            }

            if (txtQuestionIndex != null)
                txtQuestionIndex.setText((currentQuestionIndex + 1) + "/" + questionList.size());
            if (txtQuestionText != null) txtQuestionText.setText(q.getText());
            correctAnswer = q.getAnswer();

            // Xử lý hiển thị ảnh
            if (imgQuestion != null) {
                if (q.getImage() != null && !q.getImage().isEmpty()) {
                    if (q.getImage().length() > 100) {
                        try {
                            byte[] decodedString = Base64.decode(q.getImage(), Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            if (decodedByte != null) {
                                imgQuestion.setImageBitmap(decodedByte);
                                imgQuestion.setVisibility(View.VISIBLE);
                            } else {
                                imgQuestion.setVisibility(View.GONE);
                            }
                        } catch (Exception e) {
                            imgQuestion.setVisibility(View.GONE);
                        }
                    } else {
                        int resId = getResources().getIdentifier(q.getImage(), "drawable", getPackageName());
                        imgQuestion.setImageResource(resId != 0 ? resId : R.drawable.panda);
                        imgQuestion.setVisibility(View.VISIBLE);
                    }
                } else {
                    imgQuestion.setVisibility(View.GONE);
                }
            }

            if ("drag".equals(q.getType())) showDragLayout(q.getOptions());
            else if ("matching".equals(q.getType())) showMatchingLayout(q.getOptions());
            else if ("comparison".equals(q.getType())) showComparisonLayout(q.getOptions());
            else showQuizLayout(q.getOptions());
        } else {
            handleQuizFinish();
        }
    }

    private void handleQuizFinish() {
        if (isPreviewMode) {
            Toast.makeText(this, "Kết thúc xem trước!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (quizProgressBar != null) quizProgressBar.setProgress(100);
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
        setAllLayoutsInvisible();
        findViewById(R.id.optionsGrid).setVisibility(View.VISIBLE);
        for (int i = 0; i < 4; i++) {
            if (optionViews[i] != null) {
                if (options != null && i < options.size()) {
                    if (optionTexts[i] != null) optionTexts[i].setText(options.get(i));
                    optionViews[i].setVisibility(View.VISIBLE);
                    optionViews[i].setAlpha(1.0f);
                } else optionViews[i].setVisibility(View.GONE);
            }
        }
    }

    private void showDragLayout(List<String> options) {
        setAllLayoutsInvisible();
        if (dragContainer != null) {
            dragContainer.setVisibility(View.VISIBLE);
            dragContainer.removeAllViews();
            if (options != null) {
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
        }
        if (dropZone != null) {
            dropZone.setVisibility(View.VISIBLE);
            if (txtDroppedValue != null) txtDroppedValue.setText("?");
        }
    }

    private void showMatchingLayout(List<String> optionsJson) {
        setAllLayoutsInvisible();
        View matchingContainer = findViewById(R.id.matchingContainer);
        if (matchingContainer != null) matchingContainer.setVisibility(View.VISIBLE);
        if (layoutMatchingPairs != null) {
            layoutMatchingPairs.removeAllViews();
            matchingData.clear();
            matchingCorrectCount = 0;
            selectedLeftView = null;
            List<String> leftItems = new ArrayList<>();
            List<String> rightItems = new ArrayList<>();
            try {
                if (optionsJson != null && !optionsJson.isEmpty()) {
                    JSONArray arr = new JSONArray(optionsJson.get(0));
                    for (int i = 0; i < arr.length(); i++) {
                        String pair = arr.getString(i);
                        String[] split = pair.split(":");
                        if (split.length >= 2) {
                            leftItems.add(split[0]);
                            rightItems.add(split[1]);
                            matchingData.put(split[0], split[1]);
                        }
                    }
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
                        return;
                    }
                    if (matchingData.get(selectedLeftValue).equals(rVal)) {
                        playCorrectSound();
                        v.setVisibility(View.INVISIBLE);
                        selectedLeftView.setVisibility(View.INVISIBLE);
                        selectedLeftView = null;
                        matchingCorrectCount++;
                        if (matchingCorrectCount == matchingData.size()) checkAnswer("correct_matching");
                    } else {
                        vibrateError();
                        selectedLeftView.setAlpha(1.0f);
                        selectedLeftView = null;
                        currentQuestionFirstTry = false;
                    }
                });
                layoutMatchingPairs.addView(view);
            }
        }
    }

    private void showComparisonLayout(List<String> options) {
        setAllLayoutsInvisible();
        if (comparisonContainer != null) {
            comparisonContainer.setVisibility(View.VISIBLE);
            if (txtDroppedCompare != null) txtDroppedCompare.setText("?");
            if (options != null && options.size() >= 2) {
                if (txtCompareLeft != null) txtCompareLeft.setText(options.get(0));
                if (txtCompareRight != null) txtCompareRight.setText(options.get(1));
            }
        }
        if (compareDragArea != null) {
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
    }

    private void setAllLayoutsInvisible() {
        findViewById(R.id.optionsGrid).setVisibility(View.GONE);
        if (dragContainer != null) dragContainer.setVisibility(View.GONE);
        if (findViewById(R.id.matchingContainer) != null) findViewById(R.id.matchingContainer).setVisibility(View.GONE);
        if (comparisonContainer != null) comparisonContainer.setVisibility(View.GONE);
        if (dropZone != null) dropZone.setVisibility(View.GONE);
    }

    private void checkAnswer(String selectedAnswer) {
        if (isAnswered) return;
        boolean isCorrect = "correct_matching".equals(selectedAnswer) || (selectedAnswer != null && selectedAnswer.equals(correctAnswer));
        if (isCorrect) {
            playCorrectSound();
            if (currentQuestionFirstTry) correctAnswersCount++;
            if (dropZone != null && dropZone.getVisibility() == View.VISIBLE && txtDroppedValue != null)
                txtDroppedValue.setText(selectedAnswer);
            if (comparisonContainer != null && comparisonContainer.getVisibility() == View.VISIBLE && txtDroppedCompare != null)
                txtDroppedCompare.setText(selectedAnswer);
            
            isAnswered = true;
            if (txtQuestionText != null) {
                txtQuestionText.postDelayed(() -> {
                    currentQuestionIndex++;
                    displayQuestion();
                }, 800);
            }
        } else {
            vibrateError();
            currentQuestionFirstTry = false;
        }
    }

    private void playCorrectSound() {
        try {
            if (correctPlayer != null) {
                correctPlayer.release();
            }
            // Bạn cần thêm file correct.mp3 vào thư mục res/raw
            // Nếu chưa có, đoạn code này sẽ không crash nhưng âm thanh sẽ không phát
            int resId = getResources().getIdentifier("correct", "raw", getPackageName());
            if (resId != 0) {
                correctPlayer = MediaPlayer.create(this, resId);
                correctPlayer.start();
                correctPlayer.setOnCompletionListener(MediaPlayer::release);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void vibrateError() {
        Vibrator v = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            v = vibratorManager.getDefaultVibrator();
        } else {
            v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }

        if (v != null && v.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(300);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (correctPlayer != null) {
            correctPlayer.release();
            correctPlayer = null;
        }
    }
}
