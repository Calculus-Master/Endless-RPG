package com.calculusmaster.endlessrpg.gameplay.skills;

import com.calculusmaster.endlessrpg.util.Global;

public enum GatheringSkill
{
    MINING,
    FORAGING,
    FISHING,
    WOODCUTTING,
    FARMING,
    HUNTING;

    public static GatheringSkill cast(String input)
    {
        return Global.castEnum(input, values());
    }
}