package com.henriquenapimo1.tapio.listener;

import com.henriquenapimo1.tapio.TapioBot;
import com.henriquenapimo1.tapio.commands.games.LolCommand;
import com.henriquenapimo1.tapio.utils.CommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class InteractionListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        System.out.format("⨠ %s em #%s usou → /%s %s %s%n",event.getUser().getAsTag(),event.getChannel().getName(),
                event.getName(),event.getSubcommandName()!=null  ? event.getSubcommandName() : "",event.getOptions().isEmpty() ? "" : event.getOptions());

        event.deferReply().queue();

        TapioBot.getManager().runCommand(event.getName(),
                new CommandContext(event,event.getUser()));
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        System.out.format("⨠ %s em #%s usou o botão → %s%n",event.getUser().getAsTag(),event.getChannel().getName(),event.getButton().getId());

        if(event.getButton().getId() != null && event.getButton().getId().startsWith("tapiolastmatch_")) {
            String name = event.getButton().getId().replace("tapiolastmatch_","");

            event.deferReply().queue();

            new LolCommand().lastMatch(new CommandContext(event,event.getUser()),name);
        }
    }
}
