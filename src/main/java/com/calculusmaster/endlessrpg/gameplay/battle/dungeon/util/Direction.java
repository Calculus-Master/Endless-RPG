package com.calculusmaster.endlessrpg.gameplay.battle.dungeon.util;

import com.calculusmaster.endlessrpg.util.Global;

public enum Direction
{
    UP,
    DOWN,
    LEFT,
    RIGHT;

    public static Direction cast(String input)
    {
        return Global.castEnum(input, values());
    }
}
