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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getStarsEarned() {
        return starsEarned;
    }

    public void setStarsEarned(int starsEarned) {
        this.starsEarned = starsEarned;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
