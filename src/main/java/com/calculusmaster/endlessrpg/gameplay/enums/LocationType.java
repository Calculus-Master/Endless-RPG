package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.util.Global;

import java.util.SplittableRandom;

public enum LocationType
{
    TOWN,
    DUNGEON,
    FOREST,
    MEADOW,
    CAVE,
    MOUNTAIN,
    LAKE,
    DESERT;

    public static LocationType cast(String input)
    {
        return Global.castEnum(input, values());
    }

    public static LocationType getRandomBiome()
    {
        return values()[new SplittableRandom().nextInt(2, values().length)];
    }
}
