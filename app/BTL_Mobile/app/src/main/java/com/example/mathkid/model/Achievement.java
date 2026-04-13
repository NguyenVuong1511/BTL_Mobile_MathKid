package com.example.mathkid.model;

public class Achievement {
    public int id;
    public String title;
    public String description;
    public String icon;
    public String type;
    public int requiredValue;
    public boolean isUnlocked;
    public long earnedDate;

    public Achievement(int id, String title, String description, String icon, String type, int requiredValue, boolean isUnlocked, long earnedDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.type = type;
        this.requiredValue = requiredValue;
        this.isUnlocked = isUnlocked;
        this.earnedDate = earnedDate;
    }
}
