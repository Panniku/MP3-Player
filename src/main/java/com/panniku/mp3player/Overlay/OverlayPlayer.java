package com.panniku.mp3player.Overlay;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.util.SparseArray;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.panniku.mp3player.*;
import com.panniku.mp3player.Adapters.PlaylistsRecyclerViewAdapter;
import com.panniku.mp3player.Adapters.SecondaryAdapter;
import com.panniku.mp3player.Adapters.SelectorRecyclerViewAdapter;
import com.panniku.mp3player.Constructors.PlaylistsConstructor;
import com.panniku.mp3player.Constructors.SongsConstructor;
import com.panniku.mp3player.Adapters.SongRecyclerViewAdapter;
import com.panniku.mp3player.Notification.NotificationCreator;
import com.panniku.mp3player.Utils.Utils;
import com.panniku.mp3player.Visualizer.BarVisualizer;
import com.squareup.picasso.Picasso;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class OverlayPlayer extends Service {

    /*
    * This class will house all the main components of this window:
    * 1. Main overlay window
    * 2. Container of all 4 layouts with additional (Playlist, folders.........)
    * 3. Functionality of all 4 layouts
    * 4. Miscellaneous code
    * */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static RelativeLayout tinyRoot;
    private static ImageView tinyArtwork;
    private static TextView tinySongTitle;

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

    private TextView tinyTypeText;
    private TextView tinyTogglerTxt;

    private TextView rewriteCache;

    private TextView toAboutView;


    //
    private RelativeLayout songsView, playlistsView, albumsView, downloaderView;
    private RelativeLayout songsRoot, playlistsRoot, albumsRoot, downloaderRoot;


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

    // DOWNLOADER
    private RelativeLayout downloaderBuffer;
    private ImageView downloaderHelp;
    private ImageView downloaderPaste;
    private ImageView downloaderKB;
    private EditText downloaderURLText;
    private ImageView downloaderSearch;
    private RelativeLayout downloaderInfo;
    private TextView dlName, dlAuthor, dlViews;
    private ImageView dlThumb;
    private ImageView downloaderDL;
    public static String songURL;
    public static String dlURL, dlNameStr;

    // TODO sections

    // ABOUT
    private RelativeLayout aboutRoot;

    // STATUS BAR
    private static TextView statusbarText;


    //
    // NOTIFICATION
    private NotificationManager notifManager;

    // DATA - SAVING AND LOADING
    //TEMP
    // SAVE
    private static final String prefString = "overlayWindowPref";

    private static final String toSaveTotalArray = "toSaveTotalArray";

    private static final String toSaveOpacityString = "toSaveOpacity";
    private static final String toSaveSeekbarPosString = "toSaveSeekbarPosString";

    private static final String toSaveTinyPlayerTypeString = "toSaveTinyPlayerTypeString";
    private static final String toSaveTinyChangeString = "toSaveTinyChangeString";

    //
    private static float toSaveOpacity;
    private static int toSaveSeekbarPos;

    private static String toSaveType;
    private static String toSaveTxt;

    // LOAD
    private static float toLoadOpacity;
    private static int toLoadSeekbarPos;

    private static String toLoadTinyPlayerType;
    private static String toLoadTinyChangeTxt;

    // CACHE
    // WRITE CACHE
    private OutputStream outputStream;
    public static String cacheDir;

    // MISC
    private static Context thisContext;
    private RelativeLayout no_songs;
    private boolean isMainView = true, isSettingsView = false;
    private static boolean isPlaying;
    private static boolean isNotif;
    private String prevStatus;
    int animTime;
    int durWidth;

    boolean isExpanded = false;
    private static String isLoop;
    ImageView secret;

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

        tinyRoot = windowView.findViewById(R.id.overlayTinyRoot);
        tinyArtwork = windowView.findViewById(R.id.overlayTinyArtwork);
        tinySongTitle = windowView.findViewById(R.id.overlayTinySongTitle);

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

        tinyTypeText = windowView.findViewById(R.id.overlaySettingsTinyPlayerType);
        tinyTogglerTxt = windowView.findViewById(R.id.overlaySettingsTinyPlayerToggler);

        rewriteCache = windowView.findViewById(R.id.settingRecacheText);

        toAboutView = windowView.findViewById(R.id.settingsAboutTxt);

        // MAIN VIEW
        songsView = windowView.findViewById(R.id.overlayExpandedSongsView);
        playlistsView = windowView.findViewById(R.id.overlayExpandedPlaylistsView);
        albumsView = windowView.findViewById(R.id.overlayExpandedAlbumsView);
        downloaderView = windowView.findViewById(R.id.overlayExpandedDownloaderView);

        songsRoot = windowView.findViewById(R.id.overlaySongsRoot);
        playlistsRoot = windowView.findViewById(R.id.overlayPlaylistsRoot);
        albumsRoot = windowView.findViewById(R.id.overlayAlbumsRoot);
        downloaderRoot = windowView.findViewById(R.id.overlayDownloaderRoot);

        // SONGS
        shuffleImage = windowView.findViewById(R.id.overlaySongsShuffle);
        reloadImage = windowView.findViewById(R.id.overlaySongsReload);

        no_songs = windowView.findViewById(R.id.NO_SONGS);

        // PLAYLISTS
        playlistAddList = windowView.findViewById(R.id.overlayPlaylistsAddNewList);

        // DOWNLOADER
        downloaderBuffer = windowView.findViewById(R.id.overlayDownloaderBuffer);
        downloaderHelp = windowView.findViewById(R.id.downloaderHelp);
        downloaderPaste = windowView.findViewById(R.id.downloaderPasteText);
        downloaderKB = windowView.findViewById(R.id.downloaderToggleKB);
        downloaderURLText = windowView.findViewById(R.id.downloaderURLText);
        downloaderSearch = windowView.findViewById(R.id.downloaderSearchURL);
        downloaderInfo = windowView.findViewById(R.id.downloaderInfo);
        dlName = windowView.findViewById(R.id.dlName);
        dlAuthor = windowView.findViewById(R.id.dlAuthor);
        dlViews = windowView.findViewById(R.id.dlViews);
        dlThumb = windowView.findViewById(R.id.dlThumb);
        downloaderDL = windowView.findViewById(R.id.downloaderDLURL);

        // TODO every other section

        // about
        aboutRoot = windowView.findViewById(R.id.overlayAboutRoot);

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
            songsRecyclerView.setHasFixedSize(true);
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

        barVisualizer.setColor(Color.parseColor("#FFFFFF"));
        barVisualizer.setDensity(5); // n + 1
        statusbarText.setText("Loaded " + songUriArrayList.size() + " songs.");
        isLoop = "All";


        //
        //
        //
        // NOTIFICATION
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            createChannel();
//        }

        //startService(new Intent(OverlayPlayer.this, RecentServiceClear.class));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel();
        }
        registerReceiver(receiver, new IntentFilter("TOGGLER"));


        // MISC
        thisContext = OverlayPlayer.this;
        new InitialPlayer().init(songUriArrayList);

        songsRoot.setVisibility(View.VISIBLE);
        playlistsRoot.setVisibility(View.GONE);
        albumsRoot.setVisibility(View.GONE);
        downloaderRoot.setVisibility(View.GONE);
        settingsLayout.setVisibility(View.GONE);

        opacityText.setText(String.valueOf(toLoadSeekbarPos));
        opacitySeekbar.setProgress(toLoadSeekbarPos);

        tinyTypeText.setText(toLoadTinyPlayerType);
        tinyTogglerTxt.setText(toLoadTinyChangeTxt);

        if(toLoadTinyPlayerType == "Notification"){
            isNotif = true;
        } else {
            isNotif = false;
        }
        durWidth = miniSongDuration.getLayoutParams().width;

        secret = windowView.findViewById(R.id.secret);
        secret.setOnClickListener(new View.OnClickListener() {
            int taps = 0;
            @Override
            public void onClick(View view) {
                taps++;
                if(taps == 4){
                    Toast.makeText(OverlayPlayer.this, "???", Toast.LENGTH_SHORT).show();
                    taps = 0;
                }
            }
        });
        //new WindowToast().init();

        // EVENTS
        songsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                songsRoot.setVisibility(View.VISIBLE);
                playlistsRoot.setVisibility(View.GONE);
                albumsRoot.setVisibility(View.GONE);
                downloaderRoot.setVisibility(View.GONE);
            }
        });
        playlistsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                songsRoot.setVisibility(View.GONE);
                playlistsRoot.setVisibility(View.VISIBLE);
                albumsRoot.setVisibility(View.GONE);
                downloaderRoot.setVisibility(View.GONE);
            }
        });
        albumsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                songsRoot.setVisibility(View.GONE);
                playlistsRoot.setVisibility(View.GONE);
                albumsRoot.setVisibility(View.VISIBLE);
                downloaderRoot.setVisibility(View.GONE);
            }
        });
        downloaderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                songsRoot.setVisibility(View.GONE);
                playlistsRoot.setVisibility(View.GONE);
                albumsRoot.setVisibility(View.GONE);
                downloaderRoot.setVisibility(View.VISIBLE);
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
                int width = (getResources().getDisplayMetrics().widthPixels / 2);
                int height = (getResources().getDisplayMetrics().heightPixels / 2);

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
                        params.x = (int) ((initialX + motionEvent.getRawX()) - initialTouchX);
                        params.y = (int) ((initialY + motionEvent.getRawY()) - initialTouchY);
                        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                        //params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                        //Log.d("move", "params.x: " + params.x + " wid: " + width);
                        //Log.d("move", "params.y: " + params.y + " hei: " + height);
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
        tinyRoot.setOnTouchListener(new View.OnTouchListener() {
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
                        params.x = (int) ((initialX + motionEvent.getRawX()) - initialTouchX);
                        params.y = (int) ((initialY + motionEvent.getRawY()) - initialTouchY);
                        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
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
                windowLayout.setVisibility(View.GONE);
                Log.d("GETTING NOTIF SAVE TYPE", "onClick: " + isNotif + " | " + toLoadTinyPlayerType);
                if(isNotif){
                    NotificationCreator.init(OverlayPlayer.this);
                    Toast.makeText(OverlayPlayer.this, "Overlay minimized to Notification.", Toast.LENGTH_SHORT).show();
                } else if(!isNotif) {
                    tinyRoot.setVisibility(View.VISIBLE);
                    Toast.makeText(OverlayPlayer.this, "To maximize overlay, click on the song image.", Toast.LENGTH_SHORT).show();
                }
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
                        InitialPlayer.pause();
                        isPlaying = false;
                    } else {
                        miniPlayPause.setImageResource(R.drawable.pause);
                        try {
                            InitialPlayer.resume(OverlayPlayer.this);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
                    try {
                        InitialPlayer.seekPrevious(OverlayPlayer.this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
                    try {
                        InitialPlayer.seekNext(OverlayPlayer.this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

        tinyTogglerTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("toLoadTinyPlayerType: ", "Value: " + toLoadTinyPlayerType);
                if(isNotif){
                    tinyTogglerTxt.setText("Change to Notification");
                    tinyTypeText.setText("Overlay");
                    toSaveType = "Overlay";
                    toSaveTxt = "Change to Notification";
                    isNotif = false;
                } else {
                    tinyTogglerTxt.setText("Change to Overlay");
                    tinyTypeText.setText("Notification");
                    toSaveType = "Notification";
                    toSaveTxt = "Change to Overlay";
                    isNotif = true;
                }
                saveData();
            }
        });

        rewriteCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rewriteCache.setText("Re-writing cache...");
                try {
                    rewriteCache();
                    Toast.makeText(OverlayPlayer.this, "Finished Re-writing cache.", Toast.LENGTH_SHORT).show();
                    rewriteCache.setText("Re-write image cache");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });

        toAboutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aboutRoot.setVisibility(View.VISIBLE);
                prevStatus = statusbarText.getText().toString();
                statusbarText.setText("Tap on the blank area to close.");
            }
        });
        aboutRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aboutRoot.setVisibility(View.GONE);
                statusbarText.setText(prevStatus);
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
                miniPlayPause.setImageResource(R.drawable.pause);
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

        // DOWNLOADER CONTROLS
        downloaderHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(OverlayPlayer.this, DownloaderGuide.class));
            }
        });

        downloaderPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData cd = clipboardManager.getPrimaryClip();
                if(clipboardManager.hasPrimaryClip()){
                    Log.d("ClipboardManager: ", "cd: " + cd);
                    Log.d("ClipboardManager: ", "Has primary clip.");
                }
                if(cd != null){
                    Log.d("Clipboard cd: ", "m: " + cd);
                    Log.d("Clipboard Amt: ", "m: " + cd.getItemCount());
                    Log.d("Clipboard indx: ", "m: " + cd.getItemAt(0));
                    ClipData.Item item = cd.getItemAt(0);
                    downloaderURLText.setText(item.getText().toString());
                    songURL = item.getText().toString();
                } else {
                    Toast.makeText(OverlayPlayer.this, "Nothing to paste!", Toast.LENGTH_SHORT).show();
                    Log.d("FAIL Clipboard cd: ", "m: " + cd);
                }
            }
        });

        downloaderKB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloaderURLText.setCursorVisible(false);
                params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                window.updateViewLayout(windowView, params);
                Toast.makeText(OverlayPlayer.this, "Keyboard closed.", Toast.LENGTH_SHORT).show();
            }
        });

        downloaderURLText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //downloaderURLText.setCursorVisible(true);
                params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                window.updateViewLayout(windowView, params);
                return false;
            }
        });
        downloaderURLText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Log.d("URLTEXT", "GETTING FOCUS CHANGE...");
                if(!b){
                    Log.d("URLTEXT", "FOCUS IS CHANGED!");
                    params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    window.updateViewLayout(windowView, params);
                } else {
                    Log.d("URLTEXT", "FOCUS NOT CHANGED.");
                }
            }


        });
        downloaderURLText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH
                        || i == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                    Log.d("onEditorAction: ", "ACTION FINISHED.");
                    params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    window.updateViewLayout(windowView, params);

                    return true;
                }
                // Return true if you have consumed the action, else false.
                return false;
            }
        });

        downloaderSearch.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View view) {
                downloaderBuffer.setVisibility(View.VISIBLE);
                songURL = downloaderURLText.getText().toString();
                Log.d("Search: ", "url: " + songURL);
                if(Patterns.WEB_URL.matcher(songURL).matches() && songURL != null){
                    new YouTubeExtractor(OverlayPlayer.this){
                        @SuppressLint("SetTextI18n")
                        @Override
                        protected void onExtractionComplete(@Nullable SparseArray<YtFile> ytFiles, @Nullable VideoMeta videoMeta) {
                            if (ytFiles != null) {

                                int itag = ytFiles.keyAt(0);
                                YtFile ytFile = ytFiles.get(itag);

                                if (ytFile.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360) {
                                    Log.d("onExtractionComplete: ", "Title: " + videoMeta.getTitle());
                                    Log.d("onExtractionComplete: ", "Author: " + videoMeta.getAuthor());
                                    Log.d("onExtractionComplete: ", "Views: " + videoMeta.getViewCount());
                                    Log.d("onExtractionComplete: ", "Thumb: " + videoMeta.getThumbUrl());
                                }

                                String downloadUrl = ytFiles.get(itag).getUrl();
                                dlURL = downloadUrl;
                                dlNameStr = videoMeta.getTitle();

                                downloaderBuffer.setVisibility(View.GONE);
                                downloaderInfo.setVisibility(View.VISIBLE);

                                dlName.setText("Name: " + videoMeta.getTitle());
                                dlAuthor.setText("Author: " + videoMeta.getAuthor());
                                dlViews.setText("Views: " + String.valueOf(videoMeta.getViewCount()));
//                                Picasso.get().load(videoMeta.getThumbUrl()).error(R.drawable.song_icon).into(dlThumb, new Callback() {
//                                    @Override
//                                    public void onSuccess() {
//                                        Log.d("onSuccess: ", "LOADED DL PIC // PICASSO");
//                                    }
//
//                                    @Override
//                                    public void onError(Exception e) {
//                                        Log.d("onSuccess: ", "FAILED TO LOAD PIC.");
//
//                                    }
//                                });
                                Toast.makeText(OverlayPlayer.this, "Found " + videoMeta.getTitle() , Toast.LENGTH_SHORT).show();

                                Log.d("Download: ", "URL: " + downloadUrl);
                            } else {
                                Toast.makeText(OverlayPlayer.this, "The song could not be found!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }.extract(songURL);
                } else {
                    Toast.makeText(OverlayPlayer.this, "Invalid link!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        downloaderDL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dlURL != null){
                    Uri uri = Uri.parse(dlURL);
                    DownloadManager.Request request = new DownloadManager.Request(uri);
                    request.setTitle(dlNameStr);

                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, dlNameStr + ".mp3");

                    DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    manager.enqueue(request);

                    Toast.makeText(OverlayPlayer.this, "Downloading...", Toast.LENGTH_SHORT).show();
                    downloaderURLText.setText("");
                    dlName.setText("");
                    dlAuthor.setText("");
                    dlViews.setText("");
                    downloaderInfo.setVisibility(View.GONE);
                    Toast.makeText(OverlayPlayer.this, "Go to Songs -> Reload, find the song and enjoy.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OverlayPlayer.this, "Cannot download an empty url!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // ABOUT


        /**
         * WRITING CACHE
         * */
        try {
            writeCache();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

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
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media._ID
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
                String ogName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                long songId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));


                Uri songCover = Uri.parse("content://media/external/audio/albumart");
                Uri uriSongCover = ContentUris.withAppendedId(songCover, songId);
                String ogFileName = ogName.split(".mp3")[0];

                SongsConstructor musicModel = new SongsConstructor(path, title, artist, album, ogFileName, duration, uriSongCover);
                tempAudioList.add(musicModel); // this adds the "files" with metadata
                //Log.d("getAllSongs()", "path: " + path);
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
    public static void updateMiniPlayer() throws IOException {

        miniSongTitle.setText(InitialPlayer.getArrayList().get(InitialPlayer.getPos()).getTitle());
        miniSongArtist.setText(InitialPlayer.getArrayList().get(InitialPlayer.getPos()).getArtist());
        miniSongDuration.setText(Utils.getTimeFormatted(InitialPlayer.getArrayList().get(InitialPlayer.getPos()).getDuration()));
        miniExpandedMaxDur.setText(Utils.getTimeFormatted(InitialPlayer.getArrayList().get(InitialPlayer.getPos()).getDuration()));

        tinySongTitle.setText(InitialPlayer.getArrayList().get(InitialPlayer.getPos()).getTitle());

        miniPlayPause.setImageResource(R.drawable.pause);

//        Uri songArt = Utils.getAlbumArt(InitialPlayer.getPos());
//        if(songArt != null){
//            miniImage.setImageURI(songArt);
//            tinyArtwork.setImageURI(songArt);
//            miniPlayer.setBackgroundColor(Utils.getDominantColor(songArt, thisContext));
//            //tinyRoot.setBackgroundColor(getDominantColor(bitmap));
//            Log.d("onBindViewHolder()", "Loading Artwork.");
//        } else {
//            miniImage.setImageResource(R.drawable.song_image);
//            tinyArtwork.setImageResource(R.drawable.song_image);
//            miniPlayer.setBackgroundColor(Color.rgb(26, 26, 26));
//            Log.d("onBindViewHolder()", "Loading -Placeholder.");
//        }


        if (cacheDir != null) {
            File filePath = new File(cacheDir + "/" + songUriArrayList.get(InitialPlayer.getPos()).getOgName() + ".png");
            Picasso.get().load(filePath).error(R.drawable.song_image).into(miniImage);
            Picasso.get().load(filePath).error(R.drawable.song_image).into(tinyArtwork);
            Log.d("onBindViewHolder()", "Loading Artwork.");
        } else {
            miniImage.setImageResource(R.drawable.song_image);
            tinyArtwork.setImageResource(R.drawable.song_image);
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

//    private void createChannel(){
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            NotificationChannel notifChannel = new NotificationChannel(
//                    NotificationCreator.CHANNEL_ID,
//                    "MP3 Player",
//                    NotificationManager.IMPORTANCE_HIGH);
//            notifManager = getSystemService(NotificationManager.class);
//            if(notifManager != null){
//                notifManager.createNotificationChannel(notifChannel);
//            }
//        }
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //showToast(this, toastLayout, toastText, "Thank you for using this app!");
        saveData();
        isPlaying = false;
        if(windowView != null){
            window.removeView(windowView);
        }
        notifDestroy();
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

        editor.putString(toSaveTinyPlayerTypeString, toSaveType);
        editor.putString(toSaveTinyChangeString, toSaveTxt);

        Log.d("saveData: ", "SAVE TYPE: " + toSaveType);
        Log.d("saveData: ", "SAVE TXT: " + toSaveTxt);

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

        toLoadTinyPlayerType = sharedPreferences.getString(toSaveTinyPlayerTypeString, "Notification");
        toLoadTinyChangeTxt = sharedPreferences.getString(toSaveTinyChangeString, "Change to Overlay");

        Log.d("loadData: ", "LOAD TYPE: " + toLoadTinyPlayerType);
        Log.d("loadData: ", "LOAD TXT: " + toLoadTinyChangeTxt);
    }

    private void writeCache() throws FileNotFoundException {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "mp3Player_imgCache");
        cacheDir = String.valueOf(dir);
        if(!dir.exists()){
            dir.mkdir();
            Toast.makeText(thisContext, "Made cache directory. (Pictures)", Toast.LENGTH_SHORT).show();
            Log.d("writeCache: ", "DIR: " + dir);
            if(songUriArrayList != null){
                for(int i = 0; i < songUriArrayList.size(); i++) {
                    Bitmap bitmap = Utils.getAlbumArt(songUriArrayList.get(i).getPath());
                    if(bitmap != null){
                        //Log.d("GET_SONG+TESTUGQ9RQ3RR", "loc: " + songUriArrayList.get(i).getPath());
                        File file = new File(dir, songUriArrayList.get(i).getOgName() + ".png");
                        outputStream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        Log.d("writeCache", "WROTE " + file);
                    }
                }
            }
        }
    }

    private void rewriteCache() throws FileNotFoundException {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "mp3Player_imgCache");
        cacheDir = String.valueOf(dir);
        if(dir != null){
            for(int i = 0; i < songUriArrayList.size(); i++) {
                Bitmap bitmap = Utils.getAlbumArt(songUriArrayList.get(i).getPath());
                if(bitmap != null){
                    File file = new File(dir, songUriArrayList.get(i).getOgName() + ".png");
                    outputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    Log.d("rewriteCache", "WROTE " + file);
                }
            }
        }
    }

    // NOTIFICATIONS

    // ?
    // DO NOT USE UNTIL FUTURE UPDATES.
    // this code is either bugged or broken. un-codeblock at your risk.
    //
    //
    // this code initializes the notification
    // NotificationCreator.init(MainActivity.this, InitialPlayer.getArrayList(), InitialPlayer.getPos(), InitialPlayer.getPlayer().isPlaying());

    // vvv THIS IS ONLY FOR v1.3 OVERLAY TOGGLER.
    private void createChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notifChannel = new NotificationChannel(
                    NotificationCreator.CHANNEL_ID,
                    "MP3 Player",
                    NotificationManager.IMPORTANCE_HIGH);
            notifManager = getSystemService(NotificationManager.class);
            if(notifManager != null){
                notifManager.createNotificationChannel(notifChannel);
            }
        }
    }
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionName");
            switch(action){
                case NotificationCreator.TOGGLE:
                    Log.d("RECEIVED INTENT: ", "TOGGLE OVERLAY");
                    windowLayout.setVisibility(View.VISIBLE);
                    Toast.makeText(context, "Overlay is visible!", Toast.LENGTH_SHORT).show();
            }
        }
    };
//    //
//    BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getExtras().getString("actionName");
//            //Log.d("BEFORE onReceive: CHECKING PLAYER POSITION", "pos: " + InitialPlayer.getPos());
//            Log.d("onReceive: ", "Action: " + action);
//            switch (action){
//                case NotificationCreator.SEEK_PREV:
//                    Log.d("NOTIF RECEIVE", "SEEK PREV");
//                    onSongPrev();
//                case NotificationCreator.PLAY:
//                    Log.d("NOTIF RECEIVE", "PLAY");
//                    ///onSongPlay();
//                case NotificationCreator.SEEK_NEXT:
//                    Log.d("NOTIF RECEIVE", "SEEK NEXT");
//                    onSongNext();
//                case NotificationCreator.CANCEL:
//                    Log.d("NOTIF RECEIVE", "CANCEL");
//                    onClose();
//
//            }
//            //Log.d("AFTER onReceive: CHECKING PLAYER POSITION", "pos: " + InitialPlayer.getPos());
//
//        }
//    };
//
//    @Override
//    public void onSongPrev() {
//        Log.d("NOTIFICATION ACTION", "onSongPrev");
////        if(InitialPlayer.getPlayer() != null){
////            try {
////                InitialPlayer.seekPrevious(OverlayPlayer.this);
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
////        }
//    }
//
//    @Override
//    public void onSongPlay() {
//        Log.d("NOTIFICATION ACTION", "onSongPlay");
////        if(InitialPlayer.getPlayer() != null){
////            if(isPlaying){
////                miniPlayPause.setImageResource(R.drawable.play);
////                tinyPlayPause.setImageResource(R.drawable.play);
////                InitialPlayer.pause();
////                isPlaying = false;
////            } else {
////                miniPlayPause.setImageResource(R.drawable.pause);
////                tinyPlayPause.setImageResource(R.drawable.pause);
////                try {
////                    InitialPlayer.resume(OverlayPlayer.this);
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////                isPlaying = true;
////            }
////        }
//    }
//
//    @Override
//    public void onSongNext() {
//        Log.d("NOTIFICATION ACTION", "onSongNext");
////        if(InitialPlayer.getPlayer() != null){
////            try {
////                InitialPlayer.seekNext(OverlayPlayer.this);
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
////        }
//    }
//
//    @Override
//    public void onMaximize() {
//        notifDestroy();
//        //windowLayout.setVisibility(View.VISIBLE);
//    }
//
//    @Override
//    public void onClose() {
//        //onDestroy();
//    }
//
//    //
    private void notifDestroy(){
        notifManager.cancelAll();
        unregisterReceiver(receiver);
    }

 }