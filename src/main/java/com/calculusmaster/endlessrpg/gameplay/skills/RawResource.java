package com.calculusmaster.endlessrpg.gameplay.skills;

import java.util.ArrayList;
import java.util.List;

import static com.calculusmaster.endlessrpg.gameplay.skills.GatheringSkill.*;

public enum RawResource
{
    //Mining
    MINING_T1(MINING, 1, ""),
    MINING_T2(MINING, 2, ""),
    MINING_T3(MINING, 3, ""),
    MINING_T4(MINING, 4, ""),
    MINING_T5(MINING, 5, ""),
    MINING_T6(MINING, 6, ""),
    MINING_T7(MINING, 7, ""),
    MINING_T8(MINING, 8, ""),
    MINING_T9(MINING, 9, ""),
    MINING_T10(MINING, 10, ""),

    //Foraging
    FORAGING_T1(FORAGING, 1, ""),
    FORAGING_T2(FORAGING, 2, ""),
    FORAGING_T3(FORAGING, 3, ""),
    FORAGING_T4(FORAGING, 4, ""),
    FORAGING_T5(FORAGING, 5, ""),
    FORAGING_T6(FORAGING, 6, ""),
    FORAGING_T7(FORAGING, 7, ""),
    FORAGING_T8(FORAGING, 8, ""),
    FORAGING_T9(FORAGING, 9, ""),
    FORAGING_T10(FORAGING, 10, ""),

    //Fishing
    FISHING_T2(FISHING, 2, ""),
    FISHING_T1(FISHING, 1, ""),
    FISHING_T3(FISHING, 3, ""),
    FISHING_T4(FISHING, 4, ""),
    FISHING_T5(FISHING, 5, ""),
    FISHING_T6(FISHING, 6, ""),
    FISHING_T7(FISHING, 7, ""),
    FISHING_T8(FISHING, 8, ""),
    FISHING_T9(FISHING, 9, ""),
    FISHING_T10(FISHING, 10, ""),

    //Woodcutting
    WOODCUTTING_T1(WOODCUTTING, 1, ""),
    WOODCUTTING_T2(WOODCUTTING, 2, ""),
    WOODCUTTING_T3(WOODCUTTING, 3, ""),
    WOODCUTTING_T4(WOODCUTTING, 4, ""),
    WOODCUTTING_T5(WOODCUTTING, 5, ""),
    WOODCUTTING_T6(WOODCUTTING, 6, ""),
    WOODCUTTING_T7(WOODCUTTING, 7, ""),
    WOODCUTTING_T8(WOODCUTTING, 8, ""),
    WOODCUTTING_T9(WOODCUTTING, 9, ""),
    WOODCUTTING_T10(WOODCUTTING, 10, ""),

    //Farming
    FARMING_T1(FARMING, 1, ""),
    FARMING_T2(FARMING, 2, ""),
    FARMING_T3(FARMING, 3, ""),
    FARMING_T4(FARMING, 4, ""),
    FARMING_T5(FARMING, 5, ""),
    FARMING_T6(FARMING, 6, ""),
    FARMING_T7(FARMING, 7, ""),
    FARMING_T8(FARMING, 8, ""),
    FARMING_T9(FARMING, 9, ""),
    FARMING_T10(FARMING, 10, ""),

    //Hunting
    HUNTING_T1(HUNTING, 1, ""),
    HUNTING_T2(HUNTING, 2, ""),
    HUNTING_T3(HUNTING, 3, ""),
    HUNTING_T4(HUNTING, 4, ""),
    HUNTING_T5(HUNTING, 5, ""),
    HUNTING_T6(HUNTING, 6, ""),
    HUNTING_T7(HUNTING, 7, ""),
    HUNTING_T8(HUNTING, 8, ""),
    HUNTING_T9(HUNTING, 9, ""),
    HUNTING_T10(HUNTING, 10, "");

    private final GatheringSkill skill;
    private final int tier;
    private final String name;

    RawResource(GatheringSkill skill, int tier, String name)
    {
        this.skill = skill;
        this.tier = tier;
        this.name = name;
    }

    public GatheringSkill getSkill()
    {
        return this.skill;
    }

    public int getTier()
    {
        return this.tier;
    }

    public String getName()
    {
        return this.name.isEmpty() ? this.toString() : this.name; //TODO: Actual names
    }

    public static List<RawResource> getResources(GatheringSkill skill)
    {
        List<RawResource> resources = new ArrayList<>();
        for(RawResource r : RawResource.values()) if(r.getSkill().equals(skill)) resources.add(r);
        return resources;
    }

    public static List<RawResource> getResources(int tier)
    {
        List<RawResource> resources = new ArrayList<>();
        for(RawResource r : RawResource.values()) if(r.getTier() == tier) resources.add(r);
        return resources;
    }

    public static RawResource getResource(GatheringSkill skill, int tier)
    {
        for(RawResource r : RawResource.values()) if(r.getSkill().equals(skill) && r.getTier() == tier) return r;
        return null;
    }
}
