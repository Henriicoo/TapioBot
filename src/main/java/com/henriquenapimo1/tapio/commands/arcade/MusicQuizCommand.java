package com.henriquenapimo1.tapio.commands.arcade;

import com.henriquenapimo1.tapio.commands.ICommand;
import com.henriquenapimo1.tapio.utils.CommandContext;
import com.henriquenapimo1.tapio.utils.music.MusicQuizManager;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MusicQuizCommand implements ICommand {
    @Override
    public String getName() {
        return "quiz";
    }

    @Override
    public String getDescription() {
        return "quiz musical";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.ARCADE;
    }

    @Override
    public List<SubcommandData> getSubcommands() {
        return List.of(new SubcommandData("iniciar","Inicia o quiz musical. (É preciso estar num canal de voz)")
                .addOptions(new OptionData(OptionType.STRING,"genero","O gênero que será usado no jogo",true)
                        .addChoice("Indie 2016","indie2016")));
    }

    @Override
    public boolean isGuildOnly() {
        return true;
    }

    @Override
    public void run(@NotNull CommandContext ctx) {
        assert ctx.getEvent().getGuild() != null;

        if(ctx.getMember().getVoiceState() == null) {
            ctx.replyEphemeral("Você não pode jogar o quiz sem estar num canal de voz!");
            return;
        }

        ctx.getEvent().getGuild().getAudioManager().openAudioConnection(ctx.getMember().getVoiceState().getChannel());
        MusicQuizManager.getInstance().startQuiz(ctx.getOptions().get(0).getAsString(), ctx.getChannel());
    }
}
