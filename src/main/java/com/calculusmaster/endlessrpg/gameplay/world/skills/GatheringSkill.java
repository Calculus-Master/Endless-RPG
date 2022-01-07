package com.calculusmaster.endlessrpg.gameplay.world.skills;

import com.calculusmaster.endlessrpg.util.Global;

public enum GatheringSkill
{
    MINING,
    FORAGING,
    FISHING,
    WOODCUTTING,
    FARMING;

    public static GatheringSkill cast(String input)
    {
        return Global.castEnum(input, values());
    }
}