package com.calculusmaster.endlessrpg.gameplay.battle;

import com.calculusmaster.endlessrpg.gameplay.battle.player.AIPlayer;
import com.calculusmaster.endlessrpg.gameplay.battle.player.AbstractPlayer;
import com.calculusmaster.endlessrpg.gameplay.battle.player.UserPlayer;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.*;
import java.util.stream.Collectors;

public class Battle
{
    public static final List<Battle> BATTLES = new ArrayList<>();

    private BattleType battleType;
    private List<AbstractPlayer> players;

    private RPGCharacter[] battlers;
    private int turn;
    private List<String> turnResults;

    //Creators

    public static Battle createPVP(String p1ID, String p2ID)
    {
        Battle b = new Battle();

        b.setBattleType(BattleType.PVP);
        b.createPlayers(p1ID, p2ID);
        b.setup();

        return b;
    }

    public static Battle createPVE(String userID)
    {
        Battle b = new Battle();

        b.setBattleType(BattleType.PVE);
        b.createPlayers(userID);
        b.setup();

        return b;
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

    public void submitTurn(int target)
    {
        //TODO: Spell system (different types of attacks, different types of effects)
        //TODO: Temporary just does damage
        int damage = this.battlers[this.turn].getDamage(this.battlers[target]);

        this.battlers[target].damage(damage);

        this.turnResults.add(this.battlers[this.turn].getName() + " attacked " + this.battlers[target].getName() + " and dealt " + damage + " damage!");

        this.advanceTurn();
    }

    //Embeds
    public void sendEndEmbed()
    {
        this.sendTurnEmbed();
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

            String overview = "*" + b.getOwner().getName() + "*'s **\"" + b.getName() + "\"** (" + (i + 1) + "): ";
            if(b.isDefeated()) overview += "DEFEATED";
            else overview += "%s / %s HEALTH".formatted(b.getHealth(), b.getStat(Stat.HEALTH));

            desc.append(overview).append("\n\n");
        }

        desc.append("**Turn Outcome:**\n");
        for(String s : this.turnResults) desc.append(s).append("\n");

        embed.setDescription(desc.toString());
    }

    //Common Utilities
    public boolean isComplete()
    {
        return this.players.stream().anyMatch(AbstractPlayer::isDefeated);
    }

    private void advanceTurn()
    {
        if(this.isComplete()) this.sendEndEmbed();
        else
        {
            this.sendTurnEmbed();
            if(++this.turn >= this.battlers.length)
            {
                this.setBattlerTurnOrder(); //Recalculate turn order if Speed stats changed during battle
                this.turn = 0;
            }

            this.turnResults.clear();

            //TODO: AI Decisions made here
            if(this.battlers[this.turn].isAI()) this.submitAITurn();
        }
    }

    private void submitAITurn()
    {
        List<Integer> possibleTargets = new ArrayList<>();

        for(int i = 0; i < this.battlers.length; i++)
            if(!this.battlers[i].isDefeated() && this.turn != i && !this.battlers[i].isOwnedBy(this.battlers[this.turn].getOwnerID()) )
                possibleTargets.add(i);

        this.submitTurn(possibleTargets.get(new Random().nextInt(possibleTargets.size())));
    }

    //Battle Setup - Common
    private void setBattleType(BattleType type)
    {
        this.battleType = type;
    }

    //Misc. Setup and Initializations (Common)
    private void setup()
    {
        this.setBattlerTurnOrder();
        this.turn = 0;
        this.turnResults = new ArrayList<>();
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
        this.players.add(new AIPlayer());
    }

    //Accessors
    public BattleType getBattleType()
    {
        return this.battleType;
    }

    public List<AbstractPlayer> getPlayers()
    {
        return this.players;
    }

    private enum BattleType
    {
        PVP,
        PVE;
    }
}
