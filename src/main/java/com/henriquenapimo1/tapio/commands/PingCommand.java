package com.henriquenapimo1.tapio.commands;

import com.henriquenapimo1.tapio.TapioBot;
import com.henriquenapimo1.tapio.utils.CommandContext;
import org.jetbrains.annotations.NotNull;

public class PingCommand implements ICommand {
    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String getDescription() {
        return "ping do bot só pra teste";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILS;
    }

    @Override
    public boolean isGuildOnly() {
        return false;
    }

    @Override
    public void run(@NotNull CommandContext ctx) {
        ctx.reply("Pong " + TapioBot.getBot().getGatewayPing()+"ms");
    }
}
