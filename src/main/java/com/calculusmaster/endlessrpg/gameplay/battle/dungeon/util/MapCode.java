package com.calculusmaster.endlessrpg.gameplay.battle.dungeon.util;

import java.util.Objects;

public enum MapCode
{
    EMPTY,
    ROOM,
    SPAWN,
    BOSS;

    public static MapCode from(int n)
    {
        return Objects.requireNonNull(n >= 0 && n < values().length ? values()[n] : null);
    }
}
