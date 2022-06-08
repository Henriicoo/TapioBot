package com.henriquenapimo1.tapio.utils.music;

import com.henriquenapimo1.tapio.TapioBot;
import com.henriquenapimo1.tapio.utils.music.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MusicQuizManager {

    private static MusicQuizManager INSTANCE;
    private final Map<Long, MusicQuizManager> quizManager;

    public MusicQuizManager() {
        quizManager = new HashMap<>();
    }

    private boolean isInGame = false;

    private List<QuizTrack> originalPlaylist;
    private List<QuizTrack> gamePlaylist;

    private TextChannel txtChannel;

    private int round = 0;

    private Timer timer;

    public void startQuiz(String type, TextChannel channel) {
        /*
        dá load na playlist (limita por algumas tracks, ex. 10), seta o canal e começa um timer de 15 segundos
        manda uma mensagem no chat pedindo o NOME ou o AUTOR da música
        com alguns botões de opções. Os jogadores NO CANAL tem até o término do
        temporizador para acertarem. Só podem responder UMA VEZ com certeza. Caso respondam
        errado, a música é pulada, caso respondam certo, o jogador leva um ponto.
         Quem tiver mais pontos no final, ganha
         */

        if(isInGame)
            return;

        isInGame = true;

        txtChannel = channel;
        originalPlaylist = gamePlaylist = TapioBot.getSpotifyAPI().getPlaylistTracks(type);

        Collections.shuffle(gamePlaylist);
        gamePlaylist = gamePlaylist.stream().limit(10).toList();

        txtChannel.sendMessage(new MessageBuilder().setEmbeds(new EmbedBuilder()
                        .setTitle("Quiz Musical 🎵")
                        .setColor(new Color(99,89,148))
                        .appendDescription("""
                        **Como jogar?**
                        Uma música será tocada por `30` segundos, e o seu objetivo é acertar o nome ou o cantor dela, usando botões, que estarão em baixo da mensagem. Vence o jogador que apertar o botão **CORRETO** primeiro.
                        Caso o botão errado seja pressionado, todos perdem e começará a próxima música.
                        O jogo será iniciado em `5` segundos. Todos os participantes deverão estar no *mesmo canal* de voz.
                        **Bom jogo**!""")
                        .setTimestamp(Instant.now())
                        .setFooter("Quiz Musical - TEMA " + type)
                        .build()
                ).build()
        ).queue();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                newRound(true,false, false,null);
                timer.cancel();
            }
        }, 5 * 1000);
    }

    private String rightAnswerID = "";
    private String rightAnswerString = "";

    private QuizType quizType;

    private List<Button> getShuffledButtons(QuizTrack track) {
        List<Button> btn = new ArrayList<>();

        List<QuizTrack> tracks = originalPlaylist;
        tracks.remove(track);
        Collections.shuffle(tracks);

        tracks = new ArrayList<>(tracks.stream().limit(5).toList());
        tracks.add(track);

        Collections.shuffle(tracks);

        List<QuizType> types = new ArrayList<>(Arrays.stream(QuizType.values()).toList());
        Collections.shuffle(types);

        QuizType type = types.get(0);
        quizType = type;

        for(int i = 0; i < 5; i++) {
            boolean isEqual = tracks.get(i).equals(track);
            if(isEqual) rightAnswerID = "tapiomusicquiz_" + i;

            switch (type) {
                case FULL -> {
                    String name = tracks.get(i).getName() + " - " + tracks.get(i).getArtist();
                    if(isEqual) rightAnswerString = name;
                    btn.add(Button.secondary("tapiomusicquiz_" + i, name));
                }
                case NAME -> {
                    String name = tracks.get(i).getName();
                    if(isEqual) rightAnswerString = name;
                    btn.add(Button.secondary("tapiomusicquiz_" + i, name));
                }
                case ARTIST -> {
                    String name = tracks.get(i).getArtist();
                    if(isEqual) rightAnswerString = name;
                    btn.add(Button.secondary("tapiomusicquiz_" + i, name));
                }
            }
        }

        return btn;
    }

    private final HashMap<Long,Integer> placar = new HashMap<>();

    public void answer(ButtonInteractionEvent event) {
        assert event.getButton().getId() != null;
        assert event.getMember() != null;

        if(!isInGame) {
            event.reply("Não há nenhum quiz acontecendo no momento!").setEphemeral(true).queue();
            return;
        }

        // se o usuário não está no canal de voz
        if(!event.getMember().getVoiceState().inAudioChannel() || !event.getMember().getVoiceState().getChannel().equals(
                event.getGuild().getAudioManager().getConnectedChannel())) {
            event.reply("Você não está participando do jogo. Entre no canal de voz para participar").setEphemeral(true).queue();
            return;
        }
        timer.cancel();

        if(event.getButton().getId().equals(rightAnswerID)) { // ganhou!

            event.reply("Você acertou! Parabéns").setEphemeral(true).queue();

            if(placar.containsKey(event.getUser().getIdLong())) {
                placar.replace(event.getUser().getIdLong(), placar.get(event.getUser().getIdLong())+1);
            } else {
                placar.put(event.getUser().getIdLong(),1);
            }

            newRound(false,true, false, event.getUser().getIdLong());
        } else { // perdeu !
            event.reply("Não foi dessa vez.").setEphemeral(true).queue();

            newRound(false,false, false,null);
        }
    }

    private Message gameMessage;

    private void newRound(boolean isNew, boolean rightAnswer, boolean timesUp, Long id) {
        if(!isNew) { // pula a música e manda a resposta correta
            PlayerManager.getInstance().getMusicManager(txtChannel.getGuild()).audioPlayer.stopTrack();
            String status = "Ninguém acertou!";
            if(rightAnswer)
                status = "Resposta correta! "+txtChannel.getGuild().getMemberById(id).getUser().getName()+" acertou!";

            if(timesUp)
                status = "O tempo para resposta ACABOU!";

            String aviso = "Um novo round irá começar em 5 segundos.";
            if(round==10)
                aviso = "Fim de jogo. Aguarde para ver o placar.";

            gameMessage.editMessage(new MessageBuilder().setEmbeds(new EmbedBuilder()
                            .setColor(new Color(99,89,148))
                            .setTitle(status)
                            .setDescription("A resposta correta era: `"+rightAnswerString+"`\n"+aviso)
                            .build())
                    .build()).submit()
                    .thenCompose((m) -> m.delete().submitAfter(5, TimeUnit.SECONDS));
        }

        if(round==10) {
            Object[] a = placar.entrySet().toArray();

            Arrays.sort(a, (o1, o2) -> ((Map.Entry<String, Integer>) o2).getValue()
                    .compareTo(((Map.Entry<String, Integer>) o1).getValue()));

            StringBuilder placar = new StringBuilder();

            for (Object e : a) {
                placar.append(String.format("**%s**",(txtChannel.getGuild().getMemberById(String.valueOf(((Map.Entry<String, Integer>) e).getKey())).getAsMention()))).append(" → **").append(((Map.Entry<String, Integer>) e).getValue()).append("** pontos;\n");
            }

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    txtChannel.sendMessage(new MessageBuilder().setEmbeds(new EmbedBuilder()
                                    .setTitle("Fim de Jogo!")
                                    .setDescription("O jogo terminou. Veja o placar de pontos de cada um:\n \n"+ placar)
                                    .setTimestamp(Instant.now())
                                    .setColor(new Color(99,89,148))
                                    .build())
                            .build()
                    ).queue();

                    PlayerManager.getInstance().getMusicManager(txtChannel.getGuild()).audioPlayer.stopTrack();
                    txtChannel.getGuild().getAudioManager().closeAudioConnection();

                    endGame();
                }
                },5*1000);
            return;
        }

        if(!isNew) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startRound();
                    timer.cancel();
                }
            },5*1000);
            return;
        }

        startRound();
    }

    public void timesOver() {
        newRound(false,false, true,null);
    }

    private void startRound() {
        PlayerManager.getInstance().loadAndPlay(txtChannel,gamePlaylist.get(round).getAudioURL());
        List<Button> botoes = getShuffledButtons(gamePlaylist.get(round));

        if(gameMessage != null)
            gameMessage.delete().queue();

        Consumer<Message> callback = (response) -> gameMessage = response;
        txtChannel.sendMessage(new MessageBuilder().setEmbeds(new EmbedBuilder()
                        .setTitle("Round "+(round+1)+"/10 • Qual o "+typeParser(quizType)+" da música?")
                        .addField("Seja o primeiro a acertar","*Reaja usando os botões*",true)
                        .setColor(new Color(99,89,148))
                        .build())
                .setActionRows(ActionRow.of(botoes)).build()).queue(callback);

        round = round+1;
    }

    private String typeParser(QuizType t) {
        switch (t) {
            case FULL -> {
                return "nome e o artista";
            }
            case NAME -> {
                return "nome";
            }
            case ARTIST -> {
                return "artista";
            }
            default -> {
                return "";
            }
        }
    }

    private void endGame() {
        quizType = null;
        round = 0;
        originalPlaylist = null;
        gamePlaylist = null;
        txtChannel = null;

        if(timer != null)
            timer.cancel();

        timer = null;
        rightAnswerID = "";
        rightAnswerString = "";
        gameMessage = null;

        isInGame = false;
    }

    private enum QuizType {
        FULL, NAME, ARTIST
    }

    public MusicQuizManager getManager(long guildId) {
        return this.quizManager.computeIfAbsent(guildId, (id) -> INSTANCE);
    }

    public boolean isInGame() {
        return isInGame;
    }

    public static MusicQuizManager getInstance() {
        if (INSTANCE == null) INSTANCE = new MusicQuizManager();
        return INSTANCE;
    }
}
