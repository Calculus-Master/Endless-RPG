package com.calculusmaster.endlessrpg.gameplay.battle.player;

import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;

public class UserPlayer extends AbstractPlayer
{
    public PlayerDataQuery data;

    public UserPlayer(String ID)
    {
        super(ID);
    }

    @Override
    public void createTeam()
    {
        this.data = new PlayerDataQuery(ID);
        //TODO: This is temporary, need to implement a Team system and maybe altered size duels
        this.team.add(this.data.getActiveCharacter());
    }

    @Override
    public String getName()
    {
        return this.data.getUsername();
    }
}
