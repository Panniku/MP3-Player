package com.panniku.mp3player.Overlay;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.panniku.mp3player.InitialPlayer;
import com.panniku.mp3player.R;

public class DownloaderGuide extends Service {

    private static RelativeLayout layout;
    private static View view;
    private static WindowManager wm;
    private static Button button;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        int LAYOUT_FLAG_TYPE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG_TYPE = WindowManager.LayoutParams.TYPE_PHONE;
        }

        view = LayoutInflater.from(this).inflate(R.layout.downloader_guide, null);
        WindowManager.LayoutParams toastParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Log.d("toastWindow", "x: " + wm.getDefaultDisplay().getWidth());
        Log.d("toastWindow", "y: " + wm.getDefaultDisplay().getHeight());

        wm.addView(view, toastParams);

        view.findViewById(R.id.dlClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSelf();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(view != null){
            wm.removeView(view);
        }
    }
}
