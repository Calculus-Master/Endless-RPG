package com.calculusmaster.endlessrpg.gameplay.battle.player;

import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
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
        for(String s : this.data.getCharacterList()) this.team.add(RPGCharacter.build(s));
    }

    @Override
    public String getName()
    {
        return this.data.getUsername();
    }
}
