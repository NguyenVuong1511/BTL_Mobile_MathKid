package com.example.mathkid.model;

public class Lesson {
    public int id;
    public String title;
    public String icon;
    public int starsEarned;
    public boolean isLocked;
    public boolean isComplete;
    public int orderIndex;

    public Lesson(int id, String title, String icon, int starsEarned, boolean isLocked, boolean isComplete, int orderIndex) {
        this.id = id;
        this.title = title;
        this.icon = icon;
        this.starsEarned = starsEarned;
        this.isLocked = isLocked;
        this.isComplete = isComplete;
        this.orderIndex = orderIndex;
    }
}
