package com.henriquenapimo1.tapio.utils.music.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildMusicManager {
    public final AudioPlayer audioPlayer;

    public final TrackScheduler scheduler;

    private final AudioPlayerSendHandler sendHandler;

    private final PlayerManager playerManager;

    public GuildMusicManager(AudioPlayerManager manager, PlayerManager playerManager) {
        this.audioPlayer = manager.createPlayer();
        this.scheduler = new TrackScheduler(this.audioPlayer,this);
        this.audioPlayer.addListener(this.scheduler);
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
        this.playerManager = playerManager;
    }

    public AudioPlayerSendHandler getSendHandler() {
        return sendHandler;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
