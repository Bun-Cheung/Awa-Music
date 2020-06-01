package com.awareness.music;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media.session.MediaButtonReceiver;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
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


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
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
    private boolean mIsPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        Utils.createNotificationChannel(this);
        showFirstUseDialog();
        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MusicService.class), mConnectionCallback, null);
        mMediaBrowser.connect();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaController.getTransportControls().stop();
        mMediaBrowser.disconnect();
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
                mMediaController.getTransportControls().seekTo(progress);
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
                    mIsPlaying = false;
                    break;
                case PlaybackStateCompat.STATE_PLAYING:
                    mBtnPlay.setImageResource(R.drawable.ic_pause);
                    mSeekbar.setProgress((int) state.getPosition());
                    mIsPlaying = true;
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
                if (mIsPlaying) {
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
        builder.setTitle("首次设置");
        builder.setMessage("首次使用，建议设置个性化音乐场景");
        builder.setPositiveButton("设置", (dialog, which) -> {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            dialog.dismiss();
        });
        builder.setNegativeButton("取消", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
