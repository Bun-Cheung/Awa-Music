package com.awareness.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

public class Utils {
    private static final String FOREGROUND_MUSIC_CHANNEL = "Music control channel";
    private static final String AWARENESS_CHANNEL = "Awareness channel";
    private static final String SHARED_PREFERENCE_NAME = "AwaMusic SharedPreference";

    public static String formatMS(long ms) {
        long oneHourMS = 60 * 60 * 1000;
        long oneMinMS = 60 * 1000;
        long hour = ms / oneHourMS;
        long minute = (ms - hour * oneHourMS) / oneMinMS;
        long second = (ms - hour * oneHourMS - minute * oneMinMS) / 1000;
        String result = "";
        if (hour != 0) {
            result += hour + ":";
        }
        if (minute >= 10) {
            result += minute + ":";
        } else {
            result += "0" + minute + ":";
        }
        if (second >= 10) {
            result += second;
        } else {
            result += "0" + second;
        }

        return result;
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) {
                return;
            }
            CharSequence musicChannelName = "Music Controller";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel musicChannel = new NotificationChannel(FOREGROUND_MUSIC_CHANNEL,
                    musicChannelName, importance);
            musicChannel.setDescription("Control the Music player in Notification bar");
            manager.createNotificationChannel(musicChannel);

            CharSequence awaChannelName = "Awareness Notification";
            NotificationChannel awarenessChannel = new NotificationChannel(AWARENESS_CHANNEL,
                    awaChannelName, importance);
            awarenessChannel.setDescription("Notification base on the capability of HMS Awareness Kit");
            manager.createNotificationChannel(awarenessChannel);
        }
    }

    public static Notification buildAwarenessNotification(Context context, String title,
                                                          String content, PendingIntent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AWARENESS_CHANNEL);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(intent)
                .setAutoCancel(true);
        return builder.build();
    }

    public static Notification buildMusicNotification(Context context, MediaSessionCompat session) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FOREGROUND_MUSIC_CHANNEL);
        MediaMetadataCompat mediaMetadata = session.getController().getMetadata();
        if (mediaMetadata == null) {
            return builder.build();
        }
        int state = session.getController().getPlaybackState().getState();
        NotificationCompat.Action playOrPauseAction = state == PlaybackStateCompat.STATE_PLAYING ?
                new NotificationCompat.Action(R.drawable.icon_pause_notification, "pause",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PAUSE)) :
                new NotificationCompat.Action(R.drawable.icon_play_notification, "play",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY));

        MediaDescriptionCompat description = mediaMetadata.getDescription();
        builder.setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setLargeIcon(description.getIconBitmap())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setColor(context.getColor(R.color.mainBackgroundColor))
                .setContentIntent(session.getController().getSessionActivity())
                .addAction(R.drawable.icon_previous_notification, "pre",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))
                .addAction(playOrPauseAction)
                .addAction(R.drawable.icon_next_notification, "next",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle().
                setMediaSession(session.getSessionToken()).setShowActionsInCompactView(0, 1, 2));
        return builder.build();
    }

    public static boolean getSPData(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(key, true);
    }

    public static void setSPData(Context context, String key, boolean value) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
}
