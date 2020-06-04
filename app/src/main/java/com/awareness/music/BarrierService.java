package com.awareness.music;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import com.huawei.hms.kit.awareness.barrier.BarrierStatus;

public class BarrierService extends IntentService {

    private final String TAG = getClass().getSimpleName();

    public BarrierService() {
        super("BarrierService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager == null) {
                return;
            }
            Notification notification = new Notification.Builder(this, Constant.SERVICE_CHANNEL).build();
            startForeground(2, notification);
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            Log.e(TAG, "intent is null");
            return;
        }
        BarrierStatus barrierStatus = BarrierStatus.extract(intent);
        String barrierLabel = barrierStatus.getBarrierLabel();
        int status = barrierStatus.getPresentStatus();
        switch (barrierLabel) {
            case Constant.HEADSET_BLUETOOTH_BARRIER_LABEL:
                if (status == BarrierStatus.TRUE) {
                    Intent contentIntent = new Intent(this, MainActivity.class);
                    if (intent.getBooleanExtra(Constant.IS_OPEN_ACTIVITY_FLAG, false)) {
                        Log.i(TAG, "try to start activity");
                        contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(contentIntent);
                    } else {
                        String title = "Awareness Kit感知到耳机链接";
                        String content = "点击打开Awa-Music";
                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2,
                                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        Utils.sendAwarenessNotification(this, title, content, pendingIntent);
                    }
                }
                break;
        }
    }
}
