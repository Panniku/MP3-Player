package com.panniku.mp3player.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;

public class Utils {


    public static final int VIEWTYPE_GROUP = 0;

    public static Bitmap getAlbumArt(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        byte[] data = retriever.getEmbeddedPicture();
        Bitmap bitmap = null;
        if(data != null) {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Log.d("getAlbumArt()", "w, h :" + bitmap.getWidth() + " " + bitmap.getHeight());
        }
        retriever.release();
        return bitmap;
    }

    public static String getTimeFormatted(long milliSeconds) {
        String finalTimerString = "";
        String secondsString;

        int hours = (int) (milliSeconds / 3600000);
        int minutes = (int) (milliSeconds % 3600000) / 60000;
        int seconds = (int) ((milliSeconds % 3600000) % 60000 / 1000);

        if (hours > 0)
            finalTimerString = hours + ":";

        if (seconds < 10)
            secondsString = "0" + seconds;
        else
            secondsString = "" + seconds;

        finalTimerString = finalTimerString + minutes + ":" + secondsString;
        return finalTimerString;
    }

    public static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

}
