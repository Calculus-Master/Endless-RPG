package com.calculusmaster.endlessrpg.gameplay.battle.player;

import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;

public class UserPlayer extends AbstractPlayer
{
    public PlayerDataQuery data;

    public UserPlayer(String ID)
    {
        super(ID);

        this.data = new PlayerDataQuery(ID);

        for(String s : this.data.getCharacterList()) this.team.add(RPGCharacter.build(s));

        this.initTeam();
    }

    @Override
    public String getName()
    {
        return this.data.getUsername();
    }
}
