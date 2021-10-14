package com.calculusmaster.endlessrpg.gameplay.battle;

import com.calculusmaster.endlessrpg.gameplay.battle.player.AIPlayer;
import com.calculusmaster.endlessrpg.gameplay.battle.player.AbstractPlayer;
import com.calculusmaster.endlessrpg.gameplay.battle.player.UserPlayer;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.gameplay.spells.SimpleAttackSpell;
import com.calculusmaster.endlessrpg.gameplay.spells.Spell;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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

    private Optional<MessageReceivedEvent> event = Optional.empty();

    //Creators

    public static Battle createPVP(String p1ID, String p2ID)
    {
        Battle b = new Battle();

        b.setBattleType(BattleType.PVP);
        b.createPlayers(p1ID, p2ID);
        b.setup();

        BATTLES.add(b);
        return b;
    }

    public static Battle createPVE(String userID)
    {
        Battle b = new Battle();

        b.setBattleType(BattleType.PVE);
        b.createPlayers(userID);
        b.setup();

        BATTLES.add(b);
        return b;
    }

    public static boolean simulate(RPGCharacter characterPlayer, RPGCharacter characterAI)
    {
        Battle b = new Battle();

        b.setBattleType(BattleType.PVE);

        b.players = new ArrayList<>();
        AIPlayer player = new AIPlayer().overrideTeam(characterPlayer);
        AIPlayer AI = new AIPlayer().overrideTeam(characterAI);
        b.players.add(player);
        b.players.add(AI);

        b.setup();

        while(!b.isComplete()) b.submitAITurn();

        return b.getWinner().ID.equals(player.ID);
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

        embed.setDescription(this.getWinner().getName() + " has defeated " + this.getLoser().getName() + "!");

        //TODO: Battle win rewards

        this.sendEmbed(embed);

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

            String overview = "(" + (i + 1) + ") *" + b.getOwner().getName() + "*'s **\"" + b.getName() + "\"**: ";
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
        return this.players.stream().anyMatch(AbstractPlayer::isDefeated);
    }

    public AbstractPlayer getWinner()
    {
        if(!this.isComplete()) throw new IllegalStateException("Cannot find Battle Winner because Battle is not complete!");
        else return this.players.get(0).isDefeated() ? this.players.get(1) : this.players.get(0);
    }

    public AbstractPlayer getLoser()
    {
        return this.getWinner().ID.equals(this.players.get(0).ID) ? this.players.get(1) : this.players.get(0);
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
        this.submitTurn(possibleTargets.get(new Random().nextInt(possibleTargets.size())), new SimpleAttackSpell());
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
