package com.panniku.mp3player.Services;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationManagerCompat;

public class NotificationViewBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent("VIEW").putExtra("viewActionName", intent.getAction()));

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
    }
}
