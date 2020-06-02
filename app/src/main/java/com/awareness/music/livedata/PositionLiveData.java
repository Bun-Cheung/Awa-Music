package com.awareness.music.livedata;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;

public class PositionLiveData {

    private MutableLiveData<Integer> currentPosition = new MutableLiveData<>();
    private Handler handler = new Handler(Looper.getMainLooper());

    private PositionLiveData() {
        currentPosition.postValue(0);
    }

    public static PositionLiveData getInstance() {
        return SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        private static final PositionLiveData sInstance = new PositionLiveData();
    }

    public void checkPosition(MediaPlayer mediaPlayer) {
        handler.postDelayed(() -> {
            int position = mediaPlayer.getCurrentPosition();
            if (currentPosition.getValue() != null && currentPosition.getValue() != position) {
                currentPosition.postValue(position);
            }
            checkPosition(mediaPlayer);
        }, 1000);
    }

    public MutableLiveData<Integer> getCurrentPosition() {
        return currentPosition;
    }
}
