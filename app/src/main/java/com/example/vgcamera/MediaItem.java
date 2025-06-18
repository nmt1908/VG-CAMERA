package com.example.vgcamera;

public class MediaItem {
    public String uri;
    public boolean isVideo;
    public boolean isSelected;
    public long duration; // thời lượng video (ms), ảnh thì để = 0

    public MediaItem(String uri, boolean isSelected, boolean isVideo, long duration) {
        this.uri = uri;
        this.isSelected = isSelected;
        this.isVideo = isVideo;
        this.duration = duration;
    }
    public String getUri() { return uri; }
    public boolean isVideo() { return isVideo; }
    public MediaItem(String uri, boolean isVideo) {
        this(uri, false, isVideo, 0);
    }
}
