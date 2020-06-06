package com.awareness.music;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.awareness.music.entity.Music;
import com.awareness.music.livedata.MusicPositionLiveData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends MediaBrowserServiceCompat implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private final String TAG = getClass().getSimpleName();
    private MediaSessionCompat mSession;
    private PlaybackStateCompat mPlaybackStateCompat;
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private List<Music> mPlayList = new ArrayList<>();
    private int mPlayListLength;
    private int mCurrentIndexOfList = 0;
    private boolean mIsFirstStart = true;
    private boolean mIsFirstPreparedMusic = true;

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayList = MockData.getMusicList(this);
        mPlayListLength = mPlayList.size();
        mPlaybackStateCompat = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f)
                .build();
        mSession = new MediaSessionCompat(this, "MusicService");
        mSession.setCallback(mSessionCallback);
        setSessionToken(mSession.getSessionToken());
        mSession.setPlaybackState(mPlaybackStateCompat);
        mSession.setActive(true);
        Intent sessionIntent = new Intent(this, MainActivity.class);
        mSession.setSessionActivity(PendingIntent.getActivity(this, 0, sessionIntent, 0));
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mIsFirstStart && mPlayListLength > 0) {
            setMetadata(mPlayList.get(0));
            mIsFirstStart = false;
            updateNotification();
            prepareMediaPlayer(mPlayList.get(mCurrentIndexOfList));
            MusicPositionLiveData.getInstance().checkPosition(mMediaPlayer);
        } else {
            String tag = intent.getStringExtra("MusicTag");
            if (tag != null) {
                for (int i = 0; i < mPlayListLength; i++) {
                    if (mPlayList.get(i).getTag().equals(tag)) {
                        mSessionCallback.onSkipToQueueItem(i);
                        break;
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MusicPositionLiveData.getInstance().removeCallback();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("Awa-Music", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }


    private MediaSessionCompat.Callback mSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            super.onPlay();
            mMediaPlayer.start();
            mPlaybackStateCompat = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, mMediaPlayer.getCurrentPosition(),
                            1.0f).build();
            mSession.setPlaybackState(mPlaybackStateCompat);
            updateNotification();
        }

        @Override
        public void onPause() {
            super.onPause();
            if (mPlaybackStateCompat.getState() == PlaybackStateCompat.STATE_PLAYING) {
                mMediaPlayer.pause();
                mPlaybackStateCompat = new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PAUSED,
                        mMediaPlayer.getCurrentPosition(), 1.0f).build();
                mSession.setPlaybackState(mPlaybackStateCompat);
                updateNotification();
            }
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            if (mPlayListLength == 0) {
                Log.e(TAG, "no music in playlist");
                return;
            }
            if (mCurrentIndexOfList < mPlayListLength - 1) {
                ++mCurrentIndexOfList;
            } else {
                mCurrentIndexOfList = 0;
            }
            prepareMediaPlayer(mPlayList.get(mCurrentIndexOfList));
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            if (mPlayListLength == 0) {
                Log.e(TAG, "no music in playlist");
                return;
            }
            if (mCurrentIndexOfList == 0) {
                mCurrentIndexOfList = mPlayListLength - 1;
            } else {
                --mCurrentIndexOfList;
            }
            prepareMediaPlayer(mPlayList.get(mCurrentIndexOfList));
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            mMediaPlayer.seekTo((int) pos);
            mPlaybackStateCompat = new PlaybackStateCompat.Builder()
                    .setState(mPlaybackStateCompat.getState(), pos, 1.0f).build();
            mSession.setPlaybackState(mPlaybackStateCompat);
        }

        @Override
        public void onStop() {
            super.onStop();
            stopSelf();
        }

        @Override
        public void onSkipToQueueItem(long id) {
            super.onSkipToQueueItem(id);
            mCurrentIndexOfList = (int) id;
            prepareMediaPlayer(mPlayList.get(mCurrentIndexOfList));
        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            if (mediaButtonEvent.getAction() != null) {
                Log.i(TAG, "receive action" + mediaButtonEvent.getAction());
                KeyEvent keyEvent = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (keyEvent != null) {
                    int keyCode = keyEvent.getKeyCode();
                    if (keyCode == PlaybackStateCompat.toKeyCode(PlaybackStateCompat.ACTION_PLAY)) {
                        mSessionCallback.onPlay();
                    } else if (keyCode == PlaybackStateCompat.toKeyCode(PlaybackStateCompat.ACTION_SKIP_TO_NEXT)) {
                        mSessionCallback.onSkipToNext();
                    } else if (keyCode == PlaybackStateCompat.toKeyCode(PlaybackStateCompat.ACTION_PAUSE)) {
                        mSessionCallback.onPause();
                    } else if (keyCode == PlaybackStateCompat.toKeyCode(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)) {
                        mSessionCallback.onSkipToPrevious();
                    }
                }
            }
            return super.onMediaButtonEvent(mediaButtonEvent);
        }
    };

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mIsFirstPreparedMusic) {
            mIsFirstPreparedMusic = false;
            return;
        }
        mMediaPlayer.start();
        mPlaybackStateCompat = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f).build();
        mSession.setPlaybackState(mPlaybackStateCompat);
        updateNotification();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mSessionCallback.onSkipToNext();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return true;
    }


    private void prepareMediaPlayer(Music music) {
        mMediaPlayer.reset();
        Uri musicUri = music.getMusicUri();
        if (musicUri == null) {
            return;
        }
        try {
            mMediaPlayer.setDataSource(this, musicUri);
            setMetadata(music);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, "prepare music failed");
            e.printStackTrace();
        }
    }

    private void updateNotification() {
        Notification mediaNotification = Utils.buildMusicNotification(this, mSession);
        startForeground(1, mediaNotification);
    }

    private void setMetadata(Music music) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), music.getCoverImage());
        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, music.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, music.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, music.getArtist())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, music.getDuration())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                .build();
        mSession.setMetadata(metadata);
    }
}
