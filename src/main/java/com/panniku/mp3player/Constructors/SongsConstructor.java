package com.panniku.mp3player.Constructors;

import android.net.Uri;

public class SongsConstructor {
    private String path;
    private String title;
    private String artist;
    private String album;
    private String ogName;
    private int duration;
    private Uri art;

    public SongsConstructor(String path, String title, String artist, String album, String ogName, int duration, Uri art) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.ogName = ogName;
        this.duration = duration;
        this.art = art;
    }

    public SongsConstructor() {
        //
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Uri getArt() { return art; }

    public void setArt(Uri art) { this.art = art; }

    public String getOgName() { return ogName; }

    public void setOgName(String ogName) { this.ogName = ogName; }
}

