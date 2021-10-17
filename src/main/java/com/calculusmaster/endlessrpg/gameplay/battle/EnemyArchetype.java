package com.calculusmaster.endlessrpg.gameplay.battle;

import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;

import java.util.function.Function;

public enum EnemyArchetype
{
    RANDOM(EnemyBuilder::createDefault);

    private Function<Integer, RPGCharacter> builder;

    EnemyArchetype(Function<Integer, RPGCharacter> builder)
    {
        this.builder = builder;
    }

    public RPGCharacter create(int level)
    {
        return this.builder.apply(level);
    }
}
