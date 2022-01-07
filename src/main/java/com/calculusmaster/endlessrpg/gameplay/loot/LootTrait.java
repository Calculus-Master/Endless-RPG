package com.calculusmaster.endlessrpg.gameplay.loot;


import com.calculusmaster.endlessrpg.util.Global;

public enum LootTrait
{
    WISE("All experience gains are increased by 5%.");

    private final String description;

    LootTrait(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return this.description;
    }

    public static LootTrait cast(String input)
    {
        return Global.castEnum(input, values());
    }
}
