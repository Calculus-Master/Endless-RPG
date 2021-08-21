package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.util.Global;

public enum EquipmentType
{
    HELMET,
    CHESTPLATE,
    GAUNTLETS,
    LEGGINGS,
    BOOTS,
    LEFT_HAND,
    RIGHT_HAND;

    public String getStyledName()
    {
        return Global.normalize(this.toString().replaceAll("_", " "));
    }
}
