package com.panniku.mp3player.Overlay;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.panniku.mp3player.*;
import com.panniku.mp3player.Adapters.PlaylistsRecyclerViewAdapter;
import com.panniku.mp3player.Adapters.SecondaryAdapter;
import com.panniku.mp3player.Adapters.SelectorRecyclerViewAdapter;
import com.panniku.mp3player.Constructors.PlaylistsConstructor;
import com.panniku.mp3player.Constructors.SongsConstructor;
import com.panniku.mp3player.Adapters.SongRecyclerViewAdapter;
import com.panniku.mp3player.Utils.Utils;
import com.panniku.mp3player.Visualizer.BarVisualizer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.UUID;

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

    private ImageView tinyDrag;
    private static RelativeLayout tinyRoot;
    private static ImageView tinyArtwork;
    private static TextView tinySongTitle;
    private static ImageView tinyPrev, tinyPlayPause, tinyNext;

    // MAIN WINDOW
    private RelativeLayout windowLayout, mainLayout;
    static private View windowView;
    private WindowManager window;

    // CONTROLS
    public static BarVisualizer barVisualizer;
    private ImageView toMiniPlayer, settingsImage, minimizeImage, closeImage;

    // SECONDARY VIEW
    private RelativeLayout secondaryRoot;

    static private RecyclerView secondaryRecyclerView;
    static private SecondaryAdapter secondaryRecyclerViewAdapter;
    static private ArrayList<SongsConstructor> secondaryArrayList;
    static private ArrayList<ArrayList<SongsConstructor>> totalSecondaryArrayList = new ArrayList<>();

    private ImageView secondaryExit;
    private ImageView secondaryThumb;
    private TextView secondaryTitle;

    private ImageView secondaryAddSong, secondaryListSong, secondarySettings;

    // TODO settings

    // MINI PLAYER
    private static RelativeLayout miniPlayer;
    // CONTROLS
    private static ImageView miniImage;
    private static TextView miniSongTitle;
    private static TextView miniSongArtist;
    private static TextView miniSongDuration;
    private static ImageView miniSeekPrev, miniPlayPause, miniSeekNext;
    private static ImageView miniLoop;

    private static TextView miniExpandedCurDur, miniExpandedMaxDur;
    private static SeekBar miniExpandedSeekBar;

    private RelativeLayout miniExpandedLayout;
    private ImageView miniToggleLayout;

    // SETTINGS
    private RelativeLayout settingsLayout;

    private TextView opacityText;
    private SeekBar opacitySeekbar;

    private RelativeLayout songsView, playlistsView, albumsView, foldersView;
    private RelativeLayout songsRoot, playlistsRoot, albumsRoot, foldersRoot;

    // SONGS
    private ImageView shuffleImage, reloadImage;

    private static ArrayList<SongsConstructor> songUriArrayList;
    private static SongRecyclerViewAdapter songRecyclerViewAdapter;
    private RecyclerView songsRecyclerView;


    // PLAYLISTS
    private TextView playlistAddList;

    private static ArrayList<PlaylistsConstructor> playlistArrayList;
    private static PlaylistsRecyclerViewAdapter playlistsRecyclerViewAdapter;
    private RecyclerView playlistsRecyclerView;

    // TODO sections

    // STATUS BAR
    private static TextView statusbarText;

    // DATA - SAVING AND LOADING
    //TEMP
    // SAVE
    private static final String prefString = "overlayWindowPref";

    private static final String toSaveTotalArray = "toSaveTotalArray";

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
    int durWidth;

    boolean isExpanded = false;
    private static String isLoop;

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

        //animTime = this.getResources().getInteger(android.R.integer.config_shortAnimTime);
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

        tinyDrag = windowView.findViewById(R.id.overlayTinyDrag);
        tinyRoot = windowView.findViewById(R.id.overlayTinyRoot);
        tinyArtwork = windowView.findViewById(R.id.overlayTinyArtwork);
        tinySongTitle = windowView.findViewById(R.id.overlayTinySongTitle);
        tinyPrev = windowView.findViewById(R.id.overlayTinySeekPrev);
        tinyPlayPause = windowView.findViewById(R.id.overlayTinyPlayPause);
        tinyNext = windowView.findViewById(R.id.overlayTinySeekNext);

        // MAIN WINDOW AND ROOT LAYOUTS
        windowLayout = windowView.findViewById(R.id.overlayExpandedRoot);
        mainLayout = windowView.findViewById(R.id.overlayExpandedHolderBase);
        secondaryRoot = windowView.findViewById(R.id.overlaySecondaryRoot);
        miniPlayer = windowView.findViewById(R.id.overlayMiniRoot);
        settingsLayout = windowView.findViewById(R.id.overlaySettingsRoot);

        // SECONDARY

        secondaryAddSong = windowView.findViewById(R.id.overlaySecondaryAddSongs);
        secondaryListSong = windowView.findViewById(R.id.overlaySecondarySortSongs);
        secondarySettings = windowView.findViewById(R.id.overlaySecondarySettings);

        secondaryExit = windowView.findViewById(R.id.overlaySecondaryExit);
        secondaryThumb = windowView.findViewById(R.id.overlaySecondaryThumb);
        secondaryTitle = windowView.findViewById(R.id.overlaySecondaryName);

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
        miniLoop = windowView.findViewById(R.id.overlayMiniLoop);

        miniExpandedCurDur = windowView.findViewById(R.id.overlayMiniSongCurrentDuration);
        miniExpandedMaxDur = windowView.findViewById(R.id.overlayMiniSongMaxDuration);
        miniExpandedSeekBar = windowView.findViewById(R.id.overlayMiniSeekBar);

        miniExpandedLayout = windowView.findViewById(R.id.overlayMiniExpandedControls);
        miniToggleLayout = windowView.findViewById(R.id.overlayMiniExpander);

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

        // PLAYLISTS
        playlistAddList = windowView.findViewById(R.id.overlayPlaylistsAddNewList);



        // TODO every other section

        // STATUS BAR
        statusbarText = windowView.findViewById(R.id.overlayStatusbarSongSizeText);

        setSecondaryRecyclerView(OverlayPlayer.this);

        // SONGS - RECYCLERVIEW
        songUriArrayList = getAllSongs(this, true);
        songsRecyclerView = windowView.findViewById(R.id.overlaySongsRecyclerView);
        songsRecyclerView.setHasFixedSize(true);
        if(!(songUriArrayList.size() <= 0)){
            songRecyclerViewClick();
            songRecyclerViewAdapter.setHasStableIds(false);
            songsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            songsRecyclerView.setAdapter(songRecyclerViewAdapter);
            songsRecyclerView.setNestedScrollingEnabled(false);
            songRecyclerViewAdapter.notifyDataSetChanged();
        } else {
            no_songs.setVisibility(View.VISIBLE);
        }

        //
        //
        //
        // PLAYLISTS - RECYCLERVIEW
        playlistArrayList = new ArrayList<>();
        playlistsRecyclerView = windowView.findViewById(R.id.overlayPlaylistsRecyclerView);
        playlistsItemRecyclerViewClick();
        playlistsRecyclerViewAdapter.setHasStableIds(false);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        playlistsRecyclerView.setLayoutManager(manager);
        playlistsRecyclerView.setAdapter(playlistsRecyclerViewAdapter);
        playlistsRecyclerView.setNestedScrollingEnabled(false);

        barVisualizer.setColor(Color.parseColor("#9d79a8"));
        barVisualizer.setDensity(5); // n + 1
        statusbarText.setText("Loaded " + songUriArrayList.size() + " songs.");
        isLoop = "All";

        tinySongTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tinySongTitle.setSelected(true);

        miniSongTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        miniSongTitle.setSelected(true);

        miniSongArtist.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        miniSongArtist.setSelected(true);

        statusbarText.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        statusbarText.setSelected(true);

        // MISC
        new InitialPlayer().init(songUriArrayList);

        songsRoot.setVisibility(View.VISIBLE);
        opacityText.setText(String.valueOf(toLoadSeekbarPos));
        opacitySeekbar.setProgress(toLoadSeekbarPos);

        durWidth = miniSongDuration.getLayoutParams().width;

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
        windowLayout.setOnTouchListener(new View.OnTouchListener() {

            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int width = getResources().getDisplayMetrics().widthPixels / 2;
                int height = getResources().getDisplayMetrics().heightPixels / 2;

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
                        Log.d("move", "params.x: " + params.x + " wid: " + width);
                        Log.d("move", "params.y: " + params.y + " hei: " + height);
                        if(params.x > width){
                            params.x = width;
                            window.updateViewLayout(windowView, params);
                        }
                        if(params.x < -width){
                            params.x = -width;
                            window.updateViewLayout(windowView, params);
                        }
                        if(params.y > height){
                            params.y = height;
                            window.updateViewLayout(windowView, params);
                        }
                        if(params.y < -height){
                            params.y = -height;
                            window.updateViewLayout(windowView, params);
                        }
                        //statusbarText.setText("parX: " + params.x + " parY:" + params.y);
                        window.updateViewLayout(windowView, params);
                        //updateMiniPlayer();
                        //Log.d("onTouch()", "ACTION_MOVE");
                        //Log.d("onTouch() ACTION_MOVE", "X, Y: " + params.x + ", " + params.y);
                        //statusbarText.setText(" RAW X: " + motionEvent.getRawX() + " RAW Y: " + motionEvent.getRawY());
                        return true;
                }
                return false;
            }
        });
        tinyDrag.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            int width = getResources().getDisplayMetrics().widthPixels / 2;
            int height = getResources().getDisplayMetrics().heightPixels / 2;

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
                        if(params.x > width){
                            params.x = width;
                            window.updateViewLayout(windowView, params);
                        }
                        if(params.x < -width){
                            params.x = -width;
                            window.updateViewLayout(windowView, params);
                        }
                        if(params.y > height){
                            params.y = height;
                            window.updateViewLayout(windowView, params);
                        }
                        if(params.y < -height){
                            params.y = -height;
                            window.updateViewLayout(windowView, params);
                        }
                        window.updateViewLayout(windowView, params);
                        //updateMiniPlayer();

                        return true;
                }
                return false;
            }
        });

        // SECONDARY CONTROLS
        secondaryAddSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OverlayPlayer.this, OverlaySongsSelector.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        // TODO add the other 2 controls

        // CONTROLS
        toMiniPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isMainView && !isSettingsView){
                    toMiniPlayer.setImageResource(R.drawable.from_miniplayer);
                    mainLayout.setVisibility(View.GONE);
                    miniPlayer.setVisibility(View.VISIBLE);
                    isMainView = false;
                } else {
                    toMiniPlayer.setImageResource(R.drawable.to_miniplayer);
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
                tinyRoot.setVisibility(View.VISIBLE);
                windowLayout.setVisibility(View.GONE);
            }

        });
        tinyArtwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tinyRoot.setVisibility(View.GONE);
                windowLayout.setVisibility(View.VISIBLE);
            }
        });
        closeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSelf();
            }
        });

        // SECONDARY CONTROLS
        secondaryExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                secondaryRoot.setVisibility(View.GONE);
                mainLayout.setVisibility(View.VISIBLE);
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
                        InitialPlayer.resume(OverlayPlayer.this);
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
                        InitialPlayer.resume(OverlayPlayer.this);
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
//                    if(!isPlaying){
//                        InitialPlayer.pause();
//                    }
                }
            }
        });
        tinyPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(InitialPlayer.getPlayer() != null){
                    InitialPlayer.seekPrevious(OverlayPlayer.this);
//                    if(!isPlaying){
//                        InitialPlayer.pause();
//                    }
                }
            }
        });
        miniSeekNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(InitialPlayer.getPlayer() != null){
                    InitialPlayer.seekNext(OverlayPlayer.this);
//                    if(!isPlaying){
//                        InitialPlayer.pause();
//                    }
                }
            }
        });
        tinyNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(InitialPlayer.getPlayer() != null){
                    InitialPlayer.seekNext(OverlayPlayer.this);
//                    if(!isPlaying){
//                        InitialPlayer.pause();
//                    }
                }
            }
        });

        miniLoop.setOnClickListener(new View.OnClickListener() {
            int i = 0;
            @Override
            public void onClick(View view) {
                if(i > 1) i = 0;
                switch(i){
                    case 0:
                        miniLoop.setImageResource(R.drawable.loop_this);
                        isLoop = "This";
                        break;
                    case 1:
                        miniLoop.setImageResource(R.drawable.loop_all);
                        isLoop = "All";
                        break;
                }
                i++;
            }
        });

        miniToggleLayout.setOnClickListener(new View.OnClickListener() {
            ViewGroup.LayoutParams Lparams, Tparams;
            @Override
            public void onClick(View view) {
                Lparams = miniExpandedLayout.getLayoutParams();
                Tparams = miniSongDuration.getLayoutParams();
                if(!isExpanded){
                    Lparams.height = 120;
                    Tparams.width = 0;
                    miniExpandedLayout.setLayoutParams(Lparams);
                    miniSongDuration.setLayoutParams(Tparams);
                    miniToggleLayout.setRotation(180f);
                    isExpanded = true;
                } else {
                    Lparams.height = 0;
                    Tparams.width = durWidth;
                    miniExpandedLayout.setLayoutParams(Lparams);
                    miniSongDuration.setLayoutParams(Tparams);
                    miniToggleLayout.setRotation(0f);
                    isExpanded = false;
                }
            }
        });

        miniExpandedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                miniExpandedCurDur.setText(Utils.getTimeFormatted(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("onStartTrackingTouch()", "pos: " + miniExpandedSeekBar.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(InitialPlayer.getPlayer() != null){
                    InitialPlayer.getPlayer().seekTo(miniExpandedSeekBar.getProgress());
                    if(!isPlaying){
                        InitialPlayer.setLength(miniExpandedSeekBar.getProgress());
                    }
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
                statusbarText.setText("Shuffling...");
                int newPos = 0;
                newPos = (int) (Math.random() * songUriArrayList.size());
                Log.d("Shuffle", "Chosen: " + newPos);
                InitialPlayer.play(OverlayPlayer.this, newPos); // will update
                statusbarText.setText("Done!");
                miniPlayPause.setImageResource(R.drawable.pause);
                tinyPlayPause.setImageResource(R.drawable.pause);
                isPlaying = true;
            }
        });
        reloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statusbarText.setText("Reloading...");
                songsRecyclerView.setVisibility(View.INVISIBLE);
                songUriArrayList = getAllSongs(OverlayPlayer.this, true);
                //reloadImage.animate();
                songRecyclerViewClick();
                songsRecyclerView.setLayoutManager(new LinearLayoutManager(OverlayPlayer.this));
                songsRecyclerView.setAdapter(songRecyclerViewAdapter);
                songRecyclerViewAdapter.notifyDataSetChanged();
                songsRecyclerView.setVisibility(View.VISIBLE);

                statusbarText.setText("Done!");
                reloadImage.startAnimation(AnimationUtils.loadAnimation(OverlayPlayer.this, R.anim.reload_rotate));

                new InitialPlayer().init(songUriArrayList);
                statusbarText.setText("Loaded " + songUriArrayList.size() + " songs.");
            }
        });

        // PLAYLISTS - CONTROLS
        playlistAddList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OverlayPlaylistsEditor.setAction("New Playlist");
                Intent intent = new Intent(OverlayPlayer.this, OverlayPlaylistsEditor.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

    }

    // SONGS METHODS

    public static ArrayList<SongsConstructor> getAllSongs(Context context, Boolean shouldRandomize){
        ArrayList<SongsConstructor> tempAudioList = new ArrayList<>();
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
        Cursor cursor;
        if(shouldRandomize){
            cursor = context.getContentResolver().query(uri, projection, selection, null, "RANDOM()");
        } else {
            cursor = context.getContentResolver().query(uri, projection, selection, null, null);
        }

        if(cursor != null) {
            while(cursor.moveToNext()){
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                //String ogFileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));

                SongsConstructor musicModel = new SongsConstructor(path, title, artist, album, duration);
                tempAudioList.add(musicModel); // this adds the "files" with metadata
                Log.d("getAllSongs()", "path: " + path);
                //Log.d("ARRAYLIST", "getAllSongs: " + arrayList);
            }
            cursor.close();
        }
        return tempAudioList;
    }

    private static void rcViewClick(Context context, ArrayList<SongsConstructor> songs, int position){
        new InitialPlayer().init(songs);
        if(InitialPlayer.mediaPlayer != null){ // if not null, stop and play
            Log.d("OverlayPlayer", "Player is not null!");
            InitialPlayer.stop();
            InitialPlayer.play(context, position); // will update
        } else {
            InitialPlayer.play(context, position); // will update
        }
        miniPlayPause.setImageResource(R.drawable.pause);
        tinyPlayPause.setImageResource(R.drawable.pause);
        isPlaying = true;
    }

    public static void setSecondaryRecyclerView(Context context){
        secondaryRecyclerView = windowView.findViewById(R.id.overlaySecondaryRecyclerView);
        secondaryRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        secondaryRecyclerView.setNestedScrollingEnabled(false);
    }

    public static void updateSecondaryRecyclerView(Context context, ArrayList<SongsConstructor> arrayList){
        updateTotalList(0, arrayList);
        secondaryRecyclerView = windowView.findViewById(R.id.overlaySecondaryRecyclerView);
        if(arrayList != null){
            if(!(arrayList.size() <= 0)){
                secondaryRecyclerViewAdapter = new SecondaryAdapter(context, arrayList, new SelectorRecyclerViewAdapter.OnRecyclerViewClick() {
                    @Override
                    public void OnItemClick(View view, int position) {
                        rcViewClick(context, arrayList, position);
                    }
                });
                secondaryRecyclerView.setAdapter(secondaryRecyclerViewAdapter);
                secondaryRecyclerViewAdapter.notifyDataSetChanged();
            }
        } else {
            secondaryRecyclerViewAdapter = new SecondaryAdapter(context, null, new SelectorRecyclerViewAdapter.OnRecyclerViewClick() {
                @Override
                public void OnItemClick(View view, int position) {
                    rcViewClick(context, arrayList, position);
                }
            });
            secondaryRecyclerView.setAdapter(secondaryRecyclerViewAdapter);
            secondaryRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    private void songRecyclerViewClick() {
        songRecyclerViewAdapter = new SongRecyclerViewAdapter(OverlayPlayer.this, songUriArrayList, new SongRecyclerViewAdapter.OnRecyclerViewClick() {
            @Override
            public void OnItemClick(View view, int position) {
                Log.d("Song Title", "OnItemClick: " + songUriArrayList.get(position).getTitle());
                Log.d("SongURI", "OnItemClick: Uri:" + Uri.parse(songUriArrayList.get(position).getPath()));

                rcViewClick(OverlayPlayer.this, songUriArrayList, position);
            }
        }, new SongRecyclerViewAdapter.OnRecyclerViewLongClick() {
            @Override
            public void OnItemLongClick(View view, int position) {
                Log.d("LONGCLICK", "OnItemLongClick: Long clicked on " + songUriArrayList.get(position).getTitle());
            }
        });
    }

    private void playlistsItemRecyclerViewClick(){
        playlistsRecyclerViewAdapter = new PlaylistsRecyclerViewAdapter(this, playlistArrayList, new PlaylistsRecyclerViewAdapter.OnRecyclerViewClick() {
            @Override
            public void OnItemClick(View view, int position) {
                mainLayout.setVisibility(View.GONE);
                secondaryRoot.setVisibility(View.VISIBLE);


                secondaryTitle.setText(playlistArrayList.get(position).getName());
                if(totalSecondaryArrayList.size() != 0){
                    secondaryArrayList = totalSecondaryArrayList.get(position);
                }
                Log.d("total list", "somethng: " + totalSecondaryArrayList);
                Log.d("playlist on click", "total list: " + totalSecondaryArrayList);
                updateSecondaryRecyclerView(OverlayPlayer.this, secondaryArrayList);
            }
        }, new PlaylistsRecyclerViewAdapter.OnRecyclerViewLongClick() {
            @Override
            public void OnItemLongClick(View view, int position) {
                // TODO pLong
            }
        });
    }

    public static void addPlaylist(Bitmap thumb, String name, String date){
        playlistArrayList.add(playlistArrayList.size(), new PlaylistsConstructor(thumb, name, date));
        playlistsRecyclerViewAdapter.notifyDataSetChanged();
    }

    public static void updateTotalList(int pos, ArrayList<SongsConstructor> newList){
        Log.d("updateTotalList", "list: " + newList);
        totalSecondaryArrayList.add(newList);
    }

    // MINI PLAYER METHODS
    public static void updateMiniPlayer(){

        miniSongTitle.setText(InitialPlayer.getArrayList().get(InitialPlayer.getPos()).getTitle());
        miniSongArtist.setText(InitialPlayer.getArrayList().get(InitialPlayer.getPos()).getArtist());
        miniSongDuration.setText(Utils.getTimeFormatted(InitialPlayer.getArrayList().get(InitialPlayer.getPos()).getDuration()));
        miniExpandedMaxDur.setText(Utils.getTimeFormatted(InitialPlayer.getArrayList().get(InitialPlayer.getPos()).getDuration()));

        tinySongTitle.setText(InitialPlayer.getArrayList().get(InitialPlayer.getPos()).getTitle());

        miniPlayPause.setImageResource(R.drawable.pause);
        tinyPlayPause.setImageResource(R.drawable.pause);

        Bitmap bitmap = Utils.getAlbumArt(InitialPlayer.getArrayList().get(InitialPlayer.getPos()).getPath());
        if(bitmap != null) {
            //Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            miniImage.setImageBitmap(bitmap);
            tinyArtwork.setImageBitmap(bitmap);
            miniPlayer.setBackgroundColor(Utils.getDominantColor(bitmap));
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

        miniExpandedSeekBar.setMax(InitialPlayer.getPlayer().getDuration());
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            int curPos;
            String curDur;
            @Override
            public void run() {
                if(isPlaying){
                    curPos = InitialPlayer.getPlayer().getCurrentPosition();
                    curDur = Utils.getTimeFormatted(InitialPlayer.getPlayer().getCurrentPosition());

                    //miniExpandedCurDur.setText(curDur);
                    miniExpandedSeekBar.setProgress(curPos);
                    handler.postDelayed(this, 100);
                }
            }
        }, 100);

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
        Toast.makeText(this, "Thank you for using this app!", Toast.LENGTH_SHORT).show();
    }

    public static boolean getIsPlaying(){
        return isPlaying;
    }
    public static void setIsPlaying(boolean bool){
        isPlaying = bool;
    }
    public static BarVisualizer getVisualizer(){
        return barVisualizer;
    }
    public static TextView getStatusbarText(){
        return statusbarText;
    }
    public static String getCurrentLoop(){
        return isLoop;
    }

    // LOADING AND SAVING

    private void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(prefString, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(secondaryArrayList);
        editor.putString(toSaveTotalArray, json);

        editor.putFloat(toSaveOpacityString, toSaveOpacity);
        editor.putInt(toSaveSeekbarPosString, opacitySeekbar.getProgress());

        editor.apply();
        editor.commit();
    }
    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(prefString, MODE_PRIVATE);

        Gson gson = new Gson();

        String json = sharedPreferences.getString(toSaveTotalArray, null);
        Type type = new TypeToken<ArrayList<SongsConstructor>>() {}.getType();
        secondaryArrayList = gson.fromJson(json, type);

        toLoadOpacity = sharedPreferences.getFloat(toSaveOpacityString, 1);
        toLoadSeekbarPos = sharedPreferences.getInt(toSaveSeekbarPosString, 50);
    }
}
