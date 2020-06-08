package com.awareness.music;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.huawei.hms.kit.awareness.barrier.BarrierStatus;

public class BarrierReceiver extends BroadcastReceiver {
    private final String TAG = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        BarrierStatus barrierStatus = BarrierStatus.extract(intent);
        String barrierLabel = barrierStatus.getBarrierLabel();
        int status = barrierStatus.getPresentStatus();
        if (status == BarrierStatus.FALSE || status == BarrierStatus.UNKNOWN) {
            Log.i(TAG, barrierLabel + "barrier status is false or unknown");
            return;
        }
        Log.i(TAG, "barrier status is true");
        String title, content;
        PendingIntent pendingIntent;
        switch (barrierLabel) {
            case Constant.HEADSET_BLUETOOTH_BARRIER_LABEL:
                Intent contentIntent = new Intent(context, MainActivity.class);
                title = "Headset or bluetooth car stereo is connected.";
                content = "click to open Awa-Music";
                pendingIntent = PendingIntent.getActivity(context, 2,
                        contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                Utils.sendAwarenessNotification(context, title, content, pendingIntent);
                break;
            case Constant.MORNING_LABEL:
                title = "It's morning now";
                content = "Enjoy the music in the morning";
                pendingIntent = buildPendingIntent(context, Constant.MORNING_LABEL);
                Utils.sendAwarenessNotification(context, title, content, pendingIntent);
                break;
            case Constant.AFTERNOON_LABEL:
                title = "It's afternoon now";
                content = "Enjoy the music in the afternoon";
                pendingIntent = buildPendingIntent(context, Constant.AFTERNOON_LABEL);
                Utils.sendAwarenessNotification(context, title, content, pendingIntent);
                break;
            case Constant.NIGHT_LABEL:
                title = "It's night";
                content = "Enjoy the music at night";
                pendingIntent = buildPendingIntent(context, Constant.NIGHT_LABEL);
                Utils.sendAwarenessNotification(context, title, content, pendingIntent);
                break;
            case Constant.RUNNING_LABEL:
                title = "It's running";
                content = "Enjoy the music for running";
                pendingIntent = buildPendingIntent(context, Constant.RUNNING_LABEL);
                Utils.sendAwarenessNotification(context, title, content, pendingIntent);
                break;
            case Constant.IN_VEHICLE_LABEL:
                title = "It's in vehicle";
                content = "Enjoy the music in vehicle";
                pendingIntent = buildPendingIntent(context, Constant.IN_VEHICLE_LABEL);
                Utils.sendAwarenessNotification(context, title, content, pendingIntent);
                break;
            case Constant.ON_BICYCLE_LABEL:
                title = "It's on bicycle";
                content = "Enjoy the music for cycling";
                pendingIntent = buildPendingIntent(context, Constant.ON_BICYCLE_LABEL);
                Utils.sendAwarenessNotification(context, title, content, pendingIntent);
                break;
            case Constant.WALKING_LABEL:
                title = "It's walking";
                content = "Enjoy the music for walking";
                pendingIntent = buildPendingIntent(context, Constant.WALKING_LABEL);
                Utils.sendAwarenessNotification(context, title, content, pendingIntent);
                break;
            default:
                break;
        }
    }


    private PendingIntent buildPendingIntent(Context context, String label) {
        Intent contentIntent;
        if (Utils.isMusicServiceRunning(context)) {
            contentIntent = new Intent(context, MusicService.class);
            contentIntent.putExtra(Constant.MUSIC_TAG, label);
            return PendingIntent.getService(context, 2, contentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            contentIntent = new Intent(context, MainActivity.class);
            contentIntent.putExtra(Constant.MUSIC_TAG, label);
            return PendingIntent.getActivity(context, 2, contentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }
}
