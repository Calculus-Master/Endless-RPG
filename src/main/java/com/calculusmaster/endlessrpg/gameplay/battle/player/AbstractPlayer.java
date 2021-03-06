package com.calculusmaster.endlessrpg.gameplay.battle.player;

import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPlayer
{
    public String ID;
    public List<RPGCharacter> team;

    public AbstractPlayer(String ID)
    {
        this.ID = ID;
        this.team = new ArrayList<>();
    }

    protected void initTeam()
    {
        this.team.forEach(c -> c.forBattle(this));
    }

    public abstract String getName();

    public boolean isDefeated()
    {
        return this.team.stream().allMatch(c -> c.getHealth() <= 0);
    }
}
