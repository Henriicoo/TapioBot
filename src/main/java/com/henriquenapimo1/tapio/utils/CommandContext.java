package com.henriquenapimo1.tapio.utils;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;

public class CommandContext {

    private final SlashCommandInteractionEvent event;
    private final ButtonInteractionEvent intEvent;
    private final User user;
    private Member member;

    public ButtonInteractionEvent getInteraction() {
        return intEvent;
    }

    private boolean isSlash() {
        return intEvent == null;
    }

    public CommandContext(ButtonInteractionEvent e, User u) {
        this.event = null;
        this.intEvent = e;
        this.user = u;

        if(e.isFromGuild()) {
            assert e.getGuild() != null;
            this.member = e.getGuild().getMemberById(u.getIdLong());
        }
    }
    public CommandContext(SlashCommandInteractionEvent e, User u) {
        this.event = e;
        this.intEvent = null;
        this.user = u;

        if(e.isFromGuild()) {
            assert e.getGuild() != null;
            this.member = e.getGuild().getMemberById(u.getIdLong());
        }
    }

    public String getAsMention() {
        return getUser().getAsMention();
    }

    public TextChannel getChannel() {
        return event.getTextChannel();
    }

    public List<OptionMapping> getOptions() {
        return event.getOptions();
    }

    public boolean isFromGuild() {
        return event.isFromGuild();
    }

    public SlashCommandInteractionEvent getEvent() {
        return event;
    }

    public User getUser() {
        return user;
    }

    public String getSubCommand() {
        return event.getSubcommandName();
    }

    public Member getMember() {
        return member;
    }

    public void reply(String msg) {
        if(isSlash()) event.getHook().editOriginal(msg).queue(); else intEvent.getHook().editOriginal(msg).queue();
    }

    public void replyEphemeral(String msg) {
        if(isSlash()) event.reply(msg).setEphemeral(true).queue(); else intEvent.reply(msg).setEphemeral(true).queue();
    }

    public void reply(Message msg) {
        if(isSlash())
            event.getHook().editOriginal(msg).queue();
        else
            intEvent.getHook().editOriginal(msg).queue();
    }
}
