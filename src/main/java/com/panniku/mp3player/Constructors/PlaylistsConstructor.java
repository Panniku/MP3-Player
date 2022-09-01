package com.panniku.mp3player.Constructors;

import android.graphics.Bitmap;

import java.util.UUID;

public class PlaylistsConstructor {

    private Bitmap thumb;
    private String name;
    private String date;

    public PlaylistsConstructor(Bitmap mThumb, String mName, String mDate) {
        this.thumb = mThumb;
        this.name = mName;
        this.date = mDate;
    }

    public Bitmap getThumb() {
        return thumb;
    }

    public void setThumb(Bitmap thumb) {
        this.thumb = thumb;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}

