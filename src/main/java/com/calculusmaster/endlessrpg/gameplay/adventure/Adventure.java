package com.calculusmaster.endlessrpg.gameplay.adventure;

import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.battle.enemy.EnemyBuilder;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.LootTag;
import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.gameplay.loot.LootBuilder;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;
import com.calculusmaster.endlessrpg.util.Global;
import com.calculusmaster.endlessrpg.util.helpers.LoggerHelper;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Adventure
{
    public static int ADVENTURE_EVENT_INTERVAL = 15 * 60;

    public static final List<Adventure> ADVENTURES = new ArrayList<>();
    public static final Map<String, ScheduledFuture<?>> END_TIMES = new HashMap<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(5);

    //Core
    private RPGCharacter character;
    private PlayerDataQuery player;
    private int length;
    private int progress;
    private int level;
    private Location location;

    private List<AdventureEvent> eventLog;

    //Rewards
    private int rewardGold;
    private int rewardXP;
    private List<LootItem> rewardLoot;
    private Map<Stat, Integer> rewardCoreStat;

    private Adventure() {}

    public static Adventure create(PlayerDataQuery player, int length, int level)
    {
        Adventure a = new Adventure();

        a.setCharacter(player.getActiveCharacter());
        a.setPlayer(player);
        a.setup(length, level);

        ADVENTURES.add(a);
        return a;
    }

    //Start the Adventure
    public void start()
    {
        ScheduledFuture<?> end = SCHEDULER.scheduleAtFixedRate(this::event, 0, ADVENTURE_EVENT_INTERVAL, TimeUnit.SECONDS);

        END_TIMES.put(this.character.getCharacterID(), end);
    }

    //Called periodically throughout the adventure, for random events to proc
    private void event()
    {
        if((this.eventLog.isEmpty() && this.progress == this.length - 1) || new SplittableRandom().nextInt(100) < 40)
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
            case EARN_GOLD -> this.rewardGold += (new SplittableRandom().nextInt(this.level) * 100 + this.level * 5);
            case EARN_XP -> this.rewardXP += (new SplittableRandom().nextInt(this.level) * 100 + this.level * 10);
            case EARN_LOOT -> {
                final int amount;
                if(this.level < 30) amount = 1;
                else if(this.level < 50) amount = 2;
                else if(this.level < 80) amount = 3;
                else amount = (int)(0.05 * this.level);

                for(int i = 0; i < amount; i++) this.rewardLoot.add(LootBuilder.create(LootType.getRandom(), this.level));
            }
            case EARN_CORE_STAT -> {
                final SplittableRandom r = new SplittableRandom();

                int rNum = r.nextInt(100);

                int statsEarned;
                if(rNum < 85) statsEarned = 1;
                else if(rNum < 95) statsEarned = 2;
                else if(rNum < 98) statsEarned = 3;
                else statsEarned = 4;

                List<Stat> stats = new ArrayList<>(Arrays.asList(Stat.values()));
                Collections.shuffle(stats);
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
            case BATTLE_ENEMY -> {
                final SplittableRandom r = new SplittableRandom();
                RPGCharacter enemy = this.location.getEnemyArchetype().create(this.level);

                boolean win = Battle.simulate(this.character, enemy, this.location);

                if(win)
                {
                    if(r.nextInt(100) < 50) this.rewardGold += (this.level * 150) * ((r.nextInt(100) + 1) / 100.0);
                    if(r.nextInt(100) < 25) this.rewardXP += (this.level * 200) * ((r.nextInt(100) + 1) / 100.0);
                    if(r.nextInt(100) < 30)
                    {
                        LootItem stolenLoot = LootItem.build(enemy.getEquipment().asList().get(r.nextInt(enemy.getEquipment().asList().size())));
                        LootItem.delete(stolenLoot.getLootID());
                        enemy.getEquipment().remove(stolenLoot.getLootID());

                        this.rewardLoot.add(stolenLoot);
                    }
                }
                else
                {
                    if(r.nextInt(100) < 25) this.rewardGold *= (r.nextInt(100) + 1) / 100.0;
                    if(r.nextInt(100) < 10) this.rewardXP *= (r.nextInt(100) + 1) / 100.0;
                }

                EnemyBuilder.delete(enemy);
            }
        }

        this.eventLog.add(event);
    }

    //Complete the Adventure and give out Rewards
    public void end()
    {
        if(this.eventLog.isEmpty()) this.executeEvent(AdventureEvent.EARN_XP);

        List<String> results = new ArrayList<>();

        //Must defeat Bot to receive rewards! Mini Boss is slightly more difficult than other enemies that appear in Adventures
        RPGCharacter miniBoss = this.location.getEnemyArchetype().create(this.level + 1);
        boolean win = Battle.simulate(this.character, miniBoss, this.location);

        if(win)
        {
            this.rewardGold *= 1.5;
            this.rewardXP *= 1.5;

            results.add("**Mini Boss:** `Battle Won`! Adventure Gold and XP rewards were boosted!");

            if(new SplittableRandom().nextInt(100) < 20)
            {
                String stolenLoot = miniBoss.getEquipment().asList().get(new SplittableRandom().nextInt(miniBoss.getEquipment().asList().size()));

                miniBoss.getEquipment().remove(stolenLoot);
                this.character.addLoot(stolenLoot);

                Executors.newSingleThreadExecutor().execute(() -> {
                    LootItem loot = LootItem.build(stolenLoot);
                    loot.addTag(LootTag.MINI_BOSS);
                    loot.updateTags();
                });

                results.add("*A piece of loot was stolen from the Mini Boss!*");
            }
        }
        else
        {
            this.rewardGold *= 0.5;
            this.rewardXP *= 0.5;

            this.rewardLoot = new ArrayList<>();
            this.rewardCoreStat = new HashMap<>();

            //TODO: Add extra negative effects for losing to Mini Boss at higher levels (?)
            results.add("**Mini Boss:** `Battle Lost`! Adventure Gold and XP rewards were reduced! Other earnings were surrendered to the Mini Boss...");
        }

        EnemyBuilder.delete(miniBoss);

        if(this.rewardGold > 0)
        {
            this.character.addGold(this.rewardGold);
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
                this.character.addLoot(loot.getLootID());

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

        this.player.DM("***Adventure Completed! Here's what \"" + this.character.getName() + "\" earned:***\n" + finalResults);

        System.out.println("Event Log: " + this.eventLog + ", Results: " + results);
        Adventure.removeScheduler(this.character.getCharacterID());
        ADVENTURES.remove(this);
    }

    public static void removeScheduler(String ID)
    {
        if(END_TIMES.containsKey(ID)) END_TIMES.get(ID).cancel(true);
        END_TIMES.remove(ID);
    }

    public void setup(int length, int level)
    {
        this.length = length;
        this.progress = 0;

        this.level = level;

        this.location = Realm.CURRENT.getLocation(this.player.getLocationID());

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

    public Location getLocation()
    {
        return this.location;
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

            if(nextEvent < 0)
            {
                LoggerHelper.error(this.getClass(), "Adventure failed to complete! Event Log{%s}, Length{%s}, Progress{%s}, Level{%s}, Scheduler{%s}".formatted(this.eventLog, this.length, this.progress, this.level, END_TIMES.get(this.character.getCharacterID())));
                try {
                    END_TIMES.get(this.character.getCharacterID()).get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                Adventure.removeScheduler(this.character.getCharacterID());
                ADVENTURES.remove(this);
                return "An error occurred with your Adventure! Forcefully ending it.";
            }

            return "\nNext Event in **" + minutes + "M " + seconds + "S**\nEvent Progress: " + this.progress + " / " + this.length + "\nTotal Time Required: **" + (this.length * ADVENTURE_EVENT_INTERVAL) + "**S!";
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
        EARN_CORE_STAT(10),
        BATTLE_ENEMY(60);

        private final int weight;
        AdventureEvent(int weight) { this.weight = weight; }

        public static AdventureEvent getRandom()
        {
            List<AdventureEvent> pool = new ArrayList<>();
            for(AdventureEvent event : AdventureEvent.values()) for(int i = 0; i < event.weight; i++) pool.add(event);

            return pool.get(new SplittableRandom().nextInt(pool.size()));
        }
    }
}

