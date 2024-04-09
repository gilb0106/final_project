package com.example.final_project;

public class SavedImage {
    private String date;
    private String imageUrl;
    private String hdUrl;
    private String imageFilePath;

    public SavedImage(String date, String imageUrl, String hdUrl) {
        this.date = date;
        this.imageUrl = imageUrl;
        this.hdUrl = hdUrl;
    }

    public String getDate() {
        return date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getHdUrl() {
        return hdUrl;
    }

    public String getImageFilePath() {
        return imageFilePath;
    }
}
