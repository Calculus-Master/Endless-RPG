package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.util.Global;

import java.util.SplittableRandom;

public enum LocationType
{
    HUB,
    FINAL_KINGDOM,
    TOWN,
    DUNGEON,
    FOREST,
    MEADOW,
    CAVE,
    MOUNTAIN,
    LAKE,
    DESERT;

    public boolean isTown()
    {
        return this.equals(HUB) || this.equals(TOWN);
    }

    public static LocationType cast(String input)
    {
        return Global.castEnum(input, values());
    }

    public static LocationType getRandomBiome()
    {
        return values()[new SplittableRandom().nextInt(4, values().length)];
    }
}
