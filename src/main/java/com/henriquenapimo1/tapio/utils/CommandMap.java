package com.henriquenapimo1.tapio.utils;

import com.henriquenapimo1.tapio.commands.ICommand;
import com.henriquenapimo1.tapio.commands.games.LolCommand;
import com.henriquenapimo1.tapio.commands.PingCommand;

import java.util.ArrayList;
import java.util.List;

public class CommandMap {

    public static List<ICommand> getMap() {
        List<ICommand> lista = new ArrayList<>();

        lista.add(new PingCommand());
        lista.add(new LolCommand());

        return lista;
    }
}
