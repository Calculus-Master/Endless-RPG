package com.calculusmaster.endlessrpg.gameplay.resources.enums;

import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.resources.ResourceTraitRegistry;
import com.calculusmaster.endlessrpg.gameplay.world.skills.GatheringSkill;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import static com.calculusmaster.endlessrpg.gameplay.world.skills.GatheringSkill.*;

public enum RawResource implements Resource
{
    //Mining
    MINING_T1(MINING, 1, "Copper Ore"),
    MINING_T2(MINING, 2, "Iron Ore"),
    MINING_T3(MINING, 3, "Silver Ore"),
    MINING_T4(MINING, 4, "Gold Ore"),
    MINING_T5(MINING, 5, "Cobalt Ore"),
    MINING_T6(MINING, 6, "Platinum Ore"),
    MINING_T7(MINING, 7, "Titanium Ore"),
    MINING_T8(MINING, 8, "Mithril Ore"),
    MINING_T9(MINING, 9, "Adamantium Ore"),
    MINING_T10(MINING, 10, "Voidsteel Ore"),

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
    FISHING_T1(FISHING, 1, ""),
    FISHING_T2(FISHING, 2, ""),
    FISHING_T3(FISHING, 3, ""),
    FISHING_T4(FISHING, 4, ""),
    FISHING_T5(FISHING, 5, ""),
    FISHING_T6(FISHING, 6, ""),
    FISHING_T7(FISHING, 7, ""),
    FISHING_T8(FISHING, 8, ""),
    FISHING_T9(FISHING, 9, ""),
    FISHING_T10(FISHING, 10, ""),

    //Woodcutting
    WOODCUTTING_T1(WOODCUTTING, 1, "Oak Log"),
    WOODCUTTING_T2(WOODCUTTING, 2, "Birch Log"),
    WOODCUTTING_T3(WOODCUTTING, 3, "Acacia Log"),
    WOODCUTTING_T4(WOODCUTTING, 4, "Willow Log"),
    WOODCUTTING_T5(WOODCUTTING, 5, "Shiftwood Log"),
    WOODCUTTING_T6(WOODCUTTING, 6, "Sky Log"),
    WOODCUTTING_T7(WOODCUTTING, 7, "Hardwood Log"),
    WOODCUTTING_T8(WOODCUTTING, 8, "Enchanted Log"),
    WOODCUTTING_T9(WOODCUTTING, 9, "Ironwood Log"),
    WOODCUTTING_T10(WOODCUTTING, 10, "Void Log"),

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
    FARMING_T10(FARMING, 10, "");

    public static final int MAX_TIER = 10;

    private final GatheringSkill skill;
    private final int tier;
    private final String name;

    private final ResourceTraitRegistry traits;

    RawResource(GatheringSkill skill, int tier, String name, TraitRegistryEditor editor)
    {
        this.skill = skill;
        this.tier = tier;
        this.name = name;

        this.traits = editor == null ? ResourceTraitRegistry.of(this) : editor.edit(ResourceTraitRegistry.of(this));
    }

    RawResource(GatheringSkill skill, int tier, String name)
    {
        this(skill, tier, name, null);
    }

    public GatheringSkill getSkill()
    {
        return this.skill;
    }

    @Override
    public int getTier()
    {
        return this.tier;
    }

    @Override
    public String getName()
    {
        return this.name.isEmpty() ? this.toString() : this.name; //TODO: Actual names
    }

    @Override
    public ResourceTraitRegistry getTraits()
    {
        return this.traits;
    }

    public int getExp()
    {
        return switch(this.tier) {
            case 1 -> 10;
            case 2 -> 25;
            case 3 -> 50;
            case 4 -> 75;
            case 5 -> 100;
            case 6 -> 200;
            case 7 -> 500;
            case 8 -> 1000;
            case 9 -> 1500;
            case 10 -> 2500;
            default -> 0;
        };
    }

    public int getNodeHealth()
    {
        return switch(this.tier) {
            case 1 -> 500;
            case 2 -> 1000;
            case 3 -> 2000;
            case 4 -> 4000;
            case 5 -> 8000;
            case 6 -> 16000;
            case 7 -> 24000;
            case 8 -> 40000;
            case 9 -> 64000;
            case 10 -> 100000;
            default -> throw new IllegalArgumentException("Invalid Tier! Maximum is " + MAX_TIER + ".");
        };
    }

    public int getRequiredSkillLevel()
    {
        return 10 * (this.getTier() - 1);
    }

    public boolean canGather(RPGCharacter c)
    {
        return c.getSkillLevel(this.getSkill()) >= this.getRequiredSkillLevel();
    }

    public static RawResource cast(String input)
    {
        for(RawResource r : RawResource.values()) if(r.toString().equalsIgnoreCase(input) || (!r.getName().isEmpty() && r.getName().equalsIgnoreCase(input))) return r;
        return null;
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

    public static RawResource getRandom(int tier)
    {
        return getResources(tier).get(new SplittableRandom().nextInt(getResources(tier).size()));
    }

    public static RawResource getRandom()
    {
        return values()[new SplittableRandom().nextInt(values().length)];
    }
}
