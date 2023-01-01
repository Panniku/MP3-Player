package com.panniku.mp3player;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import com.panniku.mp3player.Constructors.SongsConstructor;
import com.panniku.mp3player.Overlay.OverlayPlayer;

import java.io.IOException;
import java.util.ArrayList;

public class InitialPlayer {

    public static MediaPlayer mediaPlayer;
    //
    private static ArrayList<SongsConstructor> songsArrayList;
    private static int thisPos, length;
    private static boolean isPlaying, isPaused;

    public void init(ArrayList<SongsConstructor> songsConstructors){
        songsArrayList = songsConstructors;
    }

    public static void stop(){
        if(mediaPlayer != null){
            if(OverlayPlayer.getVisualizer() != null){
                OverlayPlayer.getVisualizer().release(); // == now null
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
        isPaused = false;
        if(!OverlayPlayer.getIsPlaying()){
            OverlayPlayer.setIsPlaying(true);
        }
        Uri uri = Uri.parse(songsArrayList.get(pos).getPath());
        thisPos = pos;
        mediaPlayer = MediaPlayer.create(context, uri);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                OverlayPlayer.getStatusbarText().setText("Playing \"" + songsArrayList.get(InitialPlayer.getPos()).getTitle() + "\"");
                try {
                    OverlayPlayer.updateMiniPlayer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("InitialPlayer", "Playing " + songsArrayList.get(pos).getTitle());
                Log.d("InitialPlayer", "Checking ID: " + mediaPlayer.getAudioSessionId());
                //Log.d("InitialPlayer", "Tracking Bytes: " + Arrays.toString(OverlayPlayer.getVisualizer().getBytes()));
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                String curLoop = OverlayPlayer.getCurrentLoop();
                switch(curLoop){
                    case "All":
                        try {
                            seekNext(context);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d("InitialPlayer", "Case ALL: Seeking Next.");
                        break;
                    case "This":
                        play(context, pos);
                        Log.d("InitialPlayer", "Case THIS: Repeating.");
                        break;
                }
            }
        });
    }

    public static void pause(){
        if(mediaPlayer != null && isPlaying){
            isPaused = true;
            if(OverlayPlayer.getVisualizer() != null){
                OverlayPlayer.getVisualizer().release();
            }
            length = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
        } else {
            Log.d("mediaPlayer", "mediaPlayer is null or mediaPlayer is not playing.");
        }

    }

    public static void resume(Context context) throws IOException {
        isPaused = false;
        mediaPlayer.seekTo(length);
        mediaPlayer.start();
        OverlayPlayer.updateMiniPlayer();
        Log.d("mediaPlayer", "Length: " + length);
    }

    public static void seekPrevious(Context context) throws IOException {
        stop();
        thisPos--;
        Log.d("seekprev", "thisPos: " + thisPos);
        if(thisPos < 0){ // -1 < 0
            thisPos = songsArrayList.size() - 1;
            seek(context, thisPos);
            Log.d("seekPrevious()", "Seeked to last song.");
        } else {
            seek(context, thisPos);
            Log.d("seekPrevious()", "Seeked to pos: " + thisPos);
        }
        OverlayPlayer.getStatusbarText().setText("Seeking previous.");
    }

    public static void seekNext(Context context) throws IOException {
        stop();
        thisPos++;
        if(thisPos > songsArrayList.size() - 1){
            thisPos = 0;
            seek(context, thisPos);
            Log.d("seekNext()", "Seeked to first song.");
        } else {
            seek(context, thisPos);
            Log.d("seekNext()", "Seeked to pos: " + thisPos);
        }
        OverlayPlayer.getStatusbarText().setText("Seeking next.");
    }

    private static void seek(Context context, int pos) throws IOException {
        Log.d("seek", "pos: " + pos);
        length = 0;
        //Uri uri = Uri.parse(songUriArrayList.get(pos).getPath());
        if(isPlaying){
            stop();
        }
        play(context, pos);
        OverlayPlayer.updateMiniPlayer();
    }

    public static MediaPlayer getPlayer(){
        return mediaPlayer;
    }
    public static int getPos(){
        return thisPos;
    }
    public static int getLength(){
        return length;
    }
    public static void setLength(int i){
        length = i;
    }
    public static ArrayList<SongsConstructor> getArrayList(){
        return songsArrayList;
    }

}
