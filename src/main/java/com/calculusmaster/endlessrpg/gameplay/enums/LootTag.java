package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.util.Global;

public enum LootTag
{
    RANDOMIZED(""),
    CRAFTED(":tools:"),
    UNIQUE(":sparkles:"),
    MINI_BOSS(":regional_indicator_m:"),
    BOSS(":regional_indicator_b:");

    private final String icon;

    LootTag(String icon)
    {
        this.icon = icon;
    }

    public String getIcon()
    {
        return this.icon;
    }

    public static LootTag cast(String input)
    {
        return Global.castEnum(input, values());
    }
}
