package com.awareness.music.entity;

import android.app.PendingIntent;

import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier;

public class BarrierParamEntity {
    private String barrierLabel;
    private AwarenessBarrier barrier;
    private PendingIntent pendingIntent;

    public BarrierParamEntity(String barrierLabel, AwarenessBarrier barrier, PendingIntent pendingIntent) {
        this.barrierLabel = barrierLabel;
        this.barrier = barrier;
        this.pendingIntent = pendingIntent;
    }

    public String getBarrierLabel() {
        return barrierLabel;
    }

    public AwarenessBarrier getBarrier() {
        return barrier;
    }

    public PendingIntent getPendingIntent() {
        return pendingIntent;
    }
}
