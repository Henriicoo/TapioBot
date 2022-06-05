package com.henriquenapimo1.tapio.commands;

import com.henriquenapimo1.tapio.TapioBot;
import com.henriquenapimo1.tapio.utils.CommandContext;
import com.henriquenapimo1.tapio.utils.CommandMap;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.HashMap;
import java.util.List;

public class CommandManager {

    private final List<ICommand> commandList;
    private HashMap<String, ICommand> commandMap = new HashMap<>();

    public CommandManager() {
        commandList = CommandMap.getMap();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void loadAllCommands() {
        HashMap<String, ICommand> map = new HashMap<>();
        CommandListUpdateAction commands = TapioBot.getBot().updateCommands();

        commandList.forEach(c -> {
            map.put(c.getName(),c);
            SlashCommandData cmd = Commands.slash(c.getName(), c.getCategory().toString() + " | " + c.getDescription());
            if(c.getSubcommands() != null) cmd.addSubcommands(c.getSubcommands());
            if(c.getOptions() != null) cmd.addOptions(c.getOptions());
            commands.addCommands(cmd);
        });
        commandMap = map;
        commands.queue();
    }

    public void runCommand(String command, CommandContext ctx) {
        ICommand cmd = getMatch(command);
        SlashCommandInteraction event = ctx.getEvent();

        if(cmd == null) {
            event.deferReply(true).queue();

            InteractionHook hook = event.getHook().setEphemeral(true);
            hook.sendMessage("Que erro estranho... Eu ainda não tenho esse comando!").queue();
            return;
        }

        if(ctx.getUser().isBot()) return;

        // vê se o bot consegue falar
        if(ctx.isFromGuild() && !event.getTextChannel().canTalk()) {
            event.deferReply(true).queue();

            InteractionHook hook = event.getHook().setEphemeral(true);
            hook.sendMessage("Eu não tenho permissão para falar nesse canal!").queue();
            return;
        }

        if(ctx.isFromGuild()) {
            assert event.getGuild() != null;
            // vê se o usuário tem permissão
            if(cmd.getUserPermissions() != null && !ctx.getMember().hasPermission(cmd.getUserPermissions())) {
                ctx.replyEphemeral("Você não tem permissão para usar esse comando!");
                return;
            }
            // vê se o bot tem permissão
            if(cmd.getBotPermissions() != null && !event.getGuild().getSelfMember().hasPermission(cmd.getBotPermissions())) {
                ctx.replyEphemeral("Eu não tenho as permissões necessárias para executar esse comando!");
                return;
            }
        } else {
            // vê se o comando não pode ser executado ali
            if(cmd.isGuildOnly()) {
                ctx.replyEphemeral("Você não pode utilizar esse comando em mensagens privadas!");
                return;
            }
        }

        cmd.run(ctx);
    }

    public ICommand getMatch(String input) {
        return commandMap.get(input);
    }

    public int getCommandSize() {
        return commandList.size();
    }

    public List<ICommand> getCommandList() {
        return commandList;
    }
}

