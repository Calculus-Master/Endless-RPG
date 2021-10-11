package com.calculusmaster.endlessrpg.gameplay.battle;

import com.calculusmaster.endlessrpg.gameplay.battle.player.AIPlayer;
import com.calculusmaster.endlessrpg.gameplay.battle.player.AbstractPlayer;
import com.calculusmaster.endlessrpg.gameplay.battle.player.UserPlayer;

import java.util.ArrayList;
import java.util.List;

public class Battle
{
    private BattleType battleType;
    private List<AbstractPlayer> players;

    //Creators

    public static Battle createPVP(String p1ID, String p2ID)
    {
        Battle b = new Battle();

        b.setBattleType(BattleType.PVP);
        b.createPlayers(p1ID, p2ID);

        return b;
    }

    public static Battle createPVE(String userID)
    {
        Battle b = new Battle();

        b.setBattleType(BattleType.PVE);
        b.createPlayers(userID);

        return b;
    }

    //Battle Setup - Common
    private void setBattleType(BattleType type)
    {
        this.battleType = type;
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

    private enum BattleType
    {
        PVP,
        PVE;
    }
}
