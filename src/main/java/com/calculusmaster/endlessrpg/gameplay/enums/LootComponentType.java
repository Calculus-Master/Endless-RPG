package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.util.Global;

public enum LootComponentType
{
    //General
    BASIC_HANDLE(1),
    BASIC_BINDING(1),

    //Sword
    SWORD_BLADE(2),

    //Wand
    WAND_CORE(2);

    private int materialAmount;

    LootComponentType(int materialAmount)
    {
        this.materialAmount = materialAmount;
    }

    public int getMaterialAmount()
    {
        return this.materialAmount;
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
