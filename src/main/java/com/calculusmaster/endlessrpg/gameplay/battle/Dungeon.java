package com.calculusmaster.endlessrpg.gameplay.battle;

import com.calculusmaster.endlessrpg.EndlessRPG;
import com.calculusmaster.endlessrpg.gameplay.battle.enemy.EnemyBuilder;
import com.calculusmaster.endlessrpg.gameplay.battle.player.AIPlayer;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Dungeon
{
    public static final List<Dungeon> DUNGEONS = new ArrayList<>();

    private PlayerDataQuery player;
    private Location location;
    private int level;
    private MessageReceivedEvent event;

    private List<DungeonEncounter> encounters;
    private int current;
    private List<String> results;

    private boolean active;

    private boolean isFinalKingdom;

    public static Dungeon create(PlayerDataQuery player, Location location, int level, MessageReceivedEvent event)
    {
        Dungeon d = new Dungeon();

        d.setPlayer(player);
        d.setLocation(location);
        d.setLevel(level);
        d.setEvent(event);
        d.createEncounters();
        d.setup();

        DUNGEONS.add(d);
        return d;
    }

    public static Dungeon createFinalKingdom(PlayerDataQuery player, Location location, int level, MessageReceivedEvent event)
    {
        Dungeon d = new Dungeon();

        d.setPlayer(player);
        d.setLocation(location);
        d.setLevel(level);
        d.setEvent(event);
        d.createFinalKingdomEncounters();
        d.setup();

        DUNGEONS.add(d);
        return d;
    }

    public void fail()
    {
        EmbedBuilder embed = new EmbedBuilder();

        embed
                .setTitle(this.location.getName())
                .setDescription("*Level " + this.level + " Dungeon*\nProgress: " + this.current + " / " + this.encounters.size())
                .addField("Results", "***LOSS***\nYou lost the Dungeon! Better luck next time...", false);

        this.event.getChannel().sendMessageEmbeds(embed.build()).queue();

        DUNGEONS.remove(this);
    }

    public void win()
    {
        EmbedBuilder embed = new EmbedBuilder();

        embed
                .setTitle(this.location.getName())
                .setDescription("*Level " + this.level + " Dungeon*\nProgress: " + this.current + " / " + this.encounters.size())
                .addField("Results", "***VICTORY***\nYou have conquered " + this.location.getName() + "!", false);

        this.event.getChannel().sendMessageEmbeds(embed.build()).queue();

        DUNGEONS.remove(this);
    }

    public boolean isComplete()
    {
        return this.current >= this.encounters.size();
    }

    public void nextEncounter()
    {
        this.active = true;

        this.results = new ArrayList<>();
        switch(this.encounters.get(this.current))
        {
            case BATTLE -> {

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(this.location.getName())
                        .setDescription("You run into a group of enemies, who look ready to fight...");

                this.event.getChannel().sendMessageEmbeds(embed.build()).queue();

                //TODO: Single team of RPGCharacters, health goes across multiple encounters
                List<RPGCharacter> enemies = new ArrayList<>();
                int number = this.isFinalKingdom ? 4 : this.player.getCharacterList().size();
                Supplier<Integer> level = this.isFinalKingdom ? () -> this.level - 1 : () -> new SplittableRandom().nextInt(this.level - 1, this.level + 2);
                for(int i = 0; i < number; i++) enemies.add(EnemyBuilder.createDefault(level.get()));

                Battle b = Battle.createDungeon(this.player.getID(), new AIPlayer(enemies), this.location);
                b.setEvent(this.event);

                b.sendTurnEmbed();
            }
            case BOSS, FINAL_KINGDOM_MINI_BOSS, FINAL_KINGDOM_KING, FINAL_KINGDOM_BOSS -> {
                DungeonEncounter enc = this.encounters.get(this.current);
                EmbedBuilder embed = new EmbedBuilder().setTitle(this.location.getName());

                if(enc.equals(DungeonEncounter.BOSS) || enc.equals(DungeonEncounter.FINAL_KINGDOM_MINI_BOSS))
                    embed.setDescription(this.getBossRoomDescription() + "\n\n***A mysterious figure emerges...\nIt is the only obstacle between you and victory...***");
                else if(enc.equals(DungeonEncounter.FINAL_KINGDOM_KING))
                    embed.setDescription(this.getThroneDescription() + "\n\n***The ruler of " + this.location.getName() + " stands in front of the throne and eyes you coldly...You've made it this far, and this is the final stand...***");
                else if(enc.equals(DungeonEncounter.FINAL_KINGDOM_BOSS))
                    embed.setDescription("***You reach the top of the castle, to gaze upon the Realm and relax. Suddenly a massive roar greets you as you turn around to behold a fearsome dragon...This is the true test!***");

                this.event.getChannel().sendMessageEmbeds(embed.build()).queue();

                //TODO: Improve King and Dragon Descriptions
                if(enc.equals(DungeonEncounter.BOSS) || enc.equals(DungeonEncounter.FINAL_KINGDOM_MINI_BOSS))
                    Executors.newSingleThreadScheduledExecutor().schedule(this::startBossFight, 10, TimeUnit.SECONDS);
                else if(enc.equals(DungeonEncounter.FINAL_KINGDOM_KING))
                    Executors.newSingleThreadScheduledExecutor().schedule(this::startKingFight, 10, TimeUnit.SECONDS);
                else if(enc.equals(DungeonEncounter.FINAL_KINGDOM_BOSS))
                    Executors.newSingleThreadScheduledExecutor().schedule(this::startDragonFight, 5, TimeUnit.SECONDS);
            }
            case TREASURE -> {
                this.results.add("`NYI` – Treasure Event");
                this.advance();
            }
            case LOOT -> {
                this.results.add("`NYI` – Loot Event");
                this.advance();
            }
        }
    }

    private void startKingFight()
    {
        this.startBossFight(); //TODO: King Archetype
    }

    private void startDragonFight()
    {
        this.startBossFight(); //TODO: Dragon Archetype
    }

    private void startBossFight()
    {
        //TODO: Dragon or something - cooler boss (this exists in Final Kingdom, but what about regular dungeons?)
        this.startBattle(new AIPlayer(EnemyBuilder.createDefault(this.level + new SplittableRandom().nextInt(5, 11))));
    }

    private void startBattle(AIPlayer ai)
    {
        Battle b = Battle.createDungeon(this.player.getID(), ai, this.location);
        b.setEvent(this.event);

        b.sendTurnEmbed();
    }

    public void addResult(String result)
    {
        this.results.add(result);
    }

    public void advance()
    {
        this.current++;
        this.active = false;

        if(this.isComplete()) this.win();
        else this.sendEncounterEmbed();
    }

    public void sendStartEmbed()
    {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Level " + this.level + " Dungeon")
                .setDescription("You enter `" + this.location.getName() + "` ... What secrets lie within its depths?")
                .addField("Encounters", "Between %s and %s".formatted(this.randomCount()[0], this.randomCount()[1]), false)
                .setFooter("Use 'r!dungeon next' to enter the depths!");

        this.event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }

    public void sendEncounterEmbed()
    {
        EmbedBuilder embed = new EmbedBuilder();

        StringBuilder results = new StringBuilder();
        for(String s : this.results) results.append(s).append("\n");

        embed.setTitle(this.location.getName())
                .setDescription("*Level %s Dungeon*\n".formatted(this.level) + "DUNGEON_DESCRIPTION")
                .addField("Progress", "About to enter Encounter " + (this.current + 1) + "\n**Total:** Between " + this.randomCount()[0] + " and " + this.randomCount()[1], false)
                .addField("Previous Encounter Results (" + Global.normalize(this.encounters.get(this.current - 1).toString()) + ")", results.toString(), false)
                .addField("Next Encounter", new SplittableRandom().nextInt(10) < 5 || this.encounters.get(this.current).equals(DungeonEncounter.BOSS) ? Global.normalize(this.encounters.get(this.current).toString()) : "???", false)
                .setFooter("Use 'r!dungeon next' to continue further!");

        this.event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }

    private int[] randomCount()
    {
        return new int[]{this.encounters.size() - new SplittableRandom().nextInt(1, 5), this.encounters.size() + new SplittableRandom().nextInt(1, 5)};
    }

    private void createEncounters()
    {
        this.isFinalKingdom = false;
        this.encounters = new ArrayList<>();

        int number = new SplittableRandom().nextInt(5, 15);
        for(int i = 0; i < number; i++) this.encounters.add(DungeonEncounter.getRandom());
        this.encounters.add(DungeonEncounter.BOSS);
    }

    private void createFinalKingdomEncounters()
    {
        this.isFinalKingdom = true;
        this.encounters = new ArrayList<>();

        int preMiniBoss = new SplittableRandom().nextInt(1, 4);
        for(int i = 0; i < preMiniBoss; i++) this.encounters.add(DungeonEncounter.BATTLE);
        int miniBoss = new SplittableRandom().nextInt(1, 4);
        for(int i = 0; i < miniBoss; i++) this.encounters.add(DungeonEncounter.FINAL_KINGDOM_MINI_BOSS);
        this.encounters.add(DungeonEncounter.FINAL_KINGDOM_KING);
        this.encounters.add(DungeonEncounter.FINAL_KINGDOM_BOSS);
    }

    private void setup()
    {
        this.current = 0;
        this.active = false;
        this.results = new ArrayList<>();
    }

    private String getBossRoomDescription()
    {
        List<String> pool = new BufferedReader(new InputStreamReader(Objects.requireNonNull(EndlessRPG.class.getResourceAsStream("/descriptions/dungeon_boss.txt")))).lines().toList();
        String desc = pool.get(new SplittableRandom().nextInt(pool.size()));
        return desc.replaceAll("(\\.\\.)", "...\n\n").replaceAll("wait", "*Wait*");
    }

    private String getThroneDescription()
    {
        List<String> pool = new BufferedReader(new InputStreamReader(Objects.requireNonNull(EndlessRPG.class.getResourceAsStream("/descriptions/dungeon_throne.txt")))).lines().toList();
        return pool.get(new SplittableRandom().nextInt(pool.size()));
    }

    public static boolean isInDungeon(String ID)
    {
        return DUNGEONS.stream().anyMatch(b -> b.getPlayer().getID().equals(ID));
    }

    public static Dungeon instance(String ID)
    {
        return Dungeon.isInDungeon(ID) ? DUNGEONS.stream().filter(b -> b.getPlayer().getID().equals(ID)).collect(Collectors.toList()).get(0) : null;
    }

    public static void delete(String ID)
    {
        int index = -1;
        for(int i = 0; i < DUNGEONS.size(); i++) if(DUNGEONS.get(i).getPlayer().getID().equals(ID)) index = i;
        if(index != -1) DUNGEONS.remove(index);
    }

    public boolean isActive()
    {
        return this.active;
    }

    public PlayerDataQuery getPlayer()
    {
        return this.player;
    }

    private void setLocation(Location location)
    {
        this.location = location;
    }

    private void setPlayer(PlayerDataQuery player)
    {
        this.player = player;
    }

    private void setLevel(int level)
    {
        this.level = level;
    }

    private void setEvent(MessageReceivedEvent event)
    {
        this.event = event;
    }

    private enum DungeonEncounter
    {
        //Regular Dungeon
        BATTLE,
        TREASURE,
        LOOT,
        BOSS,
        //Final Kingdom Dungeon
        FINAL_KINGDOM_MINI_BOSS,
        FINAL_KINGDOM_KING,
        FINAL_KINGDOM_BOSS;

        static DungeonEncounter getRandom()
        {
            return Arrays.copyOfRange(values(), 0, 4)[new SplittableRandom().nextInt(4)];
        }
    }
}
