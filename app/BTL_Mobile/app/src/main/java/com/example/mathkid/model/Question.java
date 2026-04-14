package com.example.mathkid.model;

import java.util.List;

public class Question {
    private int id;
    private int activityId;
    private String type;
    private String text;
    private String audio;
    private String image;
    private String answer;
    private List<String> options;

    public Question(int id, int activityId, String type, String text, String audio, String image, String answer, List<String> options) {
        this.id = id;
        this.activityId = activityId;
        this.type = type;
        this.text = text;
        this.audio = audio;
        this.image = image;
        this.answer = answer;
        this.options = options;
    }

    public int getId() { return id; }
    public int getActivityId() { return activityId; }
    public String getType() { return type; }
    public String getText() { return text; }
    public String getAudio() { return audio; }
    public String getImage() { return image; }
    public String getAnswer() { return answer; }
    public List<String> getOptions() { return options; }
}
