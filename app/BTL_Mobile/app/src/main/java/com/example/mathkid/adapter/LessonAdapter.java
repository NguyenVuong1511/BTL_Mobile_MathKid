package com.example.mathkid.adapter;

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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mathkid.R;
import com.example.mathkid.model.Lesson;

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

        // Xác định bài học "Hiện tại" (Bài đầu tiên chưa xong và không bị khóa)
        boolean isCurrent = false;
        for (int i = 0; i < lessonList.size(); i++) {
            if (!lessonList.get(i).isComplete && !lessonList.get(i).isLocked) {
                if (i == position) isCurrent = true;
                break;
            }
        }

        if (isEven) {
            holder.layoutLeft.setVisibility(View.VISIBLE);
            holder.layoutRight.setVisibility(View.GONE);
            bindData(holder.cardLeft, holder.innerCardLeft, holder.nodeLeft, holder.innerNodeLeft,
                    holder.txtLevelLeft, holder.txtTitleLeft, holder.imgIconLeft,
                    holder.starsLeft, holder.imgStatusLeft, holder.txtNodeNumLeft,
                    holder.btnStartLeft, lesson, position + 1, true, isCurrent);
        } else {
            holder.layoutLeft.setVisibility(View.GONE);
            holder.layoutRight.setVisibility(View.VISIBLE);
            bindData(holder.cardRight, holder.innerCardRight, holder.nodeRight, holder.innerNodeRight,
                    holder.txtLevelRight, holder.txtTitleRight, holder.imgIconRight,
                    holder.starsRight, holder.imgStatusRight, holder.txtNodeNumRight,
                    holder.btnStartRight, lesson, position + 1, false, isCurrent);
        }
    }

    private void bindData(View card, View innerCard, View node, View innerNode,
                          TextView txtLevel, TextView txtTitle, ImageView imgIcon,
                          LinearLayout starsLayout, ImageView imgStatus,
                          TextView txtNodeNum, View btnStart, Lesson lesson, int levelNum, 
                          boolean isLeft, boolean isCurrent) {

        txtLevel.setText("LEVEL " + levelNum);
        txtTitle.setText(lesson.title);
        txtNodeNum.setText(String.valueOf(levelNum));

        // Set Icon bài học
        if (lesson.icon != null && !lesson.icon.isEmpty()) {
            int iconRes = context.getResources().getIdentifier(lesson.icon, "drawable", context.getPackageName());
            if (iconRes != 0) imgIcon.setImageResource(iconRes);
        }

        // Xử lý hiển thị Sao
        if (lesson.isLocked) {
            starsLayout.setVisibility(View.INVISIBLE);
        } else {
            starsLayout.setVisibility(View.VISIBLE);
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
        }

        int themeResId;
        if (lesson.isLocked) {
            themeResId = R.style.GrayButtonTheme;
            card.setAlpha(0.6f);
            imgStatus.setVisibility(View.VISIBLE);
            imgStatus.setImageResource(R.drawable.ic_lock);
            txtNodeNum.setVisibility(View.GONE);
            btnStart.setVisibility(View.GONE);
        } else if (lesson.isComplete) {
            themeResId = isLeft ? R.style.GreenButtonTheme : R.style.BlueButtonTheme;
            card.setAlpha(1.0f);
            imgStatus.setVisibility(View.VISIBLE);
            imgStatus.setImageResource(R.drawable.ic_check);
            txtNodeNum.setVisibility(View.GONE);
            btnStart.setVisibility(View.GONE);
        } else if (isCurrent) {
            themeResId = R.style.OrangeButtonTheme;
            card.setAlpha(1.0f);
            imgStatus.setVisibility(View.GONE);
            txtNodeNum.setVisibility(View.VISIBLE);
            btnStart.setVisibility(View.VISIBLE);
        } else {
            themeResId = R.style.PurpleButtonTheme;
            card.setAlpha(1.0f);
            imgStatus.setVisibility(View.GONE);
            txtNodeNum.setVisibility(View.VISIBLE);
            btnStart.setVisibility(View.VISIBLE);
        }

        // Áp dụng theme động cho cả viền ngoài (card/node) và lõi trong (inner)
        applyThemeToView(card, themeResId, R.drawable.bg_soft_button);
        applyThemeToView(node, themeResId, R.drawable.bg_soft_button);
        applyThemeToView(innerCard, themeResId, R.drawable.bg_soft_inner);
        applyThemeToView(innerNode, themeResId, R.drawable.bg_soft_inner);
    }

    private void applyThemeToView(View view, int themeResId, int backgroundResId) {
        // Bọc context bằng theme mới để lấy được các attr tương ứng (?attr/gradientStart...)
        ContextThemeWrapper wrapper = new ContextThemeWrapper(context, themeResId);
        // Ép view nhận drawable đã được load qua context có theme
        view.setBackground(AppCompatResources.getDrawable(wrapper, backgroundResId));
    }

    @Override
    public int getItemCount() {
        return lessonList != null ? lessonList.size() : 0;
    }

    public static class LessonViewHolder extends RecyclerView.ViewHolder {
        View layoutLeft, layoutRight;
        FrameLayout cardLeft, nodeLeft, cardRight, nodeRight;
        View innerCardLeft, innerNodeLeft, innerCardRight, innerNodeRight;
        TextView txtLevelLeft, txtTitleLeft, txtNodeNumLeft, txtLevelRight, txtTitleRight, txtNodeNumRight;
        ImageView imgIconLeft, imgStatusLeft, imgIconRight, imgStatusRight;
        LinearLayout starsLeft, starsRight;
        View btnStartLeft, btnStartRight;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutLeft = itemView.findViewById(R.id.layoutLeft);
            cardLeft = itemView.findViewById(R.id.cardLeft);
            nodeLeft = itemView.findViewById(R.id.nodeLeft);
            // Lấy các View con (lớp nội dung bên trong) để đổi màu lõi
            innerCardLeft = cardLeft.getChildAt(0);
            innerNodeLeft = nodeLeft.getChildAt(0);

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
            innerCardRight = cardRight.getChildAt(0);
            innerNodeRight = nodeRight.getChildAt(0);

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
