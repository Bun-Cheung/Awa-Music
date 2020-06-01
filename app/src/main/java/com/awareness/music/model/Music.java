package com.awareness.music.model;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

public class Music {
    private Uri musicUri;
    private String title;
    private String artist;
    private long duration;
    private int coverImage;
    private String tag;

    public Music(Context context, Uri musicUri, int imageId, String tag) {
        if (musicUri != null) {
            this.musicUri = musicUri;
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(context, musicUri);
            title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            duration = Long.parseLong(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            coverImage = imageId;
            this.tag = tag;
        }

    }

    public Uri getMusicUri() {
        return musicUri;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getTag() {
        return tag;
    }

    public int getCoverImage() {
        return coverImage;
    }

    public long getDuration() {
        return duration;
    }
}
