package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.util.Global;

public enum Weather
{
    CLEAR,
    OVERCAST,
    RAIN;

    public static Weather cast(String input)
    {
        return Global.castEnum(input, values());
    }
}
