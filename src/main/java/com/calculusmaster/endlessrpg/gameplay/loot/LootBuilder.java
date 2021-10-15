package com.calculusmaster.endlessrpg.gameplay.loot;

import com.calculusmaster.endlessrpg.gameplay.enums.ElementType;
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

    public static LootItem reward(LootType type, int level)
    {
        LootItem out = switch(type) {
            case SWORD -> LootBuilder.rewardSword(level);
            case WAND -> LootBuilder.rewardWand(level);
            case SHIELD -> LootBuilder.rewardShield(level);
            case HELMET -> LootBuilder.rewardHelmet(level);
            case CHESTPLATE -> LootBuilder.rewardChestplate(level);
            case GAUNTLETS -> LootBuilder.rewardGauntlets(level);
            case LEGGINGS -> LootBuilder.rewardLeggings(level);
            case BOOTS -> LootBuilder.rewardBoots(level);
            case NONE -> throw new IllegalStateException("Unexpected Loot Type \"NONE\" in LootBuilder!");
        };

        out.getRequirements().addLevel(level);

        return out;
    }

    private static int rand(int min, int max) { return r.nextInt(max - min + 1) + min; }

    public static LootItem rewardSword(int level)
    {
        LootItem sword = LootItem.create(LootType.SWORD)
            .addBoost(Stat.ATTACK, rand(2, 5) * level + 5 + rand(1, level * 2));

        return sword;
    }

    public static LootItem rewardWand(int level)
    {
        LootItem wand = LootItem.create(LootType.WAND)
                .addBoost(Stat.ATTACK, rand(2, 5) * level + 5 + rand(1, level * 2))
                .addElementalDamage(ElementType.getRandom(), rand(2, 5) * level + rand(1, level));

        wand.getRequirements()
                .addStat(Stat.INTELLECT, level * 3);

        return wand;
    }

    public static LootItem rewardShield(int level)
    {
        LootItem shield = LootItem.create(LootType.SHIELD)
                .addBoost(Stat.DEFENSE, rand(2, 5) * level + 5 + rand(1, (int)(level * 1.5)));

        shield.getRequirements()
                .addStat(Stat.DEFENSE, Math.max(1, level / 2));

        return shield;
    }

    //Armor

    public static LootItem rewardHelmet(int level)
    {
        LootItem helmet = LootItem.create(LootType.HELMET)
                .addBoost(Stat.DEFENSE, level * (r.nextInt(2) + 2));

        return helmet;
    }

    public static LootItem rewardChestplate(int level)
    {
        LootItem chestplate = LootItem.create(LootType.CHESTPLATE)
                .addBoost(Stat.DEFENSE, level * (r.nextInt(2) + 2));

        return chestplate;
    }

    public static LootItem rewardGauntlets(int level)
    {
        LootItem gauntlets = LootItem.create(LootType.GAUNTLETS)
                .addBoost(Stat.DEFENSE, level * (r.nextInt(2) + 2));

        return gauntlets;
    }

    public static LootItem rewardLeggings(int level)
    {
        LootItem leggings = LootItem.create(LootType.LEGGINGS)
                .addBoost(Stat.DEFENSE, level * (r.nextInt(2) + 2));

        return leggings;
    }

    public static LootItem rewardBoots(int level)
    {
        LootItem boots = LootItem.create(LootType.BOOTS)
                .addBoost(Stat.DEFENSE, level * (r.nextInt(2) + 2));

        return boots;
    }
}
