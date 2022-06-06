package com.henriquenapimo1.tapio.commands.games;

import com.henriquenapimo1.tapio.TapioBot;
import com.henriquenapimo1.tapio.commands.ICommand;
import com.henriquenapimo1.tapio.utils.CommandContext;
import com.henriquenapimo1.tapio.utils.Emotes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import no.stelar7.api.r4j.basic.constants.api.regions.LeagueShard;
import no.stelar7.api.r4j.basic.constants.types.lol.LaneType;
import no.stelar7.api.r4j.basic.constants.types.lol.TeamType;
import no.stelar7.api.r4j.pojo.lol.champion.ChampionRotationInfo;
import no.stelar7.api.r4j.pojo.lol.championmastery.ChampionMastery;
import no.stelar7.api.r4j.pojo.lol.match.v5.LOLMatch;
import no.stelar7.api.r4j.pojo.lol.match.v5.MatchParticipant;
import no.stelar7.api.r4j.pojo.lol.staticdata.champion.StaticChampion;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class LolCommand implements ICommand {

    @Override
    public String getName() {
        return "lol";
    }

    @Override
    public String getDescription() {
        return "Status do perfil";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.GAMES;
    }

    @Override
    public boolean isGuildOnly() {
        return false;
    }

    @Override
    public List<SubcommandData> getSubcommands() {
        return List.of(new SubcommandData("perfil","Mostra os status das últimas partidas do jogador")
                .addOption(OptionType.STRING,"nick","Nick do jogador",true),
                new SubcommandData("partida", "Mostra os dados da partida mais recente jogada pelo jogador")
                        .addOption(OptionType.STRING,"nick","Nick do jogador",true),
                new SubcommandData("rotacao","A lista de campeões disponíveis para jogar de graça"));
    }

    @Override
    public void run(@NotNull CommandContext ctx) {
        if(ctx.getSubCommand().equals("partida"))
            lastMatch(ctx,ctx.getOptions().get(0).getAsString());

        if(ctx.getSubCommand().equals("rotacao"))
            rotacao(ctx);

        if(!ctx.getSubCommand().equals("perfil"))
            return;

        Summoner summoner = Summoner.byName(LeagueShard.BR1,ctx.getOptions().get(0).getAsString());

        if(summoner == null) {
            ctx.replyEphemeral("Jogador não encontrado!");
            return;
        }

        EmbedBuilder eb = new EmbedBuilder()
                .setAuthor(summoner.getName() + " • LVL " + summoner.getSummonerLevel(), "https://www.leagueoflegends.com/",
                        TapioBot.getRiotApi().getImageAPI().getProfileIcon(String.valueOf(summoner.getProfileIconId()),"12.10.1"))
                .setTimestamp(Instant.now())
                .setColor(new Color(3,151,171));

        // maior maestria champ
        ChampionMastery mastery = summoner.getChampionMasteries().stream().max(
                Comparator.comparing(ChampionMastery::getChampionPoints)).orElse(null);

        if(mastery != null) {
            StaticChampion champ = TapioBot.getRiotApi().getDDragonAPI().getChampion(mastery.getChampionId());

            eb.setThumbnail(TapioBot.getRiotApi().getImageAPI().getSquare(champ.getKey(),"12.10.1"));
            eb.addField(Emotes.masteryEmote+" Maior Maestria",champ.getName() + " • LVL "+mastery.getChampionLevel(),true);
            eb.addField(Emotes.xpEmote+" Pontos", String.valueOf(mastery.getChampionPoints()),true);
            eb.addField(Emotes.timeEmote+" Última vez jogado",
                    new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm").format(Date.from(Instant.ofEpochMilli(mastery.getLastPlayTime()))),true);
        }

        // partidas
        List<String> history = summoner.getLeagueGames().get();

        int num = Math.min(history.size(), 20);
        int kill=0,death=0,asst=0,trop=0,gold=0,dmgt=0,dmgc=0,win = 0;

        for(int i = 0; i < num; i++) {
            LOLMatch match = LOLMatch.get(summoner.getPlatform(),history.get(i));
            Optional<MatchParticipant> self = match.getParticipants().stream().filter(p -> p.getPuuid().equals(summoner.getPUUID())).findFirst();

            if (self.isPresent()) {
                MatchParticipant s = self.get();
                kill = kill + s.getKills();
                death = death + s.getDeaths();
                asst = asst + s.getAssists();
                trop = trop + s.getTotalMinionsKilled();
                gold = gold + s.getGoldEarned();
                dmgt = dmgt + s.getTotalDamageTaken();
                dmgc = dmgc + s.getTotalDamageDealtToChampions();
                if(s.didWin())
                    win = win+1;
            }
        }

        eb.addField("→ Partidas Recentes","*Últimas "+num+" partidas*",true);
        eb.addField(Emotes.combatEmote+" K/D/A médio", String.format("**%s**/**%s**/**%s** (%s AMA)",kill/num,death/num,asst/num, String.format("%.2f",((double)kill+(double)asst)/(double)death)),true);
        eb.addField("Tipos de dano",String.format(Emotes.attackEmote+" **Causado**: %s\n"+ Emotes.armorEmote+" **Recebido**: %s",dmgc/num,dmgt/num),true);
        eb.addField(Emotes.troopsEmote+" Tropas derrotadas", String.valueOf(trop/num),true);
        eb.addField(Emotes.goldEmote+" Ouro ganho", String.valueOf(gold/num),true);
        eb.addField("Taxa de Vitória",String.format("**%s%%**",win*100/num),true);

        ctx.getEvent().getHook().editOriginal(new MessageBuilder().append(ctx.getUser().getAsMention()).setEmbeds(eb.build()).build())
                .setActionRow(Button.secondary("tapiolastmatch_"+summoner.getName(),"Última partida")).queue();
    }

    private EmbedBuilder eb;
    public void lastMatch(CommandContext ctx, String name) {
        Summoner summoner = Summoner.byName(LeagueShard.BR1, name);

        if(summoner == null) {
            ctx.replyEphemeral("Jogador não encontrado");
            return;
        }

        String last = summoner.getLeagueGames().get().get(0);
        LOLMatch match = LOLMatch.get(summoner.getPlatform(), last);
        Optional<MatchParticipant> op = match.getParticipants().stream().filter(p -> p.getPuuid().equals(summoner.getPUUID())).findFirst();

        if(op.isEmpty()) return;
        MatchParticipant self = op.get();

        eb = new EmbedBuilder()
                .setAuthor("Última Partida • "+summoner.getName()+" às "+
                        new SimpleDateFormat("HH:mm dd/MM/yyyy").format(Date.from(Instant.ofEpochMilli(match.getGameStartTimestamp()))),"https://www.leagueoflegends.com/",
                        TapioBot.getRiotApi().getImageAPI().getProfileIcon(String.valueOf(summoner.getProfileIconId()),"12.10.1"));

        StaticChampion champ = TapioBot.getRiotApi().getDDragonAPI().getChampion(self.getChampionId());
        eb.setThumbnail(TapioBot.getRiotApi().getImageAPI().getSquare(champ.getKey(),"12.10.1"));
        eb.setTimestamp(Instant.ofEpochMilli(match.getGameStartTimestamp()));

        if(self.didWin()) {
            eb.setTitle(match.getQueue().commonName()+" • Vitória");
            eb.setColor(new Color(41, 117, 252));
        } else {
            eb.setTitle(match.getQueue().commonName()+" • Derrota");
            eb.setColor(new Color(252, 35, 54));
        }

        TeamType userTeam = self.getTeam();
        List<MatchParticipant> mainTeam = match.getParticipants().stream().filter(p -> p.getTeam().equals(userTeam)).toList();
        List<MatchParticipant> otherTeam = match.getParticipants().stream().filter(p -> !p.getTeam().equals(userTeam)).toList();

        eb.appendDescription("**Resumo da Partida**");

        AtomicInteger k = new AtomicInteger(),d = new AtomicInteger(),a = new AtomicInteger();

        mainTeam.forEach(p -> {
            k.set(k.get() + p.getKills());
            d.set(d.get() + p.getDeaths());
            a.set(a.get() + p.getAssists());
        });

        eb.addField("Equipe "+userTeam.prettyName(), Emotes.combatEmote+" KDA "+
                String.format("**%s**/**%s**/**%s** (*%s AMA*) ",k.get(),d.get(),a.get(), String.format("%.2f",((double)k.get()+d.get())/(double)a.get())),true);
        mainTeam.forEach(this::addUserField);

        otherTeam.forEach(p -> {
            k.set(k.get() + p.getKills());
            d.set(d.get() + p.getDeaths());
            a.set(a.get() + p.getAssists());
        });

        eb.addField("Equipe "+userTeam.opposite().prettyName(), Emotes.combatEmote+" KDA "+
                String.format("**%s**/**%s**/**%s** (*%s AMA*) ",k.get(),d.get(),a.get(), String.format("%.2f",((double)k.get()+d.get())/(double)a.get())),true);
        otherTeam.forEach(this::addUserField);

        if(ctx.getEvent() != null) ctx.getEvent().getHook().editOriginal(new MessageBuilder().append(ctx.getUser()).setEmbeds(eb.build()).build()).queue();
        else ctx.getInteraction().getHook().editOriginal(new MessageBuilder().append(ctx.getUser()).setEmbeds(eb.build()).build()).queue();
    }

    private void addUserField(MatchParticipant p) {
        eb.addField(parseLane(p.getChampionSelectLane())+" "+p.getSummonerName() + " • " + p.getChampionName(), Emotes.combatEmote+"KDA: "+
                String.format("**%s**/**%s**/**%s** (*%s AMA*) ",p.getKills(),p.getDeaths(),p.getAssists(), String.format("%.2f",((double)p.getKills()+(double)p.getAssists())/(double)p.getDeaths())) +
                Emotes.goldEmote+" Ouro: **"+p.getGoldEarned()+"** "+ Emotes.attackEmote+" Dano: **"+p.getTotalDamageDealtToChampions()+"**",true);
    }

    private String parseLane(LaneType lane) {
        return switch (lane) {
            case TOP -> Emotes.topEmote;
            case JUNGLE -> Emotes.jgEmote;
            case MID -> Emotes.midEmote;
            case BOT -> Emotes.botEmote;
            case UITILITY -> Emotes.supEmote;
            default -> "";
        };
    }

    private void rotacao(CommandContext ctx) {
        ChampionRotationInfo info = TapioBot.getRiotApi().getLoLAPI().getChampionAPI().getFreeToPlayRotation(LeagueShard.BR1);

        EmbedBuilder eb = new EmbedBuilder()
                .setTimestamp(Instant.now())
                .setColor(new Color(3,151,171));

        info.getFreeChampions().forEach(c -> eb.addField(parseRoles(c.getTags()) + " • " + c.getName(),
                parseDifficult(c.getInfo().getDifficulty()) + "\n" +
                        Emotes.attackEmote+" ATK: "+(int)c.getStats().getAttackdamage() + "\n" +
                        Emotes.armorEmote+" ARM: "+(int)c.getStats().getArmor() + "\n" +
                        Emotes.healthEmote+" HP: "+(int)c.getStats().getHp() + "\n" +
                        Emotes.magicEmote+" MSH: "+(int)c.getStats().getSpellblock() + "\n" +
                        Emotes.forceEmote+" MANA: "+(int)c.getStats().getMp(),true));

        ctx.reply(new MessageBuilder().setEmbeds(eb.build()).append(ctx.getUser().getAsMention()).append(" | Essa é a rotação semanal atual:").build());
    }

    private String parseDifficult(int dif) {
        if(dif<=3) return String.format("%s %s %s", Emotes.dif1, Emotes.dif0, Emotes.dif0);
        if(dif<=6) return String.format("%s %s %s", Emotes.dif1, Emotes.dif2, Emotes.dif0);
        if(dif<=10) return String.format("%s %s %s", Emotes.dif1, Emotes.dif2, Emotes.dif3);
        return String.format("%s %s %s", Emotes.dif0, Emotes.dif0, Emotes.dif0);
    }

    private String parseRoles(List<String> roles) {
        StringBuilder b = new StringBuilder();
        roles.forEach(r -> {
            switch (r) {
                case "Assassin" -> b.append(Emotes.assassin).append(" ");
                case "Fighter" -> b.append(Emotes.fighter).append(" ");
                case "Mage" -> b.append(Emotes.mage).append(" ");
                case "Marksman" -> b.append(Emotes.marksman).append(" ");
                case "Support" -> b.append(Emotes.support).append(" ");
                case "Tank" -> b.append(Emotes.tank).append(" ");
                default -> {}
            }
        });
        return b.toString();
    }
}
