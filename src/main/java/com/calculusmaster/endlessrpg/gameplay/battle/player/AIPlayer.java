package com.calculusmaster.endlessrpg.gameplay.battle.player;

import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;

import java.util.Random;

public class AIPlayer extends AbstractPlayer
{
    public AIPlayer()
    {
        super(generateID());
    }

    @Override
    public void createTeam()
    {
        //TODO: Temporary - AI team needs to have loot eventually and scale difficulty
        this.team.add(RPGCharacter.create("AI"));
    }

    private static String generateID()
    {
        final StringBuilder s = new StringBuilder();

        final String prefix = "AIPlayer-";
        final String pool = "abcdefghiklmnopqrstuvwxyz0123456789";

        final Random r = new Random();
        for(int i = 0; i < 32 - prefix.length(); i++) s.append(pool.charAt(r.nextInt(pool.length())));
        return s.toString();
    }
}
