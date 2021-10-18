package com.calculusmaster.endlessrpg.gameplay.battle.enemy;

import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.util.Global;

import java.util.SplittableRandom;
import java.util.function.Function;

public enum EnemyArchetype
{
    DEFAULT(EnemyBuilder::createDefault),
    RANDOM(l -> values()[new SplittableRandom().nextInt(values().length)].create(l)),
    KINGDOM_RULER(EnemyBuilder::createRuler);

    private Function<Integer, RPGCharacter> builder;

    EnemyArchetype(Function<Integer, RPGCharacter> builder)
    {
        this.builder = builder;
    }

    public RPGCharacter create(int level)
    {
        return this.builder.apply(level);
    }

    public static EnemyArchetype cast(String input)
    {
        return Global.castEnum(input, values());
    }
}
