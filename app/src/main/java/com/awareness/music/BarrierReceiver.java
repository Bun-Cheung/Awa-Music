package com.awareness.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huawei.hms.kit.awareness.barrier.BarrierStatus;

public class BarrierReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        BarrierStatus barrierStatus = BarrierStatus.extract(intent);
        
    }
}
