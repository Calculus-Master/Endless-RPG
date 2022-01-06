package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.util.Global;

import static com.calculusmaster.endlessrpg.gameplay.enums.LootComponentType.CoreComponentType.GENERAL;
import static com.calculusmaster.endlessrpg.gameplay.enums.LootComponentType.CoreComponentType.PRIMARY;

public enum LootComponentType
{
    //General
    BASIC_HANDLE(GENERAL, 1, 100),
    BASIC_BINDING(GENERAL, 1, 100),

    //Sword
    SWORD_BLADE(PRIMARY, 2, 400),

    //Wand
    WAND_CORE(PRIMARY, 2, 400);

    private final CoreComponentType core;
    private final int materialAmount;
    private final int goldCost;

    LootComponentType(CoreComponentType core, int materialAmount, int goldCost)
    {
        this.core = core;
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

    public boolean isGeneral()
    {
        return this.core.equals(GENERAL);
    }

    public boolean isPrimary()
    {
        return this.core.equals(PRIMARY);
    }

    public static LootComponentType cast(String input)
    {
        return Global.castEnum(input, values());
    }

    public enum CoreComponentType
    {
        GENERAL, //Components like handles, bindings
        PRIMARY; //Components like sword blades, wand cores
    }
}
