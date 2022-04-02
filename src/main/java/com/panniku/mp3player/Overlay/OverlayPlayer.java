package com.panniku.mp3player.Overlay;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.panniku.mp3player.*;
import com.panniku.mp3player.Visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class OverlayPlayer extends Service {

    /*
    * This class will house all the main components of this window:
    * 1. Main overlay window
    * 2. Optional? To-Do? - Settings (Maybe....who knows)
    * 3. Container of all 4 layouts with additional (Playlist, folders.........)
    * */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private RelativeLayout mainRoot;

    private static RelativeLayout tinyRoot;
    private static ImageView tinyArtwork;
    private static TextView tinySongTitle;
    private static ImageView tinyPrev, tinyPlayPause, tinyNext;

    // MAIN WINDOW
    private RelativeLayout windowLayout, mainLayout;
    private View windowView;
    private WindowManager window;

    // CONTROLS
    public static BarVisualizer barVisualizer;
    private ImageView toMiniPlayer, settingsImage, minimizeImage, closeImage;

    // MINI PLAYER
    private static RelativeLayout miniPlayer;
    // CONTROLS
    private static ImageView miniImage;
    private static TextView miniSongTitle;
    private static TextView miniSongArtist;
    private static TextView miniSongDuration;
    private static ImageView miniSeekPrev, miniPlayPause, miniSeekNext;

    // SETTINGS
    private RelativeLayout settingsLayout;

    private TextView opacityText;
    private SeekBar opacitySeekbar;

    private RelativeLayout songsView, playlistsView, albumsView, foldersView;
    private RelativeLayout songsRoot, playlistsRoot, albumsRoot, foldersRoot;

    // SONGS
    private ImageView shuffleImage, reloadImage;

    private static ArrayList<MusicAdapter> songUriArrayList, songFilePathArrayList, songFinalArrayList;
    private SongRecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView recyclerView;


    // TODO sections

    // STATUS BAR
    private TextView songSizeText;

    // DATA - SAVING AND LOADING
    //TEMP
    // SAVE
    private static final String prefString = "overlayWindowPref";
    private static final String toSaveOpacityString = "toSaveOpacity";
    private static final String toSaveSeekbarPosString = "toSaveSeekbarPosString";

    private static float toSaveOpacity;
    private static int toSaveSeekbarPos;

    // LOAD
    private static float toLoadOpacity;
    private static int toLoadSeekbarPos;

    // MISC
    private RelativeLayout no_songs;
    private boolean isMainView = true, isSettingsView = false;
    private static boolean isPlaying;
    int animTime;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();

        int LAYOUT_FLAG_TYPE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG_TYPE = WindowManager.LayoutParams.TYPE_PHONE;
        }

        animTime = this.getResources().getInteger(android.R.integer.config_shortAnimTime);
        loadData();

        windowView = LayoutInflater.from(this).inflate(R.layout.overlay_player, null);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        window = (WindowManager) getSystemService(WINDOW_SERVICE);
        params.x = 0;
        params.y = 100;
        params.alpha = toLoadOpacity;
        toSaveOpacity = params.alpha;
        window.addView(windowView, params);

        mainRoot = windowView.findViewById(R.id.overlayMainRoot);

        tinyRoot = windowView.findViewById(R.id.overlayTinyRoot);
        tinyArtwork = windowView.findViewById(R.id.overlayTinyArtwork);
        tinySongTitle = windowView.findViewById(R.id.overlayTinySongTitle);
        tinyPrev = windowView.findViewById(R.id.overlayTinySeekPrev);
        tinyPlayPause = windowView.findViewById(R.id.overlayTinyPlayPause);
        tinyNext = windowView.findViewById(R.id.overlayTinySeekNext);

        // MAIN WINDOW AND ROOT LAYOUTS
        windowLayout = windowView.findViewById(R.id.overlayExpandedHolderRoot);
        mainLayout = windowView.findViewById(R.id.overlayExpandedHolderBase);
        miniPlayer = windowView.findViewById(R.id.overlayMiniRoot);
        settingsLayout = windowView.findViewById(R.id.overlaySettingsRoot);

        // CONTROLS
        barVisualizer = windowView.findViewById(R.id.overlayBarVisualizer);
        toMiniPlayer = windowView.findViewById(R.id.overlayToMiniplayer);
        settingsImage = windowView.findViewById(R.id.overlaySettings);
        minimizeImage = windowView.findViewById(R.id.overlayMinimize);
        closeImage = windowView.findViewById(R.id.overlayClose);

        // MINI PLAYER
        miniImage = windowView.findViewById(R.id.overlayMiniSongArtwork);
        miniSongTitle = windowView.findViewById(R.id.overlayMiniSongTitle);
        miniSongArtist = windowView.findViewById(R.id.overlayMiniSongArtist);
        miniSongDuration = windowView.findViewById(R.id.overlayMiniSongDuration);
        miniSeekPrev = windowView.findViewById(R.id.overlayMiniSeekPrev);
        miniPlayPause = windowView.findViewById(R.id.overlayMiniPlayPause);
        miniSeekNext = windowView.findViewById(R.id.overlayMiniSeekNext);

        // SETTINGS
        opacityText = windowView.findViewById(R.id.overlaySettingsOpacityTextCounter);
        opacitySeekbar = windowView.findViewById(R.id.overlaySettingsOpacitySeekbar);

        // MAIN VIEW
        songsView = windowView.findViewById(R.id.overlayExpandedSongsView);
        playlistsView = windowView.findViewById(R.id.overlayExpandedPlaylistsView);
        albumsView = windowView.findViewById(R.id.overlayExpandedAlbumsView);
        foldersView = windowView.findViewById(R.id.overlayExpandedFoldersView);

        songsRoot = windowView.findViewById(R.id.overlaySongsRoot);
        playlistsRoot = windowView.findViewById(R.id.overlayPlaylistsRoot);
        albumsRoot = windowView.findViewById(R.id.overlayAlbumsRoot);
        foldersRoot = windowView.findViewById(R.id.overlayFoldersRoot);

        // SONGS
        shuffleImage = windowView.findViewById(R.id.overlaySongsShuffle);
        reloadImage = windowView.findViewById(R.id.overlaySongsReload);

        no_songs = windowView.findViewById(R.id.NO_SONGS);

        // TODO every other section

        // STATUS BAR
        songSizeText = windowView.findViewById(R.id.overlayStatusbarSongSizeText);

        //
        //
        //
        songUriArrayList = getAllSongs();
        recyclerView = windowView.findViewById(R.id.overlaySongsRecyclerView);
        if(!(songUriArrayList.size() <= 0)){
            recyclerViewClick();
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(recyclerViewAdapter);
        } else {
            no_songs.setVisibility(View.VISIBLE);
        }

        new InitialPlayer().init(songUriArrayList);
        barVisualizer.setColor(Color.parseColor("#9d79a8"));
        barVisualizer.setDensity(5);

        songSizeText.setText("Loaded " + songUriArrayList.size() + " songs.");

        //Log.d("SONG FINDER metadata-less", "FILE LIST NO METADATA: " + findSongNOMetadata());

//        if(songUriArrayList.size() < 0) {
//            no_songs.setVisibility(View.VISIBLE);
//        }

        // MISC
        songsRoot.setVisibility(View.VISIBLE);
        opacityText.setText(String.valueOf(toLoadSeekbarPos));
        opacitySeekbar.setProgress(toLoadSeekbarPos);
        //new WindowToast().init();

        // EVENTS
        songsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                songsRoot.setVisibility(View.VISIBLE);
                playlistsRoot.setVisibility(View.GONE);
                albumsRoot.setVisibility(View.GONE);
                foldersRoot.setVisibility(View.GONE);
            }
        });
        playlistsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                songsRoot.setVisibility(View.GONE);
                playlistsRoot.setVisibility(View.VISIBLE);
                albumsRoot.setVisibility(View.GONE);
                foldersRoot.setVisibility(View.GONE);
            }
        });
        albumsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                songsRoot.setVisibility(View.GONE);
                playlistsRoot.setVisibility(View.GONE);
                albumsRoot.setVisibility(View.VISIBLE);
                foldersRoot.setVisibility(View.GONE);
            }
        });
        foldersView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                songsRoot.setVisibility(View.GONE);
                playlistsRoot.setVisibility(View.GONE);
                albumsRoot.setVisibility(View.GONE);
                foldersRoot.setVisibility(View.VISIBLE);
            }
        });

        // WINDOW EVENTS
        // MOVEMENT
        mainRoot.setOnTouchListener(new View.OnTouchListener() {

            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //Log.d("onTouch()", "Moving...");
                //Log.d("onTouch()", "Event: " + motionEvent);
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();
                        //Log.d("onTouch()", "ACTION_DOWN");
                        return true;

                    case MotionEvent.ACTION_UP:
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (motionEvent.getRawX() - initialTouchX);
                        params.y = initialY + (int) (motionEvent.getRawY() - initialTouchY);
                        window.updateViewLayout(windowView, params);
                        //updateMiniPlayer();
                        //Log.d("onTouch()", "ACTION_MOVE");
                        return true;
                }
                return false;
            }
        });

        miniImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("to tiny", "Clicked mini artwork");
                Toast.makeText(OverlayPlayer.this, "Activated \"Tiny\" mode!", Toast.LENGTH_SHORT).show();
                windowLayout.setVisibility(View.GONE);
                tinyRoot.setVisibility(View.VISIBLE);
            }
        });
        tinyArtwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("to mini", "Clicked tiny artwork");
                Toast.makeText(OverlayPlayer.this, "Returning to normal", Toast.LENGTH_SHORT).show();
                tinyRoot.setVisibility(View.GONE);
                windowLayout.setVisibility(View.VISIBLE);
            }
        });

        // CONTROLS
        toMiniPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isMainView && !isSettingsView){
                    mainLayout.setVisibility(View.GONE);
                    miniPlayer.setVisibility(View.VISIBLE);
                    isMainView = false;
                } else {
                    mainLayout.setVisibility(View.VISIBLE);
                    miniPlayer.setVisibility(View.GONE);
                    isMainView = true;
                }
            }
        });
        settingsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("onClick() settings", "Clicked settings image.");
                if(!isSettingsView){
                    if(isMainView){
                        mainLayout.setVisibility(View.GONE);
                    } else {
                        miniPlayer.setVisibility(View.GONE);
                    }
                    settingsLayout.setVisibility(View.VISIBLE);
                    toMiniPlayer.setVisibility(View.GONE);
                    isSettingsView = true;
                } else {
                    if(isMainView){
                        mainLayout.setVisibility(View.VISIBLE);
                    } else {
                        miniPlayer.setVisibility(View.VISIBLE);
                    }
                    settingsLayout.setVisibility(View.GONE);
                    toMiniPlayer.setVisibility(View.VISIBLE);
                    isSettingsView = false;
                }
            }
        });
        minimizeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(OverlayPlayer.this, "WIP!", Toast.LENGTH_SHORT).show();
            }
        });
        closeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSelf();
            }
        });

        // MINI PLAYER CONTROLS
        miniPlayPause.setOnClickListener(new View.OnClickListener() {
            int lastPos;
            @Override
            public void onClick(View view) {
                if(InitialPlayer.getPlayer() != null){
                    if(isPlaying){
                        lastPos = InitialPlayer.getPos();
                        miniPlayPause.setImageResource(R.drawable.play);
                        tinyPlayPause.setImageResource(R.drawable.play);
                        InitialPlayer.pause();
                        isPlaying = false;
                    } else {
                        miniPlayPause.setImageResource(R.drawable.pause);
                        tinyPlayPause.setImageResource(R.drawable.pause);
                        InitialPlayer.resume();
                        isPlaying = true;
                    }
                } else {
                    //showToast(OverlayPlayer.this, toastLayout, toastText, "There are no songs playing!");
                }
            }
        });
        tinyPlayPause.setOnClickListener(new View.OnClickListener() {
            int lastPos;
            @Override
            public void onClick(View view) {
                if(InitialPlayer.getPlayer() != null){
                    if(isPlaying){
                        lastPos = InitialPlayer.getPos();
                        miniPlayPause.setImageResource(R.drawable.play);
                        tinyPlayPause.setImageResource(R.drawable.play);
                        InitialPlayer.pause();
                        isPlaying = false;
                    } else {
                        miniPlayPause.setImageResource(R.drawable.pause);
                        tinyPlayPause.setImageResource(R.drawable.pause);
                        InitialPlayer.resume();
                        isPlaying = true;
                    }
                } else {
                    //showToast(OverlayPlayer.this, toastLayout, toastText, "There are no songs playing!");
                }
            }
        });
        miniSeekPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(InitialPlayer.getPlayer() != null){
                    InitialPlayer.seekPrevious(OverlayPlayer.this);
                } else {
                    //showToast(OverlayPlayer.this, toastLayout, toastText, "There are no songs playing!");
                }
            }
        });
        tinyPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(InitialPlayer.getPlayer() != null){
                    InitialPlayer.seekPrevious(OverlayPlayer.this);
                } else {
                    //showToast(OverlayPlayer.this, toastLayout, toastText, "There are no songs playing!");
                }
            }
        });
        miniSeekNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(InitialPlayer.getPlayer() != null){
                    InitialPlayer.seekNext(OverlayPlayer.this);
                } else {
                    //showToast(OverlayPlayer.this, toastLayout, toastText, "There are no songs playing!");
                }
            }
        });
        tinyNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(InitialPlayer.getPlayer() != null){
                    InitialPlayer.seekNext(OverlayPlayer.this);
                } else {
                    //showToast(OverlayPlayer.this, toastLayout, toastText, "There are no songs playing!");
                }
            }
        });

        // SETTINGS
        opacitySeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                opacityText.setText(String.valueOf(i));
                params.alpha = (float) i / 100;
                toSaveOpacity = params.alpha;
                window.updateViewLayout(windowView, params);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // SONGS - CONTROLS
        shuffleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newPos = 0;
                newPos = (int) (Math.random() * songUriArrayList.size());
                Log.d("Shuffle", "Chosen: " + newPos);
                if(InitialPlayer.mediaPlayer != null){ // if not null, stop and play
                    Log.d("OverlayPlayer", "Player is not null!");
                    InitialPlayer.stop();
                    InitialPlayer.play(OverlayPlayer.this, newPos); // will update
                } else {
                    InitialPlayer.play(OverlayPlayer.this, newPos); // will update
                }
                miniPlayPause.setImageResource(R.drawable.pause);
                tinyPlayPause.setImageResource(R.drawable.pause);
                isPlaying = true;
            }
        });
        reloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.setVisibility(View.INVISIBLE);
                songUriArrayList = getAllSongs();
                //reloadImage.animate();
                recyclerViewClick();
                recyclerView.setLayoutManager(new LinearLayoutManager(OverlayPlayer.this));
                recyclerView.setAdapter(recyclerViewAdapter);
                recyclerView.setVisibility(View.VISIBLE);

                reloadImage.startAnimation(AnimationUtils.loadAnimation(OverlayPlayer.this, R.anim.reload_rotate));

                new InitialPlayer().init(songUriArrayList);
                songSizeText.setText("Loaded " + songUriArrayList.size() + " songs.");
            }
        });

    }

    // SONGS METHODS

    public ArrayList<MusicAdapter> getAllSongs(){
        // FIND FILE WITH METADATA
        ArrayList<MusicAdapter> tempAudioList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DISPLAY_NAME
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + "=1";
        Cursor cursor = OverlayPlayer.this.getContentResolver().query(uri, projection, selection, null, "RANDOM()");

        if(cursor != null) {
            while(cursor.moveToNext()){
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                //String ogFileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));

                MusicAdapter musicModel = new MusicAdapter(path, title, artist, album, duration);
                tempAudioList.add(musicModel); // this adds the "files" with metadata
                Log.d("getAllSongs()", "path: " + path);
                //Log.d("ARRAYLIST", "getAllSongs: " + arrayList);
            }
            cursor.close();
        }

        // TODO ??? find files, search for ones with metadata and add them to the arraylist


        return tempAudioList;
    }

    private void recyclerViewClick() {
        recyclerViewAdapter = new SongRecyclerViewAdapter(OverlayPlayer.this, songUriArrayList, new SongRecyclerViewAdapter.OnRecyclerViewClick() {
            @Override
            public void OnItemClick(View view, int position) {
                Log.d("Song Title", "OnItemClick: " + songUriArrayList.get(position).getTitle());
                Log.d("SongURI", "OnItemClick: Uri:" + Uri.parse(songUriArrayList.get(position).getPath()));

                if(InitialPlayer.mediaPlayer != null){ // if not null, stop and play
                    Log.d("OverlayPlayer", "Player is not null!");
                    InitialPlayer.stop();
                    InitialPlayer.play(OverlayPlayer.this, position); // will update
                    //updateMiniPlayer();
                } else {
                    InitialPlayer.play(OverlayPlayer.this, position); // will update
                    //updateMiniPlayer();
                }
                miniPlayPause.setImageResource(R.drawable.pause);
                tinyPlayPause.setImageResource(R.drawable.pause);
                isPlaying = true;
            }
        }, new SongRecyclerViewAdapter.OnRecyclerViewLongClick() {
            @Override
            public void OnItemLongClick(View view, int position) {
                Log.d("LONGCLICK", "OnItemLongClick: Long clicked on " + songUriArrayList.get(position).getTitle());
            }
        });
    }


    // MINI PLAYER METHODS
    public static void updateMiniPlayer(){

        miniSongTitle.setText(songUriArrayList.get(InitialPlayer.getPos()).getTitle());
        miniSongArtist.setText(songUriArrayList.get(InitialPlayer.getPos()).getArtist());
        miniSongDuration.setText(getTimeFormatted(songUriArrayList.get(InitialPlayer.getPos()).getDuration()));

        tinySongTitle.setText(songUriArrayList.get(InitialPlayer.getPos()).getTitle());

        byte[] image = getAlbumArt(songUriArrayList.get(InitialPlayer.getPos()).getPath());
        if(image != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            miniImage.setImageBitmap(bitmap);
            tinyArtwork.setImageBitmap(bitmap);
            miniPlayer.setBackgroundColor(getDominantColor(bitmap));
            //tinyRoot.setBackgroundColor(getDominantColor(bitmap));
            Log.d("onBindViewHolder()", "Loading Artwork.");
        } else {
            miniImage.setImageResource(R.drawable.song_image);
            tinyArtwork.setImageResource(R.drawable.song_image);
            miniPlayer.setBackgroundColor(Color.rgb(26, 26, 26));
            Log.d("onBindViewHolder()", "Loading -Placeholder.");
        }

        if(barVisualizer.getVisualizer() != null){
            barVisualizer.release();
        }
        barVisualizer.setPlayer(InitialPlayer.getPlayer().getAudioSessionId());
    }

    public static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //showToast(this, toastLayout, toastText, "Thank you for using this app!");
        saveData();
        if(windowView != null){
            window.removeView(windowView);
        }
        barVisualizer.release();
        InitialPlayer.stop();
    }

    public static BarVisualizer getVisualizer(){
        return barVisualizer;
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

    private static byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        byte[] art = null;
        if(retriever.getEmbeddedPicture() != null) {
            art = retriever.getEmbeddedPicture();
        }
        retriever.release();
        return art;
    }

    private void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(prefString, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putFloat(toSaveOpacityString, toSaveOpacity);
        editor.putInt(toSaveSeekbarPosString, opacitySeekbar.getProgress());

        editor.apply();
        editor.commit();
    }
    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(prefString, MODE_PRIVATE);

        toLoadOpacity = sharedPreferences.getFloat(toSaveOpacityString, 1);
        toLoadSeekbarPos = sharedPreferences.getInt(toSaveSeekbarPosString, 50);
    }
}
