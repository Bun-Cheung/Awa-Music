package com.awareness.music;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.awareness.music.livedata.MusicPositionLiveData;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String IS_FIRST_USE_KEY = "key_isFirstUse";
    private final String TAG = getClass().getSimpleName();
    private ImageView mIvCover;
    private TextView mTvMusicTitle;
    private TextView mTvArtist;
    private TextView mTvDuration;
    private TextView mTvMusicCurrentProgress;
    private ImageButton mBtnPlay;
    private SeekBar mSeekbar;
    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        Utils.createNotificationChannel(this);
        if (getSPData(IS_FIRST_USE_KEY)) {
            showFirstUseDialog();
            setSPData(IS_FIRST_USE_KEY, false);
        }
        String musicTag = getIntent().getStringExtra(Constant.MUSIC_TAG);
        Bundle bundle = null;
        if (musicTag != null) {
            bundle = new Bundle();
            bundle.putString(Constant.MUSIC_TAG, musicTag);
        }
        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MusicService.class), mConnectionCallback, bundle);
        mMediaBrowser.connect();
        MusicPositionLiveData.getInstance().getCurrentPosition().observe(this, integer -> {
            mSeekbar.setProgress(integer);
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaController != null) {
            mMediaController.getTransportControls().stop();
        }
        if (mMediaBrowser != null) {
            mMediaBrowser.disconnect();
        }
    }

    private void initView() {
        View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decorView.setSystemUiVisibility(option);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(getColor(R.color.mainBackgroundColor));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mBtnPlay = findViewById(R.id.btn_play);
        mBtnPlay.setOnClickListener(this);
        findViewById(R.id.btn_setting).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
        findViewById(R.id.btn_previous).setOnClickListener(this);
        mIvCover = findViewById(R.id.cover);
        mTvMusicTitle = findViewById(R.id.music_title);
        mTvArtist = findViewById(R.id.music_artist);
        mTvDuration = findViewById(R.id.music_duration);
        mTvMusicCurrentProgress = findViewById(R.id.music_current_progress);
        mSeekbar = findViewById(R.id.music_seek_bar);
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mMediaController.getTransportControls().seekTo(progress);
                }
                mTvMusicCurrentProgress.setText(Utils.formatMS(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private MediaBrowserCompat.ConnectionCallback mConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            super.onConnected();
            Log.i(TAG, "connect music service success");
            MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();
            try {
                mMediaController = new MediaControllerCompat(getBaseContext(), token);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mMediaController.registerCallback(mControllerCallback);
            startService(new Intent(getBaseContext(), MusicService.class));
        }
    };

    private MediaControllerCompat.Callback mControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@Nullable PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (state == null) {
                return;
            }
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_NONE:
                case PlaybackStateCompat.STATE_PAUSED:
                    mBtnPlay.setImageResource(R.drawable.ic_play);
                    mSeekbar.setProgress((int) state.getPosition());
                    break;
                case PlaybackStateCompat.STATE_PLAYING:
                    mBtnPlay.setImageResource(R.drawable.ic_pause);
                    mSeekbar.setProgress((int) state.getPosition());
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onMetadataChanged(@Nullable MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            if (metadata == null) {
                return;
            }
            mIvCover.setImageBitmap(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART));
            mTvMusicTitle.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE));
            mTvArtist.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            long duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
            mTvDuration.setText(Utils.formatMS(duration));
            mTvMusicCurrentProgress.setText(Utils.formatMS(0));
            mSeekbar.setMax((int) duration);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_setting:
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_play:
                if (mMediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                    mMediaController.getTransportControls().pause();
                } else {
                    mMediaController.getTransportControls().play();
                }
                break;
            case R.id.btn_next:
                mMediaController.getTransportControls().skipToNext();
                break;
            case R.id.btn_previous:
                mMediaController.getTransportControls().skipToPrevious();
                break;
        }
    }

    private void showFirstUseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Initial Setting");
        builder.setMessage("Go to Setting page to setup music recommendation config");
        builder.setPositiveButton("setting", (dialog, which) -> {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            dialog.dismiss();
        });
        builder.setNegativeButton("cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean getSPData(String key) {
        SharedPreferences sp = getSharedPreferences(Constant.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(key, true);
    }

    private void setSPData(String key, boolean value) {
        SharedPreferences sp = getSharedPreferences(Constant.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
}
