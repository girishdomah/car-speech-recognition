package com.example.carassistant;

public class Song {
    private String name;
    private String artist;
    private String imageUrl;
    private String fileUrl;

    public Song() {

    }

    public Song(String name, String artist, String imageUrl, String fileUrl) {
        this.name = name;
        this.artist = artist;
        this.imageUrl = imageUrl;
        this.fileUrl = fileUrl;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }
}
