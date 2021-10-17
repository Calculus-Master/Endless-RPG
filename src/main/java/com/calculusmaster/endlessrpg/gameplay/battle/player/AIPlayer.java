package com.calculusmaster.endlessrpg.gameplay.battle.player;

import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AIPlayer extends AbstractPlayer
{
    public AIPlayer(RPGCharacter... characters)
    {
        this(Arrays.asList(characters));
    }

    public AIPlayer(List<RPGCharacter> characters)
    {
        super(generateID());

        this.team.addAll(characters);

        this.initTeam();
    }

    @Override
    public String getName()
    {
        //TODO: Randomly picked RPG Character name
        return "Bot";
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
