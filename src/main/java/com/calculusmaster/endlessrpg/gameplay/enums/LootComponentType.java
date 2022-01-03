package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.util.Global;

public enum LootComponentType
{
    //General
    BASIC_HANDLE(1, 100),
    BASIC_BINDING(1, 100),

    //Sword
    SWORD_BLADE(2, 400),

    //Wand
    WAND_CORE(2, 400);

    private final int materialAmount;
    private final int goldCost;

    LootComponentType(int materialAmount, int goldCost)
    {
        this.materialAmount = materialAmount;
        this.goldCost = goldCost;
    }

    public int getMaterialAmount()
    {
        return this.materialAmount;
    }

    public int getGoldCost()
    {
        return this.goldCost;
    }

    public String getName()
    {
        return Global.normalize(this.toString().replaceAll("_", " "));
    }

    public static LootComponentType cast(String input)
    {
        return Global.castEnum(input, values());
    }
}
