package com.calculusmaster.endlessrpg.gameplay.resources.enums;

public enum RefinedResource implements Resource
{
    REFINED_MINING_T1_INGOT(1, "Copper Ingot");

    private final int tier;
    private final String name;

    RefinedResource(int tier, String name)
    {
        this.tier = tier;
        this.name = name;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public int getTier()
    {
        return this.tier;
    }
}
