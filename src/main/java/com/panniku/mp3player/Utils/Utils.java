package com.panniku.mp3player.Utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.*;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.IOException;

import static androidx.core.content.ContentProviderCompat.requireContext;

public class Utils {


    public static final int VIEWTYPE_GROUP = 0;

//    public static Bitmap getAlbumArt(String path) {
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        retriever.setDataSource(path);
//        byte[] data = retriever.getEmbeddedPicture();
//        Bitmap bitmap = null;
//        if(data != null) {
//            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//            Log.d("getAlbumArt()", bitmap.getWidth() + ", " + bitmap.getHeight());
//        }
//        retriever.release();
//        return bitmap;
//    }

    public static Bitmap getAlbumArt(String path) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        byte[] data = mmr.getEmbeddedPicture();
        if (data != null) return BitmapFactory.decodeByteArray(data, 0, data.length);
        return null;
    }

    public static Uri getAlbumArt(int position) {
        Uri songCover = Uri.parse("content://media/external/audio/albumart");
        Uri uriSongCover = ContentUris.withAppendedId(songCover, position);
        Log.d("getAlbumArt: ", "" + uriSongCover);
        return uriSongCover;
    }
//    public static Bitmap scaleBitmapAndKeepRation(Bitmap targetBmp,int reqHeightInPixels,int reqWidthInPixels){
//        Matrix matrix = new Matrix();
//        matrix.setRectToRect(new RectF(0, 0, targetBmp.getWidth(), targetBmp.getHeight()), new RectF(0, 0, reqWidthInPixels, reqHeightInPixels), Matrix.ScaleToFit.CENTER);
//        Bitmap scaledBitmap = Bitmap.createBitmap(targetBmp, 0, 0, targetBmp.getWidth(), targetBmp.getHeight(), matrix, true);
//        Log.d("getAlbumArt()", scaledBitmap.getWidth() + ", " + scaledBitmap.getHeight());
//        return scaledBitmap;
//    }


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

    public static int getDominantColor(Uri uri, Context context) throws IOException {
        Bitmap bmp;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            bmp = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.getContentResolver(), uri));
        } else {
            bmp = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        }
        Bitmap newBitmap = Bitmap.createScaledBitmap(bmp, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

    public static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

}
