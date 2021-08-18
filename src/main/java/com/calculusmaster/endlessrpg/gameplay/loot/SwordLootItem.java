package com.calculusmaster.endlessrpg.gameplay.loot;

import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.util.Global;

public class SwordLootItem extends LootItem
{
    public SwordLootItem(int attack)
    {
        super(LootType.SWORD);
        this.addStatBoosts(Stat.Pair.of(Stat.ATTACK, attack));
    }

    public SwordLootItem(int min, int max)
    {
        this(Global.randomValue(min, max));
    }
}