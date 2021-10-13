package com.calculusmaster.endlessrpg.gameplay.adventure;

import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;
import com.calculusmaster.endlessrpg.util.Global;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Adventure
{
    public static final List<Adventure> ADVENTURES = new ArrayList<>();
    public static final Map<String, ScheduledFuture<?>> END_TIMES = new HashMap<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(5);

    //Core
    private RPGCharacter character;
    private PlayerDataQuery player;
    private int length;
    private int progress;

    private List<AdventureEvent> eventLog;

    //Rewards
    private int rewardGold;
    private int rewardXP;
    private List<LootItem> rewardLoot;
    private Map<Stat, Integer> rewardCoreStat;

    private Adventure() {}

    public static Adventure create(PlayerDataQuery player, int length)
    {
        Adventure a = new Adventure();

        a.setCharacter(player.getActiveCharacter());
        a.setPlayer(player);
        a.setup(length);

        ADVENTURES.add(a);
        return a;
    }

    //Start the Adventure
    public void start()
    {
        ScheduledFuture<?> end = SCHEDULER.scheduleAtFixedRate(this::event, 0, 15, TimeUnit.MINUTES);

        END_TIMES.put(this.character.getCharacterID(), end);
    }

    //Called periodically throughout the adventure, for random events to proc
    private void event()
    {
        if((this.eventLog.isEmpty() && this.progress == this.length - 1) || new Random().nextInt(100) < 20)
        {
            AdventureEvent random = AdventureEvent.getRandom();
            this.executeEvent(random);
        }

        this.progress++;
        if(this.progress >= this.length) this.end();
    }

    private void executeEvent(AdventureEvent event)
    {
        if(event.equals(AdventureEvent.EARN_CORE_STAT) && this.eventLog.contains(AdventureEvent.EARN_CORE_STAT)) this.executeEvent(AdventureEvent.getRandom());

        switch(event)
        {
            case EARN_GOLD -> this.rewardGold += (new Random().nextInt(this.length) * 150 + this.length * 5);
            case EARN_XP -> this.rewardXP += (new Random().nextInt(this.length) * 100 + this.length * 10);
            case EARN_LOOT -> {
                final Random r = new Random();
                LootType lootType;

                do { lootType = LootType.values()[LootType.values().length]; }
                while(lootType.equals(LootType.NONE));

                LootItem earned = switch(lootType) {
                    case SWORD -> LootItem.createSword(r.nextInt(this.length * 2 - 2) + 2);
                    case HELMET -> LootItem.createHelmet(r.nextInt(this.length) + 2);
                    case CHESTPLATE -> LootItem.createChestplate(r.nextInt(this.length) + 2);
                    case GAUNTLETS -> LootItem.createGauntlets(r.nextInt(this.length) + 2);
                    case LEGGINGS -> LootItem.createLeggings(r.nextInt(this.length) + 2);
                    case BOOTS -> LootItem.createBoots(r.nextInt(this.length) + 2);
                    case NONE -> throw new IllegalStateException("Unexpected Loot Type \"NONE\" in Adventure!");
                };

                this.rewardLoot.add(earned);
            }
            case EARN_CORE_STAT -> {
                final Random r = new Random();

                int rNum = r.nextInt(100);

                int statsEarned;
                if(rNum < 85) statsEarned = 1;
                else if(rNum < 95) statsEarned = 2;
                else if(rNum < 98) statsEarned = 3;
                else statsEarned = 4;

                List<Stat> stats = new ArrayList<>(Arrays.asList(Stat.values()));
                stats.subList(0, stats.size() - statsEarned).clear();

                for(Stat s : stats)
                {
                    rNum = r.nextInt(100);

                    int amount;
                    if(rNum < 90) amount = 1;
                    else if(rNum < 98) amount = 2;
                    else amount = 3;

                    this.rewardCoreStat.put(s, this.rewardCoreStat.getOrDefault(s, 0) + amount);
                }
            }
        }

        this.eventLog.add(event);
    }


    //Complete the Adventure and give out Rewards
    public void end()
    {
        if(this.eventLog.isEmpty()) this.executeEvent(AdventureEvent.EARN_XP);

        List<String> results = new ArrayList<>();

        if(this.rewardGold > 0)
        {
            this.player.addGold(this.rewardGold);
            results.add("**Gold:** `" + this.rewardGold + "`");
        }

        if(this.rewardXP > 0)
        {
            this.character.addExp(this.rewardXP);
            results.add("**Experience:** `" + this.rewardXP + "`");
        }

        if(!this.rewardLoot.isEmpty())
        {
            StringBuilder lootText = new StringBuilder();
            for(LootItem loot : this.rewardLoot)
            {
                loot.upload();
                this.player.addLootItem(loot.getLootID());

                lootText.append("`").append(loot.getName()).append("`, ");
            }

            lootText.deleteCharAt(lootText.length() - 1).deleteCharAt(lootText.length() - 1);

            results.add("**Loot:** " + lootText);
        }

        if(!this.rewardCoreStat.isEmpty())
        {
            for(Map.Entry<Stat, Integer> e : this.rewardCoreStat.entrySet()) this.character.increaseCoreStat(e.getKey(), e.getValue());
            results.add("**Improved Core Stats:** " + this.rewardCoreStat.keySet().stream().map(s -> Global.normalize(s.toString())).collect(Collectors.joining(", ")));
        }

        this.character.completeUpdate();

        StringBuilder finalResults = new StringBuilder();
        for(String s : results) finalResults.append(s).append("\n");
        finalResults.deleteCharAt(finalResults.length() - 1);

        this.player.DM("***Adventure Completed! Here's what***`" + this.character.getName() + "` ***earned:***\n" + finalResults);

        System.out.println("Event Log: " + this.eventLog + ", Results: " + results);
        Adventure.removeScheduler(this.character.getCharacterID());
        ADVENTURES.remove(this);
    }

    public static void removeScheduler(String ID)
    {
        if(END_TIMES.containsKey(ID)) END_TIMES.get(ID).cancel(true);
        END_TIMES.remove(ID);
    }

    public void setup(int length)
    {
        this.length = length;
        this.progress = 0;

        this.eventLog = new ArrayList<>();

        this.rewardGold = 0;
        this.rewardXP = 0;
        this.rewardLoot = new ArrayList<>();
        this.rewardCoreStat = new HashMap<>();
    }

    //Player
    private void setPlayer(PlayerDataQuery player)
    {
        this.player = player;
    }

    public static boolean isInAdventure(String ID)
    {
        return ID.chars().allMatch(Character::isDigit) ? ADVENTURES.stream().anyMatch(a -> a.player.getID().equals(ID)) : isCharacterOnAdventure(ID);
    }

    //Character
    private void setCharacter(RPGCharacter character)
    {
        this.character = character;
    }

    public static boolean isCharacterOnAdventure(String character)
    {
        return END_TIMES.containsKey(character);
    }

    public String getRemainingTime()
    {
        if(END_TIMES.containsKey(this.character.getCharacterID()))
        {
            long nextEvent = END_TIMES.get(this.character.getCharacterID()).getDelay(TimeUnit.SECONDS);
            int minutes = (int)(nextEvent / 60);
            int seconds = (int)(nextEvent % 60);

            return "\nNext Event in **" + minutes + "M " + seconds + "S**\nEvent Progress: " + this.progress + " / " + this.length + "\nTotal Time Required: **" + (this.length * 15) + "**M!";
        }
        else return "Complete!";
    }

    public static Adventure instance(String ID)
    {
        return Adventure.ADVENTURES.stream().filter(a -> a.character.getCharacterID().equals(ID)).toList().get(0);
    }

    //Events
    private enum AdventureEvent
    {
        EARN_GOLD(100),
        EARN_XP(75),
        EARN_LOOT(50),
        EARN_CORE_STAT(10);

        private final int weight;
        AdventureEvent(int weight) { this.weight = weight; }

        public static AdventureEvent getRandom()
        {
            List<AdventureEvent> pool = new ArrayList<>();
            for(AdventureEvent event : AdventureEvent.values()) for(int i = 0; i < event.weight; i++) pool.add(event);

            return pool.get(new Random().nextInt(pool.size()));
        }
    }
}

