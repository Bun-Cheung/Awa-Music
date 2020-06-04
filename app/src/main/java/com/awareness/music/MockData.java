package com.awareness.music;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.awareness.music.entity.BarrierParamEntity;
import com.awareness.music.entity.Music;
import com.huawei.hms.kit.awareness.barrier.BehaviorBarrier;
import com.huawei.hms.kit.awareness.barrier.TimeBarrier;

import java.util.ArrayList;
import java.util.List;

class MockData {
    private final static List<BarrierParamEntity> timeBarrierList = new ArrayList<>();
    private final static List<BarrierParamEntity> behaviorBarrierList = new ArrayList<>();
    private final static List<Music> musicList = new ArrayList<>();

    static List<BarrierParamEntity> getTimeBarrierList(Context context) {
        if (!timeBarrierList.isEmpty()) {
            return timeBarrierList;
        }
        Intent intent = new Intent(context, BarrierReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        BarrierParamEntity morningBarrierEntity = new BarrierParamEntity(Constant.MORNING_BARRIER_LABEL,
                TimeBarrier.inTimeCategory(TimeBarrier.TIME_CATEGORY_MORNING), pendingIntent);
        BarrierParamEntity afternoonBarrierEntity = new BarrierParamEntity(Constant.AFTERNOON_BARRIER_LABEL,
                TimeBarrier.inTimeCategory(TimeBarrier.TIME_CATEGORY_AFTERNOON), pendingIntent);
        BarrierParamEntity nightBarrierEntity = new BarrierParamEntity(Constant.NIGHT_BARRIER_LABEL,
                TimeBarrier.inTimeCategory(TimeBarrier.TIME_CATEGORY_NIGHT), pendingIntent);

        timeBarrierList.add(morningBarrierEntity);
        timeBarrierList.add(afternoonBarrierEntity);
        timeBarrierList.add(nightBarrierEntity);
        return timeBarrierList;
    }

    static List<BarrierParamEntity> getBehaviorBarrierList(Context context) {
        if (!behaviorBarrierList.isEmpty()) {
            return behaviorBarrierList;
        }
        Intent intent = new Intent(context, BarrierReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        BarrierParamEntity runningBarrierEntity = new BarrierParamEntity(Constant.RUNNING_BARRIER_LABEL,
                BehaviorBarrier.keeping(BehaviorBarrier.BEHAVIOR_RUNNING), pendingIntent);
        BarrierParamEntity onBicycleBarrierEntity = new BarrierParamEntity(Constant.ON_BICYCLE_BARRIER_LABEL,
                BehaviorBarrier.keeping(BehaviorBarrier.BEHAVIOR_ON_BICYCLE), pendingIntent);
        BarrierParamEntity inVehicleBarrierEntity = new BarrierParamEntity(Constant.IN_VEHICLE_BARRIER_LABEL,
                BehaviorBarrier.keeping(BehaviorBarrier.BEHAVIOR_IN_VEHICLE), pendingIntent);
        BarrierParamEntity walkingBarrierEntity = new BarrierParamEntity(Constant.WALKING_BARRIER_LABEL,
                BehaviorBarrier.keeping(BehaviorBarrier.BEHAVIOR_WALKING), pendingIntent);

        behaviorBarrierList.add(runningBarrierEntity);
        behaviorBarrierList.add(onBicycleBarrierEntity);
        behaviorBarrierList.add(inVehicleBarrierEntity);
        behaviorBarrierList.add(walkingBarrierEntity);

        return behaviorBarrierList;
    }

    static List<Music> getMusicList(Context context) {
        if (!musicList.isEmpty()) {
            return musicList;
        }
        Uri uri1 = resIdToUri(context, R.raw.mornings);
        Music music1 = new Music(context, uri1, R.drawable.cover_morning, "morning");
        Uri uri2 = resIdToUri(context, R.raw.autumn_sunset);
        Music music2 = new Music(context, uri2, R.drawable.cover_afternoon, "afternoon");
        musicList.add(music1);
        musicList.add(music2);
        return musicList;
    }

    private static Uri resIdToUri(Context context, int resId) {
        return Uri.parse("android.resource://" + context.getPackageName() + "/" + resId);
    }
}
