package com.henriquenapimo1.tapio.utils.music;

import com.henriquenapimo1.tapio.TapioBot;
import com.henriquenapimo1.tapio.utils.music.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MusicQuizManager {

    private static MusicQuizManager INSTANCE;
    private final Map<Long, MusicQuizManager> quizManager;

    // TODO: MUDAR O TIPO DE GERENCIADOR, DE INSTANCE PRO NORMAL

    public MusicQuizManager() {
        quizManager = new HashMap<>();
    }

    private List<QuizTrack> originalPlaylist;
    private List<QuizTrack> gamePlaylist;

    private TextChannel txtChannel;

    private int round = 0;

    public void startQuiz(String type, TextChannel channel) {
        /*
        dá load na playlist (limita por algumas tracks, ex. 10), seta o canal e começa um timer de 15 segundos
        manda uma mensagem no chat pedindo o NOME ou o AUTOR da música
        com alguns botões de opções. Os jogadores NO CANAL tem até o término do
        temporizador para acertarem. Só podem chutar UMA VEZ com certeza. Caso respondam
        errado, a música é pulada. Quem tiver mais pontos no final, ganha
         */
        txtChannel = channel;
        originalPlaylist = gamePlaylist = TapioBot.getSpotifyAPI().getPlaylistTracks(type);

        Collections.shuffle(gamePlaylist);
        gamePlaylist = gamePlaylist.stream().limit(10).toList();

        // manda embed explicando como funciona e inicia um countdown de alguns segundos

        newRound();
    }

    private String rightAnswer = "";

    private List<Button> getShuffledButtons(QuizTrack track, QuizType type) {
        List<Button> btn = new ArrayList<>();

        List<QuizTrack> tracks = originalPlaylist;
        tracks.remove(track);
        Collections.shuffle(tracks);

        tracks = new ArrayList<>(tracks.stream().limit(5).toList());
        tracks.add(track);

        for(int i = 0; i < 5; i++) {
            if(tracks.get(i).equals(track))
                rightAnswer = "tapiomusicquiz_"+i;

            switch (type) {
                case FULL -> btn.add(Button.primary("tapiomusicquiz_"+i,tracks.get(i).getName() + " - " + tracks.get(i).getArtist()));
                case NAME -> btn.add(Button.primary("tapiomusicquiz_"+i,tracks.get(i).getName()));
                case ARTIST -> btn.add(Button.primary("tapiomusicquiz_"+i,tracks.get(i).getArtist()));
            }
        }

        return btn;
    }

    public void answer(ButtonInteractionEvent event) {
        assert event.getButton().getId() != null;

        if(event.getButton().getId().equals(rightAnswer)) {
            // ganhou!
        } else {
            // perdeu !
        }

        newRound();
    }

    private void newRound() {
        if(round != 0) // se não for o primeiro round, pula a música
            PlayerManager.getInstance().getMusicManager(txtChannel.getGuild()).scheduler.nextTrack();

        // TODO: TIMER DAS MÚSICAS

        PlayerManager.getInstance().loadAndPlay(txtChannel,gamePlaylist.get(round).getAudioURL());

        txtChannel.sendMessage(new MessageBuilder().append("Quiz musical!").setEmbeds(new EmbedBuilder()
                        .setTitle("Seja o primeiro a acertar")
                        .addField("*Qual o nome da música?","*Reaja usando os botões*",true)
                        .setColor(Color.ORANGE)
                        .build()).build())
                .setActionRow(getShuffledButtons(gamePlaylist.get(round),QuizType.NAME)).queue();

        round = round+1;
    }

    private enum QuizType {
        FULL, NAME, ARTIST
    }

    public MusicQuizManager getManager(long guildId) {
        return this.quizManager.computeIfAbsent(guildId, (id) -> INSTANCE);
    }

    public static MusicQuizManager getInstance() {
        if (INSTANCE == null) INSTANCE = new MusicQuizManager();
        return INSTANCE;
    }
}
