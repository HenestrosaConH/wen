package com.example.videomeeting.models;

public class OnboardingItem {

    String title, description;
    int image;

    public OnboardingItem(String title, String description, int screenImg) {
        this.title = title;
        this.description = description;
        this.image = screenImg;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public int getImage() {
        return image;
    }
    public void setImage(int image) {
        this.image = image;
    }
}
