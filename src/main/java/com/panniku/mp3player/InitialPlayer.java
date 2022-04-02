package com.panniku.mp3player;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.panniku.mp3player.Overlay.OverlayPlayer;

import java.util.ArrayList;
import java.util.Arrays;

public class InitialPlayer {

    private String songTitle, songArtist, songDuration;
    private int songArt;
    public static MediaPlayer mediaPlayer;
    //
    private static ArrayList<MusicAdapter> songUriArrayList;
    private static int thisPos, length;
    private static boolean isPlaying, isPaused;

    public void init(ArrayList<MusicAdapter> musicAdapters){
        songUriArrayList = musicAdapters;
    }

    public static void stop(){
        if(mediaPlayer != null){
            if(OverlayPlayer.getVisualizer() != null){
                OverlayPlayer.getVisualizer().release(); // == null
                Log.d("mediaPlayer", "Released Visualizer.");
            }
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            //OverlayPlayer.showToast(thisToastView, thisToastText, "Stopped.");
            Log.d("mediaPlayer", "Stopped.");
        }
    }

    public static void play(Context context, int pos){
        stop();
        isPlaying = true;
        Uri uri = Uri.parse(songUriArrayList.get(pos).getPath());
        thisPos = pos;
        mediaPlayer = MediaPlayer.create(context, uri);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                //OverlayPlayer.showToast(context,  "Playing " + songUriArrayList.get(pos).getTitle());
                OverlayPlayer.updateMiniPlayer();
                Log.d("InitialPlayer", "Playing " + songUriArrayList.get(pos).getTitle());
                Log.d("InitialPlayer", "Checking ID: " + mediaPlayer.getAudioSessionId());
                Log.d("InitialPlayer", "Tracking Bytes: " + Arrays.toString(OverlayPlayer.getVisualizer().getBytes()));
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                seekNext(context);
                OverlayPlayer.updateMiniPlayer();
            }
        });
    }

    public static void pause(){
        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            if(OverlayPlayer.getVisualizer() != null){
                OverlayPlayer.getVisualizer().release();
            }
            length = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
        } else {
            Log.d("mediaPlayer", "mediaPlayer is null or mediaPlayer is not playing.");
        }

    }

    public static void resume() {
        mediaPlayer.seekTo(length);
        mediaPlayer.start();
        OverlayPlayer.getVisualizer().setPlayer(mediaPlayer.getAudioSessionId());
        Log.d("mediaPlayer", "Length: " + length);
    }

    public static void seekPrevious(Context context) {
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
        //OverlayPlayer.showToast(context, thisToastLayout, thisToastText, "Seeking previous!");
    }

    public static void seekNext(Context context) {
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
        //OverlayPlayer.showToast(context, thisToastLayout, thisToastText, "Seeking next!");
    }

    private static void seek(Context context, int pos){
        Log.d("seek", "pos: " + pos);
        length = 0;
        //Uri uri = Uri.parse(songUriArrayList.get(pos).getPath());

        if(isPaused){
            play(context, pos);
            mediaPlayer.stop();
            OverlayPlayer.updateMiniPlayer();
        } else {
            play(context, pos);
            OverlayPlayer.updateMiniPlayer();
        }
    }

    public static MediaPlayer getPlayer(){
        return mediaPlayer;
    }

    public static int getPos(){
        return thisPos;
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

}
