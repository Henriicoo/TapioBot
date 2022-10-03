package com.henriquenapimo1.tapio.utils;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.henriquenapimo1.tapio.TapioBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.text.StringEscapeUtils;

import java.awt.*;
import java.time.Instant;
import java.util.*;

public class Eleicoes {

    public Eleicoes() {
        Timer timer = new Timer();

        timer.schedule( new TimerTask() {
            public void run() {
                anunciar();
            }
        }, 0, 60*5*1000);
    }

    private String header;

    private SortedMap<Integer,Candidato> getPorcentagem() {
        SortedMap<Integer,Candidato> resultados = new TreeMap<>();

        String tseResponse = HttpRequest.get("https://resultados.tse.jus.br/oficial/ele2022/544/dados-simplificados/br/br-c0001-e000544-r.json").body();

        JsonObject eleicoes = JsonParser.parseString(tseResponse).getAsJsonObject();

        header = "Urnas Apuradas: **" + eleicoes.getAsJsonObject().get("pst").getAsString() + "%** - " +
                "Última atualização: *" + eleicoes.getAsJsonObject().get("ht").getAsString() + "*";

        JsonArray rslt = eleicoes.getAsJsonArray("cand");

        rslt.forEach(r -> {
            JsonObject res = r.getAsJsonObject();
            resultados.put(res.get("seq").getAsInt(),new Candidato(StringEscapeUtils.unescapeXml(res.get("nm").getAsString()),res.get("n").getAsInt(),res.get("vap").getAsLong(),res.get("pvap").getAsString()));
        });

        return resultados;
    }

    private void anunciar() {
        SortedMap<Integer,Candidato> candidatos = getPorcentagem();

        TextChannel channel = TapioBot.getBot().getTextChannelById("1015307391916572796");

        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor("Eleições Presidenciais 2022","https://resultados.tse.jus.br","https://direitosnarede.org.br/wp-content/uploads/2022/03/Banners-CDR-Site_eleicoes_sticker-300x300.png");
        eb.setDescription(header);

        eb.setTimestamp(Instant.now());
        eb.setColor(Color.decode("#59358c"));

        eb.setFooter("Fonte: TSE - Tribunal Superior Eleitoral","https://play-lh.googleusercontent.com/YT8lW0SSb_1-Il4I0q11ZpqH3d_nio4im20KYPIfg9VxulivXhK0p1dKCTpb4Z2l3v0=w240-h480-rw");

        Set<Map.Entry<Integer, Candidato>> s = candidatos.entrySet();

        for (Map.Entry<Integer, Candidato> integerCandidatoEntry : s) {
            Candidato cand = (Candidato) ((Map.Entry<?, ?>) integerCandidatoEntry).getValue();
            eb.addField(cand.nome+" • "+cand.porcent+"%",cand.votos + " votos válidos", true);
        }

        channel.sendMessageEmbeds(eb.build()).queue();

    }
}
