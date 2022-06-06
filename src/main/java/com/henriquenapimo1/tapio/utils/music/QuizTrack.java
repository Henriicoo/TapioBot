package com.henriquenapimo1.tapio.utils.music;

public class QuizTrack {

    private final String name;
    private final String artist;
    private final String audioURL;

    public QuizTrack(String name, String artist, String previewUrl) {
        this.artist = artist;
        this.name = name;
        this.audioURL = previewUrl;
    }

    public String getArtist() {
        return artist;
    }

    public String getName() {
        return name;
    }

    public String getAudioURL() {
        return audioURL;
    }
}
