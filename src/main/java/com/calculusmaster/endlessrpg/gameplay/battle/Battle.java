package com.calculusmaster.endlessrpg.gameplay.battle;

import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.Dungeon;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.NewDungeon;
import com.calculusmaster.endlessrpg.gameplay.battle.player.AIPlayer;
import com.calculusmaster.endlessrpg.gameplay.battle.player.AbstractPlayer;
import com.calculusmaster.endlessrpg.gameplay.battle.player.UserPlayer;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.gameplay.spell.Spell;
import com.calculusmaster.endlessrpg.gameplay.spell.spells.StrikeSpell;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.stream.Collectors;

public class Battle
{
    public static final List<Battle> BATTLES = new ArrayList<>();

    private BattleType battleType;
    private List<AbstractPlayer> players;
    private Location location;

    private RPGCharacter[] battlers;
    private int turn;
    private List<String> turnResults;

    private Optional<MessageReceivedEvent> event = Optional.empty();

    private boolean isDungeonBattle;

    //Creators

    public static Battle createPVP(String p1ID, String p2ID)
    {
        Battle b = new Battle();

        b.setBattleType(BattleType.PVP);
        b.createPlayers(p1ID, p2ID);
        b.setLocation(Realm.CURRENT.getHub()); //TODO: Where should PvP Battles occur?
        b.setup();

        BATTLES.add(b);
        return b;
    }

    @Deprecated
    public static Battle createPVE(String userID, Location location)
    {
        Battle b = new Battle();

        b.setBattleType(BattleType.PVE);
        b.createPlayers(userID);
        b.setLocation(location);
        b.setup();

        BATTLES.add(b);
        return b;
    }

    public static Battle createDungeon(UserPlayer player, AIPlayer enemy, Location location)
    {
        Battle b = new Battle();

        player.team.forEach(c -> c.setOwner(player));

        b.setBattleType(BattleType.PVE);
        b.createPlayers(player, enemy);
        b.setLocation(location);
        b.setup();

        b.isDungeonBattle = true;
        BATTLES.add(b);
        return b;
    }

    public static Battle createDungeon(List<UserPlayer> players, AIPlayer enemy, Location location)
    {
        Battle b = new Battle();

        players.forEach(p -> p.team.forEach(c -> c.setOwner(p)));

        b.setBattleType(BattleType.PVE);
        b.createPlayers(players, enemy);
        b.setLocation(location);
        b.setup();

        b.isDungeonBattle = true;
        BATTLES.add(b);
        return b;
    }

    public static boolean simulate(RPGCharacter characterPlayer, RPGCharacter characterAI, Location location)
    {
        Battle b = new Battle();

        b.setBattleType(BattleType.PVE);
        b.setLocation(location);

        b.players = new ArrayList<>();
        AIPlayer player = new AIPlayer(characterPlayer);
        AIPlayer AI = new AIPlayer(characterAI);
        b.players.add(player);
        b.players.add(AI);

        b.setup();

        while(!b.isComplete()) b.submitAITurn();

        return b.getWinner().stream().anyMatch(p -> p.ID.equals(player.ID));
    }

    //External

    public static boolean isInBattle(String ID)
    {
        return BATTLES.stream().anyMatch(b -> b.getPlayers().stream().anyMatch(p -> p.ID.equals(ID)));
    }

    public static Battle instance(String ID)
    {
        return Battle.isInBattle(ID) ? BATTLES.stream().filter(b -> b.getPlayers().stream().anyMatch(p -> p.ID.equals(ID))).collect(Collectors.toList()).get(0) : null;
    }

    public static void delete(String ID)
    {
        int index = -1;
        for(int i = 0; i < BATTLES.size(); i++) if(BATTLES.get(i).getPlayers().stream().anyMatch(p -> p.ID.equals(ID))) index = i;
        if(index != -1) BATTLES.remove(index);
    }

    //Battle

    public void submitTurn(int target, Spell spell)
    {
        //TODO: Spell system (different types of attacks, different types of effects)

        String spellResult = spell.execute(this.battlers[this.turn], this.battlers[target], this.battlers, this);

        this.turnResults.add(spellResult);

        this.advanceTurn();
    }

    //Embeds
    public void sendEndEmbed()
    {
        this.sendTurnEmbed();

        EmbedBuilder embed = new EmbedBuilder();

        List<AbstractPlayer> winners = this.getWinner();

        embed.setDescription(winners.size() == 1 ? winners.get(0).getName() + " has won!" : winners.stream().map(AbstractPlayer::getName).collect(Collectors.joining(", ")));

        this.sendEmbed(embed);

        if(this.isDungeonBattle)
        {
            List<UserPlayer> userPlayers = this.players.stream().filter(p -> p instanceof UserPlayer).map(p -> (UserPlayer)p).toList();
            NewDungeon dungeon = NewDungeon.instance(userPlayers.get(0).ID);

            if(winners.stream().allMatch(winner -> userPlayers.stream().anyMatch(p -> winner.ID.equals(p.ID))))
            {
                dungeon.addResult("You defeated all the enemies!");
                dungeon.completeCurrentRoom();
            }
            else dungeon.fail();

            dungeon.removeTag(NewDungeon.DungeonMetaTag.AWAITING_BATTLE_RESULTS);
        }

        for(AbstractPlayer player : this.players)
        {
            if(!player.team.get(0).isAI() && Dungeon.isInDungeon(player.ID))
            {
                Dungeon d = Objects.requireNonNull(Dungeon.instance(player.ID));

                if(winners.contains(player))
                {
                    d.addResult("You defeated all the enemies!");
                    d.advance();
                }
                else d.fail();
            }
        }

        //TODO: Battle win rewards

        BATTLES.remove(this);
    }

    public void sendTurnEmbed()
    {
        EmbedBuilder embed = new EmbedBuilder();

        final StringBuilder desc = new StringBuilder();

        List<Integer> battlerDescOrder = new ArrayList<>();
        for(int i = this.turn; i < this.battlers.length; i++) battlerDescOrder.add(i);
        for(int i = 0; i < this.turn; i++) battlerDescOrder.add(i);

        for(int i : battlerDescOrder)
        {
            RPGCharacter b = this.battlers[i];

            String overview = "(" + (i + 1) + ") " + (!b.isAI() ? b.getOwner().getName() + "'s " : "") + "**\"" + b.getName() + "\"**: ";
            if(b.isDefeated()) overview += "DEFEATED";
            else overview += "%s / %s HEALTH".formatted(b.getHealth(), b.getStat(Stat.HEALTH));

            desc.append(overview).append("\n\n");
        }

        if(!this.turnResults.isEmpty())
        {
            desc.append("**Turn Outcome:**\n");
            for(String s : this.turnResults) desc.append(s).append("\n");
        }

        embed.setDescription(desc.toString());
        if(!this.isComplete()) embed.setFooter("It's now " + this.battlers[!this.turnResults.isEmpty() ? (this.nextValidCharacter()) : 0].getName() + "'s turn!");

        this.sendEmbed(embed);

        if(this.turnResults.isEmpty() && this.battlers[0].isAI()) this.submitAITurn();
    }

    private void sendEmbed(EmbedBuilder embed)
    {
        this.event.ifPresent(event -> event.getChannel().sendMessageEmbeds(embed.build()).queue());
    }

    //Common Utilities
    public boolean isComplete()
    {
        List<RPGCharacter> remaining = Arrays.stream(this.battlers).filter(c -> !c.isDefeated()).toList();

        if(this.battleType.equals(BattleType.PVP))
        {
            String owner = remaining.get(0).getOwnerID();
            return remaining.stream().allMatch(c -> c.getOwnerID().equals(owner)) || remaining.stream().noneMatch(c -> c.getOwnerID().equals(owner));
        }
        else if(this.battleType.equals(BattleType.PVE)) return remaining.stream().allMatch(RPGCharacter::isAI) || remaining.stream().noneMatch(RPGCharacter::isAI);
        else throw new NullPointerException("Battle Type is null!");
    }

    private List<AbstractPlayer> getWinner()
    {
        if(!this.isComplete()) throw new IllegalStateException("Battle must be complete!");

        List<RPGCharacter> remaining = Arrays.stream(this.battlers).filter(c -> !c.isDefeated()).toList();

        return switch(this.battleType) {
            case PVP -> new ArrayList<>(List.of(
                            Arrays.stream(this.battlers)
                            .findFirst()
                            .get()
                            .getOwner())
            );
            case PVE -> remaining.get(0).isAI()
                    ? List.of(remaining.get(0).getOwner())
                    : new ArrayList<>(
                            Arrays.stream(this.battlers)
                                    .filter(c -> !c.isAI())
                                    .map(RPGCharacter::getOwner)
                                    .distinct()
                                    .toList()
            );
        };
    }

    private void advanceTurn()
    {
        if(this.isComplete()) this.sendEndEmbed();
        else
        {
            this.sendTurnEmbed();

            this.turn = this.nextValidCharacter();

            this.turnResults.clear();

            //TODO: AI Decisions made here
            if(this.battlers[this.turn].isAI()) this.submitAITurn();
        }
    }

    private int nextValidCharacter()
    {
        int turn = this.turn;

        do
        {
            turn++;

            if(turn >= this.battlers.length)
            {
                this.setBattlerTurnOrder();
                turn = 0;
            }
        } while(this.battlers[turn].isDefeated());

        return turn;
    }

    private void submitAITurn()
    {
        List<Integer> possibleTargets = new ArrayList<>();

        for(int i = 0; i < this.battlers.length; i++)
            if(!this.battlers[i].isDefeated() && this.turn != i && !this.battlers[i].isOwnedBy(this.battlers[this.turn].getOwnerID()) )
                possibleTargets.add(i);

            //TODO: Random spell from list of spells
        this.submitTurn(possibleTargets.get(new Random().nextInt(possibleTargets.size())), new StrikeSpell());
    }

    //Battle Setup - Common
    private void setBattleType(BattleType type)
    {
        this.battleType = type;
    }

    //Misc. Setup and Initializations (Common)
    private void setup()
    {
        this.turn = 0;
        this.turnResults = new ArrayList<>();
        this.setBattlerTurnOrder();
        this.isDungeonBattle = false;
    }

    private void setBattlerTurnOrder()
    {
        this.battlers = new RPGCharacter[this.players.stream().mapToInt(c -> c.team.size()).sum()];

        List<RPGCharacter> pool = new ArrayList<>();
        for(AbstractPlayer player : this.players) pool.addAll(player.team);
        pool.sort(Comparator.comparingInt(c -> c.getStat(Stat.SPEED)));
        Collections.reverse(pool);

        for(int i = 0; i < this.battlers.length; i++) this.battlers[i] = pool.get(i);
    }

    private void setLocation(Location location)
    {
        this.location = location;
    }

    public Location getLocation()
    {
        return this.location;
    }

    //Battle Setup - PvP
    private void createPlayers(String p1ID, String p2ID)
    {
        this.players = new ArrayList<>();
        this.players.add(new UserPlayer(p1ID));
        this.players.add(new UserPlayer(p2ID));
    }

    //Battle Setup - PvE
    private void createPlayers(String userID)
    {
        this.players = new ArrayList<>();
        this.players.add(new UserPlayer(userID));
        this.players.add(new AIPlayer(RPGCharacter.create("Bot")));
    }

    private void createPlayers(String userID, AIPlayer enemy)
    {
        this.players = new ArrayList<>();
        this.players.add(new UserPlayer(userID));
        this.players.add(enemy);
    }

    private void createPlayers(UserPlayer player, AIPlayer enemy)
    {
        this.players = new ArrayList<>();
        this.players.add(player);
        this.players.add(enemy);
    }

    private void createPlayers(List<UserPlayer> players, AIPlayer enemy)
    {
        this.players = new ArrayList<>(players);
        this.players.add(enemy);
    }

    //Accessors
    public void setEvent(MessageReceivedEvent event)
    {
        this.event = Optional.of(event);
    }

    public BattleType getBattleType()
    {
        return this.battleType;
    }

    public List<AbstractPlayer> getPlayers()
    {
        return this.players;
    }

    public RPGCharacter[] getBattlers()
    {
        return this.battlers;
    }

    public RPGCharacter getCurrentCharacter()
    {
        return this.battlers[this.turn];
    }

    private enum BattleType
    {
        PVP,
        PVE;
    }
}
