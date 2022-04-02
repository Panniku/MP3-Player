package com.panniku.mp3player;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.panniku.mp3player.Actions.PlayActions;
import com.panniku.mp3player.Actions.ViewActions;
import com.panniku.mp3player.Services.NotificationPlayerBroadcast;
import com.panniku.mp3player.Services.NotificationViewBroadcast;
import com.panniku.mp3player.Services.OnClearFromService;

import java.util.ArrayList;

public class NotificationHandler extends Application {

    public static final String CHANNEL_ID = "SongNotification";
    public static final String ACTION_PREV = "PREVIOUS";
    public static final String ACTION_PLAY = "PLAY";
    public static final String ACTION_NEXT = "NEXT";
    public static final String ACTION_CLOSE = "CLOSE";

    public static final String ACTION_SONGS = "ACTION_SONGS";
    public static final String ACTION_PLAYLISTS = "ACTION_PLAYLISTS";
    public static final String ACTION_ALBUMS = "ACTION_ALBUMS";
    public static final String ACTION_FOLDERS = "ACTION_FOLDERS";

    static RemoteViews collapsedView;
    static RemoteViews expandedView;

    static MediaPlayer mediaPlayer;
    static String curView;

    static NotificationManagerCompat notificationManagerCompat;
    static NotificationCompat.Builder notification;
    static Notification mNotification;

    static ArrayList<MusicAdapter> songUriArrayList;
    static int thisPos;
    static boolean isPaused;
    static int length;
    static Context thisContext;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void createNotification(
            Context context,
            ArrayList<MusicAdapter> musicAdapters,
            int pos
    ) {
        thisContext = context;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationManagerCompat = NotificationManagerCompat.from(context);
            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "MainSession");
            
            /*
            * PREVIOUS
            * */
            PendingIntent pendPreviousIntent;
            int prevIcon;
            Intent intentPrev = new Intent(context, NotificationPlayerBroadcast.class).setAction(ACTION_PREV);
            pendPreviousIntent = getCode(context, intentPrev);
            prevIcon = R.drawable.skip_previous;


            /*
             * PLAY
             * */
            Intent intentPlay = new Intent(context, NotificationPlayerBroadcast.class).setAction(ACTION_PLAY);
            PendingIntent pendPlayIntent;
            pendPlayIntent = getCode(context, intentPlay);


            /*
             * NEXT
             * */
            PendingIntent pendNextIntent;
            int nextIcon;
            Intent intentNext = new Intent(context, NotificationPlayerBroadcast.class).setAction(ACTION_NEXT);
            pendNextIntent = getCode(context, intentNext);
            nextIcon = R.drawable.skip_next;


            /*
             * CLOSE
             * */
            Intent intentClose = new Intent(context, NotificationPlayerBroadcast.class).setAction(ACTION_CLOSE);
            PendingIntent pendCloseIntent;
            pendCloseIntent = getCode(context, intentClose);

            //
            //
            
            /*
             * SONGS
             * */
            Intent intentSongs = new Intent(context, NotificationPlayerBroadcast.class).setAction(ACTION_SONGS);
            PendingIntent pendSongsIntent;
            pendSongsIntent = getCode(context, intentSongs);

            /*
             * PLAYLISTS
             * */
            Intent intentPlaylists = new Intent(context, NotificationPlayerBroadcast.class).setAction(ACTION_PLAYLISTS);
            PendingIntent pendPlaylistsIntent;
            pendPlaylistsIntent = getCode(context, intentPlaylists);

            /*
             * ALBUMS
             * */
            Intent intentAlbums = new Intent(context, NotificationPlayerBroadcast.class).setAction(ACTION_ALBUMS);
            PendingIntent pendAlbumsIntent;
            pendAlbumsIntent = getCode(context, intentAlbums);
            
            
            /*
             * FOLDERS
             * */
            Intent intentFolders = new Intent(context, NotificationPlayerBroadcast.class).setAction(ACTION_FOLDERS);
            PendingIntent pendFoldersIntent;
            pendFoldersIntent = getCode(context, intentFolders);


            /*
            * PLAYER
            * */
            thisPos = pos;
            songUriArrayList = musicAdapters;

            String songTitle = songUriArrayList.get(pos).getTitle();
            String songArtist = songUriArrayList.get(pos).getArtist();
            String songDuration = getTimeFormatted(songUriArrayList.get(pos).getDuration());
            String songPath = songUriArrayList.get(pos).getPath();

            play(context, Uri.parse(musicAdapters.get(pos).getPath()));

            collapsedView = new RemoteViews(context.getPackageName(), R.layout.base_notif_collapsed);
            expandedView = new RemoteViews(context.getPackageName(), R.layout.base_notif_expanded);

            notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.song_icon)
                    .setContentTitle(songTitle)
                    .setContentText(songArtist)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(false)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setCustomContentView(collapsedView)
                    .setCustomBigContentView(expandedView)
                    .setColorized(true)
                    .setChannelId(CHANNEL_ID)
//                    .addAction(R.drawable.skip_previous, "Skip Previous", pendPreviousIntent)
//                    .addAction(R.drawable.play, "Play", pendPlayIntent)
//                    .addAction(R.drawable.skip_next, "Skip next", pendNextIntent)
//                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
//                            .setMediaSession(mediaSessionCompat.getSessionToken())
//                            .setShowActionsInCompactView(0, 1, 2))
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            collapsedView.setTextViewText(R.id.notifSongName, songTitle);
            collapsedView.setTextViewText(R.id.notifSongDuration, songDuration);

            collapsedView.setOnClickPendingIntent(R.id.notifCollapsePrev, pendPreviousIntent);
            collapsedView.setOnClickPendingIntent(R.id.notifCollapsePlayPause, pendPlayIntent);
            collapsedView.setOnClickPendingIntent(R.id.notifCollapseNext, pendNextIntent);

            expandedView.setOnClickPendingIntent(R.id.notifCollapseSongsView, pendSongsIntent);
            expandedView.setOnClickPendingIntent(R.id.notifCollapsePlaylistsView, pendPlaylistsIntent);
            expandedView.setOnClickPendingIntent(R.id.notifCollapseAlbumsView, pendAlbumsIntent);
            expandedView.setOnClickPendingIntent(R.id.notifCollapseFoldersView, pendFoldersIntent);

            expandedView.setViewVisibility(R.id.notifExpandedSongsViewHolder, View.VISIBLE);
            curView = "songs";

            //expandedView.setRemoteAdapter(R.id.notifExpandedListView, new Intent(context, NotificationListService.class)); // TODO

            collapsedView.setImageViewResource(R.id.notifCollapsePlayPause, R.drawable.pause);

            collapsedView.setProgressBar(R.id.notifCollapsedProgressBar, mediaPlayer.getDuration(), 0, false);

            collapsedView.setProgressBar(R.id.notifCollapsedProgressBar, mediaPlayer.getDuration(), 0, false);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer != null){
                        int totalDur = mediaPlayer.getDuration();
                        int currentPos = mediaPlayer.getCurrentPosition();
                        Log.d("run() thread", "totalDur: " + totalDur);
                        Log.d("run() thread", "currentPos: " + currentPos);
                        while(currentPos < totalDur){
                            try {
                                if(!isPaused){
                                    currentPos = mediaPlayer.getCurrentPosition();
                                    collapsedView.setProgressBar(R.id.notifCollapsedProgressBar, totalDur, currentPos, false);
                                    notificationManagerCompat.notify(1, notification.build());

                                    Log.d("progress bar", currentPos + " | " + totalDur);
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                    } else {
                        Log.d("mediaPlayer", "MEDIAPLAYER IS NULL OR IS NOT PLAYING");
                    }
                }
            }, 500);

            Bitmap icon;
            byte[] imageBytes = getAlbumArt(songPath);
            if(imageBytes != null){
                icon = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                int toSetColor = getDominantColor(icon);
                collapsedView.setImageViewBitmap(R.id.notifSongArt, icon);
                //collapsedView.setInt(R.id.notifCollapseBase, "background", toSetColor);
                Log.d("toSetcolr", "col: "+ toSetColor);
            } else {
                collapsedView.setImageViewResource(R.id.notifSongArt, R.drawable.song_image);
                //collapsedView.setInt(R.id.notifCollapseBase, "background", Color.parseColor("#000000"));
                int placeholderColor = Color.argb(0, 20, 20, 20);
                //notification.setLargeIcon(placeholder);
                //notification.setColor(placeholderColor);

            }

            mNotification = notification.build();
            mNotification.flags = Notification.FLAG_NO_CLEAR;
            notificationManagerCompat.notify(1, notification.build());
            Log.d("manager", "createNotification: Created.");

            context.registerReceiver(broadcastReceiver, new IntentFilter("SONGS"));
            context.startService(new Intent(context, OnClearFromService.class));
            Log.d("service", "Registered.");
        } else {
            Toast.makeText(context, "Notification has errored!", Toast.LENGTH_SHORT).show();
        }

    }

    static BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String playerAction = intent.getExtras().getString("actionName");
            Log.d("broadcast", "action: " + playerAction);
            switch (playerAction){
                case NotificationHandler.ACTION_PREV:
                    seekPrevious(context, thisPos);
                    onSongPrevious(); // TODO
                    break;

                case NotificationHandler.ACTION_PLAY:
                    Log.d("ACTION_PLAY VALUE", "isPaused: " + isPaused);
                    if(!isPaused){ //   if TRUE
                        pause();
                        onSongPause(); // TODO
                        collapsedView.setImageViewResource(R.id.notifCollapsePlayPause, R.drawable.play);
                        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
                        notification.setOngoing(false);
                        notificationManagerCompat.notify(1, notification.build());
                        Log.d("ACTION_PLAY", "isPaused: NOW FALSE");
                    } else { // else if FALSE
                        play(context, Uri.parse(songUriArrayList.get(thisPos).getPath())); // plays first
                        resume(); // seeks to previous/left off pos
                        onSongPlay(); // TODO
                        collapsedView.setImageViewResource(R.id.notifCollapsePlayPause, R.drawable.pause);
                        mNotification.flags = Notification.FLAG_NO_CLEAR;
                        notification.setOngoing(true);
                        notificationManagerCompat.notify(1, notification.build());
                        Log.d("ACTION_PLAY", "isPaused: NOW TRUE");
                    }
                    break;

                case NotificationHandler.ACTION_NEXT:
                    seekNext(context, thisPos);
                    onSongNext(); //TODO
                    break;

                case NotificationHandler.ACTION_SONGS:
                    onSongView();
                    Log.d("NotifHandler", "Received SONGS.");
                    break;

                case NotificationHandler.ACTION_PLAYLISTS:
                    onPlaylistsView();
                    Log.d("NotifHandler", "Received PLAYLISTS.");
                    break;

                case NotificationHandler.ACTION_ALBUMS:
                    onAlbumsView();
                    Log.d("NotifHandler", "Received ALBUMS.");
                    break;

                case NotificationHandler.ACTION_FOLDERS:
                    onFoldersView();
                    Log.d("NotifHandler", "Received FOLDERS.");
                    break;

                case NotificationHandler.ACTION_CLOSE:
                    context.unregisterReceiver(broadcastReceiver);
                    mediaPlayer.stop();
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        notificationManagerCompat.cancelAll();
                    }
                    break;
            }
        }
    };

    @Override
    public void onTerminate() {
        super.onTerminate();
        stop();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManagerCompat.cancelAll();
        }
        getApplicationContext().unregisterReceiver(broadcastReceiver);
    }

    public static void stop(){
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d("mediaPlayer", "Stopped.");
        }
    }

    public static void play(Context context, Uri song){
        stop();
        mediaPlayer = MediaPlayer.create(context, song);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                onSongPlay();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                seekNext(context, thisPos);
                onSongNext();
            }
        });
    }

    public static void pause(){
        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            length = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
        } else {
            Log.d("mediaPlayer", "mediaPlayer is null or mediaPlayer is not playing.");
        }

    }

    public static void resume() {
        mediaPlayer.seekTo(length);
        mediaPlayer.start();
        Log.d("mediaPlayer", "Length: " + length);
    }

    public static void seekPrevious(Context context, int pos) {
        stop();
        thisPos--;
        Log.d("seekprev", "thisPos: " + thisPos);
        if(thisPos < 0){ // -1 < 0
            thisPos = songUriArrayList.size() - 1;
            seek(context, thisPos);
            Log.d("seekPrevious()", "Seeked to last song.");
        } else {
            seek(context, thisPos);
            Log.d("seekPrevious()", "Seeked to pos: " + thisPos);
        }

    }

    public static void seekNext(Context context, int pos) {
        stop();
        thisPos++;
        if(thisPos > songUriArrayList.size() - 1){
            thisPos = 0;
            seek(context, thisPos);
            Log.d("seekNext()", "Seeked to first song.");
        } else {
            seek(context, thisPos);
            Log.d("seekNext()", "Seeked to pos: " + thisPos);
        }
    }

    public static void onSongPrevious() {
        Log.d("Notif", "onSongPrevious");
    }

    
    public static void onSongPlay() {
        isPaused = false;
        Log.d("Notif", "onSongPlay");
    }

    
    public static void onSongPause() {
        isPaused = true;
        Log.d("Notif", "onSongPause");
    }

    
    public static void onSongNext() {
        Log.d("Notif", "onSongNext");
    }

    
    public static void onSongDestroy() {
        // TODO 
    }

    
    public static void onSongView() {
        NotificationHandler.toChangeToLayout("songs");
        NotificationHandler.songsView();
    }

    
    public static void onPlaylistsView() {
        NotificationHandler.toChangeToLayout("playlists");
        NotificationHandler.playlistsView();
    }

    
    public static void onAlbumsView() {
        NotificationHandler.toChangeToLayout("albums");
        NotificationHandler.albumsView();
    }

    
    public static void onFoldersView() {
        NotificationHandler.toChangeToLayout("folders");
        NotificationHandler.foldersView();
    }

    private static void seek(Context context, int pos){
        Log.d("seek", "pos: " + pos);
        length = 0;
        Uri uri = Uri.parse(songUriArrayList.get(pos).getPath());
        
        if(isPaused){
            play(context, uri);
            mediaPlayer.stop();
            createNotification(thisContext, songUriArrayList, thisPos);
        } else {
            play(context, uri);
            createNotification(thisContext, songUriArrayList, thisPos);
        }
    }

    private static String getTimeFormatted(int millis) {
        String finalTimerString = "";
        String secondsString;

        //Converting total duration into time
        int hours = (int) (millis / 3600000);
        int minutes = (int) (millis % 3600000) / 60000;
        int seconds = (int) ((millis % 3600000) % 60000 / 1000);

        // Adding hours if any
        if (hours > 0)
            finalTimerString = hours + ":";

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10)
            secondsString = "0" + seconds;
        else
            secondsString = "" + seconds;

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // Return timer String;
        return finalTimerString;
    }

    public static void songsView(){
        if(curView.equals("songs")){
            expandedView.setViewVisibility(R.id.notifExpandedSongsViewHolder, View.VISIBLE);
            expandedView.setViewVisibility(R.id.notifExpandedPlaylistsViewHolder, View.GONE);
            expandedView.setViewVisibility(R.id.notifExpandedAlbumsViewHolder, View.GONE);
            expandedView.setViewVisibility(R.id.notifExpandedFoldersViewHolder, View.GONE);
        }
        notificationManagerCompat.notify(1, notification.build());
    }

    public static void playlistsView(){
        Log.d("playlistsView", "curView: " + curView);
        if(curView.equals("playlists")){
            expandedView.setViewVisibility(R.id.notifExpandedSongsViewHolder, View.GONE);
            expandedView.setViewVisibility(R.id.notifExpandedPlaylistsViewHolder, View.VISIBLE);
            expandedView.setViewVisibility(R.id.notifExpandedAlbumsViewHolder, View.GONE);
            expandedView.setViewVisibility(R.id.notifExpandedFoldersViewHolder, View.GONE);
        }
        notificationManagerCompat.notify(1, notification.build());
    }

    public static void albumsView(){
        if(curView.equals("albums")){
            expandedView.setViewVisibility(R.id.notifExpandedSongsViewHolder, View.GONE);
            expandedView.setViewVisibility(R.id.notifExpandedPlaylistsViewHolder, View.GONE);
            expandedView.setViewVisibility(R.id.notifExpandedAlbumsViewHolder, View.VISIBLE);
            expandedView.setViewVisibility(R.id.notifExpandedFoldersViewHolder, View.GONE);
        }
        notificationManagerCompat.notify(1, notification.build());
    }

    public static void foldersView(){
        if(curView.equals("folders")){
            expandedView.setViewVisibility(R.id.notifExpandedSongsViewHolder, View.GONE);
            expandedView.setViewVisibility(R.id.notifExpandedPlaylistsViewHolder, View.GONE);
            expandedView.setViewVisibility(R.id.notifExpandedAlbumsViewHolder, View.GONE);
            expandedView.setViewVisibility(R.id.notifExpandedFoldersViewHolder, View.VISIBLE);
        }
        notificationManagerCompat.notify(1, notification.build());
    }

    public static void toChangeToLayout(String layout) {
        curView = layout;
    }

    public static byte[] getAlbumArt(String uriPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uriPath.toString());
        byte[] art = null;
        if(retriever.getEmbeddedPicture() != null) {
            art = retriever.getEmbeddedPicture();
        }
        retriever.release();
        return art;
    }

    private static PendingIntent getCode(Context context, Intent intent){
        PendingIntent pendingIntent;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return pendingIntent;
    }

    public static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

}