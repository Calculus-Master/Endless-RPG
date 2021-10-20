package com.calculusmaster.endlessrpg.gameplay.battle.enemy;

import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.util.Global;
import com.calculusmaster.endlessrpg.util.helpers.LoggerHelper;

import java.util.SplittableRandom;
import java.util.function.Function;

public enum EnemyArchetype
{
    DEFAULT(EnemyBuilder::createDefault),
    RANDOM(l -> values()[new SplittableRandom().nextInt(values().length)].create(l)),
    KINGDOM_RULER(EnemyBuilder::createRuler),
    DRAGON(EnemyBuilder::createDragon),
    WYVERN(null),
    SKELETON(null),
    ANGEL(null),
    DEMON(null),
    GOBLIN(null),
    ORC(null),
    ELF(null),
    DWARF(null),
    GHOST(null),
    GOLEM(null),
    KNIGHT(null),
    MECH(null),
    ASSASSIN(null),
    PIRATE(null),
    TROLL(null),
    TITAN(null),
    VAMPIRE(null),
    WITCH(null),
    ZOMBIE(null);

    private final Function<Integer, RPGCharacter> builder;

    EnemyArchetype(Function<Integer, RPGCharacter> builder)
    {
        this.builder = builder;
        if(this.builder == null) LoggerHelper.warn(EnemyArchetype.class, "Unimplemented Enemy Archetype: " + this);
    }

    public RPGCharacter create(int level)
    {
        return this.builder == null ? DEFAULT.create(level) : this.builder.apply(level);
    }

    public static EnemyArchetype cast(String input)
    {
        return Global.castEnum(input, values());
    }
}
