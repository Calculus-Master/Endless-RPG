package com.calculusmaster.endlessrpg.gameplay.battle.dungeon;

import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.map.CoreMapGenerator;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.map.DungeonMap;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.room.DungeonRoom;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.util.Coordinate;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.util.Direction;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.gameplay.resources.container.RawResourceContainer;
import com.calculusmaster.endlessrpg.gameplay.resources.enums.RawResource;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Dungeon
{
    public static final List<Dungeon> DUNGEONS = new ArrayList<>();

    private List<DungeonPlayer> players;
    private DungeonPlayer leader;
    private LinkedHashMap<String, Boolean> acceptedPlayers;

    private Location location;
    private MessageReceivedEvent event;

    private DungeonStatus status;
    private DungeonReward reward;
    private DungeonContributions contributions;

    private int level;
    private DungeonMap map;
    private Coordinate position;

    private EnumSet<DungeonMetaTag> tags;
    private List<String> result;

    private static final List<String> PLAYERS_ON_COOLDOWN = new ArrayList<>();
    private static final Map<String, ScheduledFuture<?>> PLAYER_COOLDOWN_FUTURES = new HashMap<>();

    public static Dungeon create(Location location, MessageReceivedEvent event, PlayerDataQuery leader, List<PlayerDataQuery> others)
    {
        Dungeon d = new Dungeon();

        d.setLocation(location);
        d.setEvent(event);
        d.setPlayers(leader, others);

        d.setup();
        DUNGEONS.add(d);
        return d;
    }

    //Main

    //Start an encounter - can either be completed immediately, wait for interaction, or finish through a different framework (such as a battle)
    private void startEncounter()
    {
        switch(this.current().getType())
        {
            case TREASURE -> this.sendChoiceEmbed();
            default -> Global.delay(() -> this.current().execute(-1), 5, TimeUnit.SECONDS);
        }
    }

    //To complete encounters that require user interaction
    public void submitChoice(int choice)
    {
        Global.delay(() -> this.current().execute(choice), 5, TimeUnit.SECONDS);

        this.tags.remove(DungeonMetaTag.AWAITING_INTERACTION);
    }

    public boolean isChoiceValid(int choice)
    {
        if(!this.hasTag(DungeonMetaTag.AWAITING_INTERACTION)) throw new IllegalStateException("No Interaction is active!");
        else return this.current().isChoiceValid(choice);
    }

    public void completeCurrentRoom()
    {
        this.current().complete();

        this.sendEmbed(DungeonEmbed.ENCOUNTER);

        if(this.current().getType().equals(DungeonRoom.RoomType.BOSS)) this.win();
    }

    public void start()
    {
        this.status = DungeonStatus.ADVENTURING;

        this.players.stream().map(dp -> dp.party).forEach(party -> party.forEach(c -> this.contributions.init(c)));

        this.sendEmbed(DungeonEmbed.ENCOUNTER);
    }

    public void move(Direction direction)
    {
        this.result.clear();

        this.position = this.position.shift(direction);

        this.sendEmbed(DungeonEmbed.ENCOUNTER);

        if(!this.current().isComplete()) this.startEncounter();
    }

    public DungeonRoom current()
    {
        return this.map.getRoom(this.position);
    }

    public <R extends DungeonRoom> R current(Class<R> clazz)
    {
        return clazz.cast(this.current());
    }

    public void win()
    {
        this.status = DungeonStatus.COMPLETE;

        final EmbedBuilder embed = new EmbedBuilder();
        final ExecutorService manager = Executors.newCachedThreadPool();

        List<RPGCharacter> characters = new ArrayList<>();
        this.players.stream().map(dp -> dp.party).forEach(list -> Collections.synchronizedList(characters).addAll(list));

        //Gold and XP Distribution
        List<String> contributionResults = new ArrayList<>();
        double contributionSum = this.contributions.scores.values().stream().mapToInt(i -> i).sum();
        for(RPGCharacter c : Collections.synchronizedList(characters))
        {
            double contributionPercentage = (double)this.contributions.scores.get(c.getCharacterID()) / contributionSum;

            int shareGold = (int)(this.reward.gold * contributionPercentage);
            int shareXP = (int)(this.reward.gold * contributionPercentage);

            c.addGold(shareGold);
            c.addExp(shareXP);

            manager.execute(() -> {
                if(shareGold != 0) c.updateGold();
                if(shareXP != 0) c.updateExperience();
            });

            contributionResults.add("%s – Gold: %s, Experience: %s".formatted(c.getName(), shareGold, shareXP));
        }

        final SplittableRandom random = new SplittableRandom();
        final DungeonPlayerContributions playerContributions = new DungeonPlayerContributions(this, this.contributions);

        Map<DungeonPlayer, List<String>> lootContributionResults = new HashMap<>();
        this.players.forEach(p -> Collections.synchronizedMap(lootContributionResults).put(p, new ArrayList<>()));

        //Loot Distribution
        List<LootItem> lootPool = new ArrayList<>(List.copyOf(this.reward.loot));
        while(lootPool.size() > 0)
        {
            LootItem loot = lootPool.get(random.nextInt(lootPool.size()));
            DungeonPlayer player = playerContributions.pullWeighted();

            manager.execute(() -> player.data.addLootItem(loot.getLootID()));
            lootContributionResults.get(player).add(loot.getName());

            lootPool.remove(loot);
        }

        Map<DungeonPlayer, List<String>> resourceContributionResults = new HashMap<>();
        this.players.forEach(p -> Collections.synchronizedMap(resourceContributionResults).put(p, new ArrayList<>()));

        //Resource Distribution
        for(RawResource r : Arrays.stream(RawResource.values()).filter(r -> this.reward.resources.has(r)).toList())
        {
            int total = this.reward.resources.get(r);

            this.players.forEach(p -> {
                int amount = (int)(total * playerContributions.getPercent(p));
                if(amount > 0)
                {
                    manager.execute(() -> p.data.addResource(r, amount));
                    resourceContributionResults.get(p).add(Global.normalize(r.toString()) + "(" + amount + ")");
                }
            });
        }

        //Embed Reward Fields

        String characterRewards = String.join("\n", contributionResults);
        final String description = "Level " + this.level + " Dungeon `" + this.location.getName() + "`\nCompletion: " + (int)(this.map.getCompletion() * 100) + "%";

        final StringBuilder playerRewardsLoot = new StringBuilder();
        for(Map.Entry<DungeonPlayer, List<String>> entry : lootContributionResults.entrySet().stream().filter(e -> !e.getValue().isEmpty()).toList())
            playerRewardsLoot.append(entry.getKey().data.getUsername()).append(": ").append(String.join(", ", entry.getValue())).append("\n");

        final StringBuilder playerRewardsResource = new StringBuilder();
        for(Map.Entry<DungeonPlayer, List<String>> entry : resourceContributionResults.entrySet().stream().filter(e -> !e.getValue().isEmpty()).toList())
            playerRewardsResource.append(entry.getKey().data.getUsername()).append(": ").append(String.join(", ", entry.getValue())).append("\n");

        //Empty Checks
        if(characterRewards.isEmpty()) characterRewards = "None";
        if(playerRewardsLoot.isEmpty()) playerRewardsLoot.append("None");
        if(playerRewardsResource.isEmpty()) playerRewardsResource.append("None");

        embed
                .setTitle("Victory!")
                .setDescription(description)
                .addField("Character Rewards", characterRewards, false)
                .addField("Player Rewards - Loot", playerRewardsLoot.toString(), false)
                .addField("Player Rewards - Resources", playerRewardsResource.toString(), false);

        this.event.getChannel().sendMessageEmbeds(embed.build()).queue();

        Dungeon.delete(this.leader.data.getID());

        this.addCooldowns();
    }

    public void fail()
    {
        this.status = DungeonStatus.COMPLETE;

        final EmbedBuilder embed = new EmbedBuilder();

        embed
                .setTitle("Loss")
                .setDescription("Level " + this.level + " Dungeon `" + this.location.getName() + "`\nCompletion: " + (int)(this.map.getCompletion() * 100) + "%\n**All rewards were lost to time... Hopefully future adventures fare better!");

        this.event.getChannel().sendMessageEmbeds(embed.build()).queue();

        Dungeon.delete(this.leader.data.getID());

        this.addCooldowns();
    }

    //Embed-Related
    public void sendEmbed(DungeonEmbed type)
    {
        this.event.getChannel().sendMessageEmbeds((switch(type) {
            case START -> Embed_StartDungeon(new EmbedBuilder());
            case MAP_EXPLORED -> Embed_MapExplorationProgress(new EmbedBuilder());
            case ENCOUNTER -> Embed_Encounter(new EmbedBuilder());
        }).build()).queue();
    }

    private void sendChoiceEmbed(List<String> choices)
    {
        EmbedBuilder embed = new EmbedBuilder();

        StringBuilder desc = new StringBuilder("**Select a choice to continue:**\n");
        for(int i = 0; i < choices.size(); i++) desc.append("`").append(i + 1).append("`. *").append(choices.get(i)).append("*\n");

        embed
                .setTitle(this.location.getName())
                .setDescription(desc.toString())
                .setFooter("Be careful with your choice! Anything can happen...");

        this.event.getChannel().sendMessageEmbeds(embed.build()).queue();

        this.tags.add(DungeonMetaTag.AWAITING_INTERACTION);
    }

    private void sendChoiceEmbed()
    {
        this.sendChoiceEmbed(this.current().getChoiceDescription());
    }

    public String getExploredMap()
    {
        String[][] codedMap = new String[this.map.rows][this.map.columns];

        for(int r = 0; r < this.map.rows; r++)
        {
            for(int c = 0; c < this.map.columns; c++)
            {
                DungeonRoom room = this.map.getRoom(Coordinate.of(r, c));

                if(room == null) codedMap[r][c] = "X";
                else if(Coordinate.of(r, c).equals(this.position)) codedMap[r][c] = "☆";
                else if(!room.isComplete()) codedMap[r][c] = "?";
                else codedMap[r][c] = room.getType() == null ? "/" : room.getType().code();
            }
        }

        StringBuilder out = new StringBuilder();
        for(String[] rows : codedMap) out.append(
                Arrays.toString(rows).replaceAll("([\\[\\]])", "|").replaceAll(",", "")).append("\n");

        System.out.println(out);
        return out.toString();
    }

    private String getDefaultEmbedTitle()
    {
        return this.location.getName() + " (Level %s)".formatted(this.level);
    }

    public EmbedBuilder Embed_MapExplorationProgress(EmbedBuilder embed)
    {
        return embed
                .setTitle(this.getDefaultEmbedTitle() + " -  Exploration Progress")
                .setDescription(this.getExploredMap())
                .addField("Map Code Legend", "Each letter on the map denotes the type of room present at that location.\n" +
                        "   X - Nothing\n" +
                        "   ? - Undiscovered\n" +
                        "   ☆ - Current Position\n" +
                        "   S - Spawn\n" +
                        "   B - Boss\n" +
                        "   T - Treasure\n" +
                        "   / - Error (You should not see this code anywhere. If you do, report it!)", false)
                .setFooter("Rooms marked with ??? have either not been explored or are not part of the map!");
    }

    public EmbedBuilder Embed_StartDungeon(EmbedBuilder embed)
    {
        String lore = "*You descend into the depths...*";

        List<String> tips = new ArrayList<>(List.of(
                "The Dungeon Leader makes the decisions for the group of where to travel and what to do in encounters.",
                "Be careful with your decisions, they may change the outcome of your adventure!",
                "In order to successfully leave the Dungeon, the boss must be defeated.",
                "Once all player characters have been defeated, you automatically lose the Dungeon and leave without any rewards.",
                "If you succeed, rewards will be distributed among all characters based on their \"contribution\" to your adventure."
        ));

        tips.set(0, "- " + tips.get(0));

        return embed
                .setTitle(this.getDefaultEmbedTitle())
                .setDescription(lore)
                .addField("Size", this.map.getSize() + " Rooms", true)
                .addField("Leader", this.leader.data.getUsername(), true)
                .addField("Allies", this.players.size() == 1 ? "None" : this.players.stream().filter(dp -> !dp.data.getID().equals(this.leader.data.getID())).map(dp -> dp.data.getUsername()).collect(Collectors.joining(", ")), true)
                .addField("Tips", String.join("\n- ", tips), false);
    }

    public EmbedBuilder Embed_Encounter(EmbedBuilder embed)
    {
        return embed
                .setTitle(this.getDefaultEmbedTitle())
                .setDescription(this.current().getType().equals(DungeonRoom.RoomType.SPAWN) ? "*This is the room where you entered the Dungeon.*" : (this.current().isComplete() ? "*This room has been completed.*\n\n" + String.join(" ", this.result) : "*" + this.current().getDescription() + "*"))
                .addField("Room Type", Global.normalize(this.current().getType().toString()), true);
    }

    //Utilities
    private boolean isReady()
    {
        return this.acceptedPlayers.values().stream().allMatch(b -> b);
    }

    public void setPlayerAccepted(String ID, boolean value)
    {
        if(value)
        {
            this.acceptedPlayers.put(ID, true);

            if(this.isReady())
            {
                this.event.getChannel().sendMessage("You are entering `" + this.location.getName() + "`!" + this.players.stream().map(dp -> dp.data).map(PlayerDataQuery::getMention).collect(Collectors.joining(" "))).queue();

                Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                    this.sendEmbed(DungeonEmbed.START);
                    this.start();
                }, 5, TimeUnit.SECONDS);
            }
        }
        else
        {
            if(ID.equals(this.leader.data.getID())) throw new IllegalStateException("Leader cannot un-accept their own Dungeon!");
            this.acceptedPlayers.remove(ID);
            this.players.removeIf(dp -> dp.data.getID().equals(ID));
        }
    }

    private void setup()
    {
        //Default Map (Level = 0): [Min 9 | Max 49]
        this.map = new DungeonMap(new CoreMapGenerator(3, 3 + this.location.getLevel()));
        this.map.completeRoomSetup(this);

        this.position = this.map.getSpawn();

        this.status = DungeonStatus.WAITING_FOR_PLAYERS;
        this.reward = new DungeonReward();
        this.contributions = new DungeonContributions();

        this.tags = EnumSet.noneOf(DungeonMetaTag.class);
    }

    //Cooldowns
    private void addCooldowns()
    {
        for(DungeonPlayer player : this.players) Dungeon.addDungeonCooldown(player.data.getID());
    }

    public static void addDungeonCooldown(String player)
    {
        Collections.synchronizedList(PLAYERS_ON_COOLDOWN).add(player);
        ScheduledFuture<?> cooldown = Executors.newSingleThreadScheduledExecutor().schedule(() -> Dungeon.removeDungeonCooldown(player), 1, TimeUnit.HOURS);
        Collections.synchronizedMap(PLAYER_COOLDOWN_FUTURES).put(player, cooldown);
    }

    public static void removeDungeonCooldown(String player)
    {
        Collections.synchronizedList(PLAYERS_ON_COOLDOWN).remove(player);
        Collections.synchronizedMap(PLAYER_COOLDOWN_FUTURES).remove(player);
    }

    public static boolean isOnCooldown(String player)
    {
        return Collections.synchronizedList(PLAYERS_ON_COOLDOWN).contains(player);
    }

    public static String getDungeonCooldown(String player)
    {
        long total = Collections.synchronizedMap(PLAYER_COOLDOWN_FUTURES).get(player).getDelay(TimeUnit.SECONDS);

        int hours = (int)total / 3600;
        int minutes = ((int)total % 3600) / 60;
        int seconds = ((int)total % 3600) % 60;

        return "`%sH %sM %sS`".formatted(hours, minutes, seconds);
    }

    //Core Accessors

    public void addResult(String result)
    {
        this.result.add(result);
    }

    public DungeonReward reward()
    {
        return this.reward;
    }

    public DungeonContributions contributions()
    {
        return this.contributions;
    }

    public boolean isValidLocation(Coordinate target)
    {
        return (target.row >= 0 && target.row < this.map.rows) && (target.column >= 0 && target.column < this.map.columns) && this.map.getRoom(target) != null;
    }

    private DungeonPlayer getPlayer(String ID)
    {
        return this.players.stream().filter(dp -> dp.data.getID().equals(ID)).findFirst().orElseThrow(() -> {throw new IllegalStateException("Couldn't find player of ID %s in Dungeon!".formatted(ID));});
    }

    public MessageReceivedEvent getEvent()
    {
        return this.event;
    }

    public boolean hasTag(DungeonMetaTag tag)
    {
        return this.tags.contains(tag);
    }

    public void addTag(DungeonMetaTag tag)
    {
        this.tags.add(tag);
    }

    public void removeTag(DungeonMetaTag tag)
    {
        this.tags.remove(tag);
    }

    public DungeonMap getMap()
    {
        return this.map;
    }

    public Coordinate getPosition()
    {
        return this.position;
    }

    public int getLevel()
    {
        return this.level;
    }

    public List<DungeonPlayer> getPlayers()
    {
        return this.players;
    }

    public DungeonPlayer getLeader()
    {
        return this.leader;
    }

    public String getName()
    {
        return this.location.getName();
    }

    public DungeonStatus getStatus()
    {
        return this.status;
    }

    public Location getLocation()
    {
        return this.location;
    }

    private void setLocation(Location location)
    {
        this.location = location;
    }

    private void setEvent(MessageReceivedEvent event)
    {
        this.event = event;
    }

    private void setPlayers(PlayerDataQuery leader, List<PlayerDataQuery> others)
    {
        this.leader = new DungeonPlayer(leader);

        this.players = new ArrayList<>(others.stream().map(DungeonPlayer::new).toList());
        this.players.add(0, this.leader);

        this.acceptedPlayers = new LinkedHashMap<>();
        this.players.forEach(dp -> this.acceptedPlayers.put(dp.data.getID(), false));
        this.acceptedPlayers.put(this.leader.data.getID(), true);

        this.level = this.players.stream().mapToInt(dp -> dp.level + this.location.getLevel()).sum() / this.players.size();
        this.result = new ArrayList<>();
    }

    //Interfacers

    public static boolean isInDungeon(String ID)
    {
        return Dungeon.instance(ID) != null;
    }

    public static Dungeon instance(String ID)
    {
        return DUNGEONS.stream().filter(b -> b.players.stream().anyMatch(dp -> dp.data.getID().equals(ID))).findFirst().orElse(null);
    }

    public static void delete(String ID)
    {
        DUNGEONS.removeIf(d -> d.players.stream().anyMatch(dp -> dp.data.getID().equals(ID)));
    }

    //Internal

    public enum DungeonStatus
    {
        WAITING_FOR_PLAYERS,
        ADVENTURING,
        COMPLETE;
    }

    public enum DungeonEmbed
    {
        START,
        MAP_EXPLORED,
        ENCOUNTER,
        //TODO: More DungeonEmbed types
    }

    public enum DungeonMetaTag
    {
        AWAITING_BATTLE_RESULTS,
        AWAITING_INTERACTION;
    }

    public static class DungeonPlayerContributions
    {
        private LinkedHashMap<DungeonPlayer, Integer> scores;

        DungeonPlayerContributions(Dungeon dungeon, DungeonContributions characterScores)
        {
            dungeon.getPlayers().forEach(player -> Collections.synchronizedMap(this.scores).put(player, player.party.stream().mapToInt(c -> Collections.synchronizedMap(characterScores.scores).getOrDefault(c.getCharacterID(), 0)).sum()));
        }

        public DungeonPlayer pullWeighted()
        {
            List<DungeonPlayer> playerPool = new ArrayList<>();
            this.scores.forEach((player, score) -> {for(int i = 0; i < score; i++) playerPool.add(player);});
            Collections.shuffle(playerPool);

            return playerPool.get(0);
        }

        public double getPercent(DungeonPlayer player)
        {
            return (double)(this.scores.get(player)) / this.scores.values().stream().mapToInt(i -> i).sum();
        }
    }

    public static class DungeonContributions
    {
        private LinkedHashMap<String, Integer> scores;

        {
            scores = new LinkedHashMap<>();
        }

        public void increase(String character, int amount)
        {
            this.scores.put(character, this.scores.getOrDefault(character, 0) + amount);
        }

        public void increase(int amount)
        {
            this.scores.forEach((character, score) -> this.increase(character, amount));
        }

        public void init(RPGCharacter c)
        {
            this.scores.put(c.getCharacterID(), 0);
        }
    }

    public static class DungeonReward
    {
        public int gold;
        public int xp;
        public List<LootItem> loot;
        public RawResourceContainer resources;

        {
            this.gold = 0;
            this.xp = 0;
            this.loot = new ArrayList<>();
            this.resources = new RawResourceContainer();
        }
    }

    public static class DungeonPlayer
    {
        public PlayerDataQuery data;
        public List<RPGCharacter> party;
        public int level;

        DungeonPlayer(PlayerDataQuery data)
        {
            this.data = data;
            this.party = new ArrayList<>(this.data.getParty().stream().map(RPGCharacter::build).toList());
            this.party.forEach(c -> c.forBattle(null));
            this.level = this.party.stream().mapToInt(RPGCharacter::getLevel).sum() / this.party.size();
        }
    }
}
