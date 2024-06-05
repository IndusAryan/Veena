package com.aryan.veena.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import androidx.annotation.NonNull;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;
import com.aryan.veena.ui.activities.MainActivity;
import com.aryan.veena.utils.NowPlayingNotificatio;

public class PlayService extends MediaSessionService {

        public MediaSession mediaSession;
        private ExoPlayer player;

        @Override
        public void onCreate() {
            super.onCreate();
            player = new ExoPlayer.Builder(this).build();
            mediaSession = new MediaSession.Builder(this, player)
                    .setSessionActivity(getPendingIntent())
                    .build();
        }

        private PendingIntent getPendingIntent() {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            return PendingIntent.getActivity(
                    this,
                    1,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );
        }

        // Remember to release the player and media session in onDestroy
        @Override
        public void onDestroy() {
            super.onDestroy();
            if (mediaSession != null) {
                player.release();
                mediaSession.release();
            }
            mediaSession = null;
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NowPlayingNotificatio.NOTIFICATION_ID);
        }

        @Override
        public void onTaskRemoved(Intent rootIntent) {
            stopSelf();
            // Cancel the notification
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NowPlayingNotificatio.NOTIFICATION_ID);
        }

        @Override
        public MediaSession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo) {
            return mediaSession;
        }

        public static class PlaybackServiceBinder extends Binder {
            private final PlayService service;

            public PlaybackServiceBinder(PlayService service) {
                this.service = service;
            }

            public PlayService getService() {
                return service;
            }

            public MediaSession getMediaSession() {
                return service.mediaSession;
            }
        }
}
