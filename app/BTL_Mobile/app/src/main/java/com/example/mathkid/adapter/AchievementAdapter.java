package com.example.mathkid.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mathkid.R;
import com.example.mathkid.model.Achievement;
import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.ViewHolder> {

    private Context context;
    private List<Achievement> achievements;
    private OnAchievementListener listener;

    public interface OnAchievementListener {
        void onEdit(Achievement achievement);
        void onDelete(Achievement achievement);
    }

    public AchievementAdapter(Context context, List<Achievement> achievements, OnAchievementListener listener) {
        this.context = context;
        this.achievements = achievements;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_achievement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);
        holder.tvTitle.setText(achievement.title);
        holder.tvDescription.setText(achievement.description);
        holder.tvCondition.setText("Loại: " + achievement.type + " - GT: " + achievement.requiredValue);

        if (achievement.icon != null && !achievement.icon.isEmpty()) {
            if (achievement.icon.startsWith("ic_")) {
                int resId = context.getResources().getIdentifier(achievement.icon, "drawable", context.getPackageName());
                if (resId != 0) {
                    holder.ivIcon.setImageResource(resId);
                } else {
                    holder.ivIcon.setImageResource(R.drawable.ic_star);
                }
            } else {
                try {
                    byte[] decodedString = Base64.decode(achievement.icon, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    holder.ivIcon.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    holder.ivIcon.setImageResource(R.drawable.ic_star);
                }
            }
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_star);
        }

        holder.ivEdit.setOnClickListener(v -> listener.onEdit(achievement));
        holder.ivDelete.setOnClickListener(v -> listener.onDelete(achievement));
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    public void updateList(List<Achievement> newList) {
        this.achievements = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon, ivEdit, ivDelete;
        TextView tvTitle, tvDescription, tvCondition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCondition = itemView.findViewById(R.id.tvCondition);
        }
    }
}
