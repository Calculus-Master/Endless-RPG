package com.calculusmaster.endlessrpg.gameplay.loot;

import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;

import java.util.Random;

public class LootBuilder
{
    public static LootItem rewardSword(int level)
    {
        LootItem sword = LootItem.create(LootType.SWORD);

        //All swords have attack
        sword.withBoosts(Stat.Pair.of(Stat.ATTACK, level * (new Random().nextInt(2) + 3)));

        return sword;
    }
}
