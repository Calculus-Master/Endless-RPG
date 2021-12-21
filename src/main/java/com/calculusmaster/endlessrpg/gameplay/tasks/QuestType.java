package com.calculusmaster.endlessrpg.gameplay.tasks;

import com.calculusmaster.endlessrpg.util.Global;

public enum QuestType
{
    MAIN,
    RANDOM,
    LOCATION;

    public static QuestType cast(String input)
    {
        return Global.castEnum(input, values());
    }
}
