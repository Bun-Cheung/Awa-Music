package com.awareness.music;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huawei.hms.kit.awareness.barrier.BarrierStatus;

public class BarrierReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        BarrierStatus barrierStatus = BarrierStatus.extract(intent);
        String barrierLabel = barrierStatus.getBarrierLabel();
        int status = barrierStatus.getPresentStatus();
        switch (barrierLabel) {
            case Constant.HEADSET_BLUETOOTH_BARRIER_LABEL:
                if (status == BarrierStatus.TRUE) {
                    Intent contentIntent = new Intent(context, MainActivity.class);
                    String title = "Awareness Kit感知到耳机或车载蓝牙链接";
                    String content = "点击打开Awa-Music";
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 2,
                            contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    Utils.sendAwarenessNotification(context, title, content, pendingIntent);
                }
                break;
            default:
                break;
        }
    }
}
