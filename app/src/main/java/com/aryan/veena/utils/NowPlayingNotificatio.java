package com.aryan.veena.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.aryan.veena.R;

public class NowPlayingNotificatio {
    public static final String CHANNEL_ID = "media_playback_channel";
    public static final Integer NOTIFICATION_ID = 1;

    public static void createNotificationChannel(Context Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Media_Playback",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
             channel.setDescription("Channel for media playback");
            NotificationManager manager = (NotificationManager) Context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(channel);
        }
    }

    public static Notification createMediaNotification(
            Context Context,
            String songTitle,
            Bitmap thumbnail
            ) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(Context, CHANNEL_ID);
        builder.setContentTitle(songTitle)
                .setLargeIcon(thumbnail)
                .setColorized(true)
                .setColor(Color.WHITE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        return builder.build();
    }

    public static void showNotification(Context context, Notification notification) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notification);
    }
}
