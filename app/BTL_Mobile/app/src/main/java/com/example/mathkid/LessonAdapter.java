package com.example.mathkid;

import android.content.Context;
import android.graphics.Color;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mathkid.database.Lesson;

import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private List<Lesson> lessonList;
    private Context context;

    public LessonAdapter(List<Lesson> lessonList, Context context) {
        this.lessonList = lessonList;
        this.context = context;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessonList.get(position);
        boolean isEven = position % 2 == 0;

        if (isEven) {
            holder.layoutLeft.setVisibility(View.VISIBLE);
            holder.layoutRight.setVisibility(View.GONE);
            bindData(holder.cardLeft, holder.txtLevelLeft, holder.txtTitleLeft, holder.imgIconLeft,
                    holder.starsLeft, holder.imgStatusLeft, holder.nodeLeft, holder.txtNodeNumLeft,
                    holder.btnStartLeft, lesson, position + 1, true);
        } else {
            holder.layoutLeft.setVisibility(View.GONE);
            holder.layoutRight.setVisibility(View.VISIBLE);
            bindData(holder.cardRight, holder.txtLevelRight, holder.txtTitleRight, holder.imgIconRight,
                    holder.starsRight, holder.imgStatusRight, holder.nodeRight, holder.txtNodeNumRight,
                    holder.btnStartRight, lesson, position + 1, false);
        }
    }

    private void bindData(FrameLayout card, TextView txtLevel, TextView txtTitle, ImageView imgIcon,
                          LinearLayout starsLayout, ImageView imgStatus, FrameLayout node,
                          TextView txtNodeNum, View btnStart, Lesson lesson, int levelNum, boolean isLeft) {

        txtLevel.setText("LEVEL " + levelNum);
        txtTitle.setText(lesson.title);
        txtNodeNum.setText(String.valueOf(levelNum));

        // Set Icon
        if (lesson.icon != null && !lesson.icon.isEmpty()) {
            int iconRes = context.getResources().getIdentifier(lesson.icon, "drawable", context.getPackageName());
            if (iconRes != 0) imgIcon.setImageResource(iconRes);
        }

        // Handle Stars
        for (int i = 0; i < starsLayout.getChildCount(); i++) {
            ImageView star = (ImageView) starsLayout.getChildAt(i);
            if (i < lesson.starsEarned) {
                star.setColorFilter(Color.parseColor("#FFD600"));
                star.setAlpha(1.0f);
            } else {
                star.setColorFilter(Color.WHITE);
                star.setAlpha(0.3f);
            }
        }

        // Reset themes and logic
        int cardTheme, nodeTheme;
        if (lesson.isLocked) {
            cardTheme = R.style.GrayButtonTheme;
            nodeTheme = R.style.GrayButtonTheme;
            card.setAlpha(0.6f);
            imgStatus.setVisibility(View.VISIBLE);
            imgStatus.setImageResource(R.drawable.ic_lock);
            txtNodeNum.setVisibility(View.GONE);
            btnStart.setVisibility(View.GONE);
        } else if (lesson.isComplete) {
            cardTheme = isLeft ? R.style.GreenButtonTheme : R.style.BlueButtonTheme;
            nodeTheme = cardTheme;
            card.setAlpha(1.0f);
            imgStatus.setVisibility(View.VISIBLE);
            imgStatus.setImageResource(R.drawable.ic_check);
            txtNodeNum.setVisibility(View.GONE);
            btnStart.setVisibility(View.GONE);
        } else {
            cardTheme = R.style.OrangeButtonTheme;
            nodeTheme = R.style.OrangeButtonTheme;
            card.setAlpha(1.0f);
            imgStatus.setVisibility(View.GONE);
            txtNodeNum.setVisibility(View.VISIBLE);
            btnStart.setVisibility(View.VISIBLE);
        }

        // Apply theme dynamically
        updateViewTheme(card, cardTheme);
        updateViewTheme(node, nodeTheme);
    }

    private void updateViewTheme(FrameLayout view, int themeResId) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(context, themeResId);
        view.getContext().setTheme(themeResId);
        // Force redraw with new theme if necessary, though background should handle it via attributes
        view.invalidate();
    }

    @Override
    public int getItemCount() {
        return lessonList != null ? lessonList.size() : 0;
    }

    public static class LessonViewHolder extends RecyclerView.ViewHolder {
        View layoutLeft, layoutRight;
        FrameLayout cardLeft, nodeLeft, cardRight, nodeRight;
        TextView txtLevelLeft, txtTitleLeft, txtNodeNumLeft, txtLevelRight, txtTitleRight, txtNodeNumRight;
        ImageView imgIconLeft, imgStatusLeft, imgIconRight, imgStatusRight;
        LinearLayout starsLeft, starsRight;
        View btnStartLeft, btnStartRight;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutLeft = itemView.findViewById(R.id.layoutLeft);
            cardLeft = itemView.findViewById(R.id.cardLeft);
            nodeLeft = itemView.findViewById(R.id.nodeLeft);
            txtLevelLeft = itemView.findViewById(R.id.txtLevelLeft);
            txtTitleLeft = itemView.findViewById(R.id.txtTitleLeft);
            imgIconLeft = itemView.findViewById(R.id.imgIconLeft);
            imgStatusLeft = itemView.findViewById(R.id.imgStatusLeft);
            starsLeft = itemView.findViewById(R.id.starsLeft);
            txtNodeNumLeft = itemView.findViewById(R.id.txtNodeNumLeft);
            btnStartLeft = itemView.findViewById(R.id.btnStartLeft);

            layoutRight = itemView.findViewById(R.id.layoutRight);
            cardRight = itemView.findViewById(R.id.cardRight);
            nodeRight = itemView.findViewById(R.id.nodeRight);
            txtLevelRight = itemView.findViewById(R.id.txtLevelRight);
            txtTitleRight = itemView.findViewById(R.id.txtTitleRight);
            imgIconRight = itemView.findViewById(R.id.imgIconRight);
            imgStatusRight = itemView.findViewById(R.id.imgStatusRight);
            starsRight = itemView.findViewById(R.id.starsRight);
            txtNodeNumRight = itemView.findViewById(R.id.txtNodeNumRight);
            btnStartRight = itemView.findViewById(R.id.btnStartRight);
        }
    }
}
