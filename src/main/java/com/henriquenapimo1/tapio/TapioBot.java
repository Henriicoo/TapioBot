package com.henriquenapimo1.tapio;

import com.henriquenapimo1.tapio.commands.CommandManager;
import com.henriquenapimo1.tapio.listener.InteractionListener;
import com.henriquenapimo1.tapio.utils.Utils;
import com.henriquenapimo1.tapio.utils.music.SpotifyAPI;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import no.stelar7.api.r4j.basic.APICredentials;
import no.stelar7.api.r4j.impl.R4J;

import javax.security.auth.login.LoginException;

public class TapioBot {
    private static JDA bot;
    private static R4J riotApi;
    private static CommandManager manager;
    private static SpotifyAPI spotifyAPI;

    public static void main(String[] args) throws LoginException {
        manager = new CommandManager();

        bot = JDABuilder.createDefault(Utils.discordToken)
                .setActivity(Activity.playing("a vida fora"))
                .setStatus(OnlineStatus.ONLINE)
                .addEventListeners(new InteractionListener())
                .build();

        System.out.println("⨠ Fui iniciado com sucesso!");

        manager.loadAllCommands();

        System.out.println("→ " + manager.getCommandSize() + " comandos foram carregados!");

        System.out.println("Carregando APIs...");
        riotApi = new R4J(new APICredentials(Utils.riotApi));
        spotifyAPI = new SpotifyAPI();

        System.out.println("Carregando playlists do Quiz Musical");
        spotifyAPI.loadPlaylists();
    }

    public static R4J getRiotApi() {
        return riotApi;
    }

    public static SpotifyAPI getSpotifyAPI() {
        return spotifyAPI;
    }

    public static JDA getBot() {
        return bot;
    }

    public static CommandManager getManager() {
        return manager;
    }
}