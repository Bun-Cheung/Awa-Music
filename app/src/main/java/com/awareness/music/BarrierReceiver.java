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
        String title, content;
        Intent contentIntent;
        PendingIntent pendingIntent;
        switch (barrierLabel) {
            case Constant.HEADSET_BLUETOOTH_BARRIER_LABEL:
                contentIntent = new Intent(context, MainActivity.class);
                title = "Awareness Kit感知到耳机或车载蓝牙链接";
                content = "点击打开Awa-Music";
                pendingIntent = PendingIntent.getActivity(context, 2,
                        contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                Utils.sendAwarenessNotification(context, title, content, pendingIntent);
                break;
            case Constant.MORNING_LABEL:
                contentIntent = new Intent(context, MainActivity.class);
                title = "Awareness Kit感知现在是早上";
                content = "点击打开Awa-Music";
                pendingIntent = PendingIntent.getActivity(context, 2,
                        contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                Utils.sendAwarenessNotification(context, title, content, pendingIntent);
                break;
            case Constant.AFTERNOON_LABEL:
                if (Utils.isMusicServiceRunning(context)) {
                    contentIntent = new Intent(context, MusicService.class);
                    contentIntent.putExtra("MusicTag", "afternoon");
                    pendingIntent = PendingIntent.getService(context, 2, contentIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                } else {
                    contentIntent = new Intent(context, MainActivity.class);
                    pendingIntent = PendingIntent.getActivity(context, 2,
                            contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                }
                title = "Awareness Kit感知现在是下午";
                content = "点击打开Awa-Music";
                Utils.sendAwarenessNotification(context, title, content, pendingIntent);
                break;
            default:
                break;
        }
    }
}
