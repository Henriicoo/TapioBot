package com.henriquenapimo1.tapio.utils;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.henriquenapimo1.tapio.TapioBot;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.*;

public class Eleicoes {

    public Eleicoes() {
        Timer timer = new Timer();

        timer.schedule( new TimerTask() {
            public void run() {
                anunciar();
            }
        }, 0, 60*1000);
    }

    private String header;

    private SortedMap<Integer,Candidato> getPorcentagem() {
        SortedMap<Integer,Candidato> resultados = new TreeMap<>();

        String tseResponse = HttpRequest.get("https://resultados.tse.jus.br/oficial/ele2022/544/dados-simplificados/br/br-c0001-e000544-r.json").body();

        JsonObject eleicoes = JsonParser.parseString(tseResponse).getAsJsonObject();

        StringBuilder b = new StringBuilder();
        b.append("Urnas Apuradas: **").append(eleicoes.getAsJsonObject().get("pst").getAsString()).append("%** - ");
        b.append("Última atualização: *").append(eleicoes.getAsJsonObject().get("ht").getAsString()).append("*");
        header = b.toString();

        JsonArray rslt = eleicoes.getAsJsonArray("cand");

        rslt.forEach(r -> {
            resultados.put(r.getAsJsonObject().get("seq").getAsInt(),new Candidato(r.getAsJsonObject().get("nm").getAsString(),r.getAsJsonObject().get("n").getAsInt(),r.getAsJsonObject().get("vap").getAsLong(),r.getAsJsonObject().get("pvap").getAsString()));
        });

        return resultados;
    }

    private void anunciar() {
        SortedMap<Integer,Candidato> candidatos = getPorcentagem();

        TextChannel channel = TapioBot.getBot().getTextChannelById("1015307391916572796");

        StringBuilder b = new StringBuilder();
        b.append(header).append("\n");

        Set<Map.Entry<Integer, Candidato>> s = candidatos.entrySet();

        for (Map.Entry<Integer, Candidato> integerCandidatoEntry : s) {

            int key = (Integer) ((Map.Entry) integerCandidatoEntry).getKey();
            Candidato cand = (Candidato) ((Map.Entry) integerCandidatoEntry).getValue();
            b.append(cand.nome).append(" (*").append(cand.num).append("*) - **").append(cand.porcent).append("%** (*").append(cand.votos).append("*)\n");
        }

        channel.sendMessage(b.toString()).queue();

    }
}
