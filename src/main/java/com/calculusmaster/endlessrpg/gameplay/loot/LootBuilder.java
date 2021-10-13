package com.calculusmaster.endlessrpg.gameplay.loot;

import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LootBuilder
{
    public static final Random r = new Random();

    static
    {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> r.setSeed(System.nanoTime()), 1, 10, TimeUnit.MINUTES);
    }

    public static LootItem rewardSword(int level)
    {
        LootItem sword = LootItem.create(LootType.SWORD);

        //All swords have attack
        sword.withBoosts(Stat.Pair.of(Stat.ATTACK, level * (r.nextInt(2) + 3)));

        return sword;
    }

    public static LootItem rewardHelmet(int level)
    {
        LootItem helmet = LootItem.create(LootType.HELMET);

        //All helmets have defense
        helmet.withBoosts(Stat.Pair.of(Stat.DEFENSE, level * (r.nextInt(2) + 2)));

        return helmet;
    }

    public static LootItem rewardChestplate(int level)
    {
        LootItem chestplate = LootItem.create(LootType.CHESTPLATE);

        //All helmets have defense
        chestplate.withBoosts(Stat.Pair.of(Stat.DEFENSE, level * (r.nextInt(2) + 2)));

        return chestplate;
    }

    public static LootItem rewardGauntlets(int level)
    {
        LootItem gauntlets = LootItem.create(LootType.GAUNTLETS);

        //All helmets have defense
        gauntlets.withBoosts(Stat.Pair.of(Stat.DEFENSE, level * (r.nextInt(2) + 2)));

        return gauntlets;
    }

    public static LootItem rewardLeggings(int level)
    {
        LootItem leggings = LootItem.create(LootType.LEGGINGS);

        //All helmets have defense
        leggings.withBoosts(Stat.Pair.of(Stat.DEFENSE, level * (r.nextInt(2) + 2)));

        return leggings;
    }

    public static LootItem rewardBoots(int level)
    {
        LootItem boots = LootItem.create(LootType.BOOTS);

        //All helmets have defense
        boots.withBoosts(Stat.Pair.of(Stat.DEFENSE, level * (r.nextInt(2) + 2)));

        return boots;
    }
}
