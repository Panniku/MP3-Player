package com.panniku.mp3player.Overlay;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.panniku.mp3player.R;

public class WindowToast extends Service {

    private static RelativeLayout toastLayout;
    private static View toastView;
    private static WindowManager toastWindow;
    private static TextView toastText;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //init();
    }

//    public void init(){
//        int LAYOUT_FLAG_TYPE;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            LAYOUT_FLAG_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//        } else {
//            LAYOUT_FLAG_TYPE = WindowManager.LayoutParams.TYPE_PHONE;
//        }
//
//        toastView = LayoutInflater.from(this).inflate(R.layout.dialog, null);
//        WindowManager.LayoutParams toastParams = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                LAYOUT_FLAG_TYPE,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                PixelFormat.TRANSLUCENT
//        );
//
//        toastWindow = (WindowManager) getSystemService(WINDOW_SERVICE);
//        Log.d("toastWindow", "dis x: " + toastWindow.getDefaultDisplay().getWidth());
//        Log.d("toastWindow", "dis y: " + toastWindow.getDefaultDisplay().getHeight());
//        //toastParams.alpha = 0;
//        toastParams.y = (toastWindow.getDefaultDisplay().getHeight() / 3) + 150;
//        //toastWindow.addView(toastView, toastParams);
//
////        toastLayout = toastView.findViewById(R.id.windowToastRoot);
////        toastText = toastView.findViewById(R.id.windowToastText);
//
//        //toastLayout.setVisibility(View.GONE);
//    }
//
//    public static void showToast(Context context, String text){
//        Log.d("showToast()", "Displaying toast.");
//        toastText.setText(text);
//        toastView.setVisibility(View.VISIBLE);
//        Animation fadein = AnimationUtils.loadAnimation(context, R.anim.fade_in);
//        toastView.setAnimation(fadein);
//        Log.d("animtaton", "dur: " + fadein.getDuration());
//    }
}
