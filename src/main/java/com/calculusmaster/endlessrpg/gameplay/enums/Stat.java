package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.gameplay.world.skills.GatheringSkill;
import com.calculusmaster.endlessrpg.util.Global;

public enum Stat
{
    //Basic Stats
    HEALTH,
    ATTACK,
    DEFENSE,
    SPEED,

    //Weapon Affinity Stats
    STRENGTH,
    INTELLECT,
    PRECISION,

    //Other Stats
    STAMINA,

    //Tool Stats
    MINING_POWER,
    FORAGING_POWER,
    FISHING_POWER,
    WOODCUTTING_POWER,
    FARMING_POWER;

    public static Stat cast(String input)
    {
        return Global.castEnum(input, values());
    }

    public static Stat getRelevantToolStat(GatheringSkill skill)
    {
        return switch(skill) {
            case MINING -> MINING_POWER;
            case FORAGING -> FORAGING_POWER;
            case FISHING -> FISHING_POWER;
            case WOODCUTTING -> WOODCUTTING_POWER;
            case FARMING -> FARMING_POWER;
        };
    }
}