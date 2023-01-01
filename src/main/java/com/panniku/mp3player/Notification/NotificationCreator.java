package com.panniku.mp3player.Notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.panniku.mp3player.Adapters.SongRecyclerViewAdapter;
import com.panniku.mp3player.Constructors.SongsConstructor;
import com.panniku.mp3player.R;
import com.panniku.mp3player.Utils.Utils;

import java.util.ArrayList;

public class NotificationCreator {

    public static final String CHANNEL_ID = "notifChannel";

//    public static final String SEEK_PREV = "seekPrev";
//    public static final String PLAY = "play";
//    public static final String SEEK_NEXT = "seekNext";
//    public static final String CANCEL = "cancel";

    public static final String TOGGLE = "toggle";

    public static Notification notification;

    public static void init(Context context){
        Log.d("NOTIFICATION CREATOR", "INITIATING");
        NotificationManagerCompat notifCompat = NotificationManagerCompat.from(context);
        MediaSessionCompat mediaCompat = new MediaSessionCompat(context,  "mp3player");

//        PendingIntent prevInt;
//        Intent intentPrev = new Intent(context, NotificationActions.class).setAction(SEEK_PREV);
//        prevInt = PendingIntent.getBroadcast(context, 0, intentPrev, getFlags());
//
//        PendingIntent playInt;
//        Intent play = new Intent(context, NotificationActions.class).setAction(PLAY);
//        playInt = PendingIntent.getBroadcast(context, 0, play, getFlags());
//
//        PendingIntent nextInt;
//        Intent intentNext = new Intent(context, NotificationActions.class).setAction(SEEK_NEXT);
//        nextInt = PendingIntent.getBroadcast(context, 0, intentNext, getFlags());
//
//        PendingIntent cancelInt;
//        Intent intentCancel = new Intent(context, NotificationActions.class).setAction(CANCEL);
//        cancelInt = PendingIntent.getBroadcast(context, 0, intentCancel, getFlags());


        PendingIntent toggleOverlay;
        Intent intent = new Intent(context, NotificationActions.class).setAction(TOGGLE);
        toggleOverlay = PendingIntent.getBroadcast(context, 0, intent, getFlags());

        // Create notification
        notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.song_icon)
                //.setLargeIcon(Utils.getAlbumArt(songsConstructors.get(pos).getPath()))
                .setContentTitle("Overlay is hidden.")
                .setContentText("Tap this to reveal the overlay!")
                //.setSubText(String.valueOf(songsConstructors.get(pos).getDuration()))
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(toggleOverlay)
//                .addAction(R.drawable.skip_previous, "Seek Prev", prevInt)
//                .addAction(playPause, "Play", playInt)
//                .addAction(R.drawable.skip_next, "Seek Next", nextInt)
//                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
//                        .setShowActionsInCompactView(0, 1, 2)
//                        .setShowCancelButton(true)
//                        .setCancelButtonIntent(cancelInt)
////                        .setMediaSession(mediaCompat.getSessionToken()))
//                )
                .setAutoCancel(true)
                .setOngoing(true)
                .build();

        notifCompat.notify(1, notification);

    }

    private static int getFlags(){
        int pendIntent;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            pendIntent = PendingIntent.FLAG_IMMUTABLE;
        } else {
            pendIntent = PendingIntent.FLAG_UPDATE_CURRENT;
        }
        return pendIntent;
    }
}
