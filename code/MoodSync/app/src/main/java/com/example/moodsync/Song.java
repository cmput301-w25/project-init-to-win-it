package com.example.moodsync;

public class Song {
    private Long id;
    private String singer;
    private String title;
    private String url;

    // Empty constructor required for Firestore
    public Song() {
    }

    public Song(Long id, String singer, String title, String url) {
        this.id = id;
        this.singer = singer;
        this.title = title;
        this.url = url;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getSinger() {
        return singer;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}