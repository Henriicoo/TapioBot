package com.henriquenapimo1.tapio.commands;

import com.henriquenapimo1.tapio.utils.CommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ICommand {

    String getName();

    String getDescription();

    CommandCategory getCategory();

    default List<OptionData> getOptions() {
        return null;
    }

    default List<SubcommandData> getSubcommands() {
        return null;
    }

    boolean isGuildOnly();

    default List<Permission> getUserPermissions() {
        return null;
    }

    default List<Permission> getBotPermissions() {
        return null;
    }

    void run(@NotNull CommandContext ctx);

    enum CommandCategory {
        ARCADE, DISCORD, UTILS, STUDIE, FUN, MUSIC
    }
}

