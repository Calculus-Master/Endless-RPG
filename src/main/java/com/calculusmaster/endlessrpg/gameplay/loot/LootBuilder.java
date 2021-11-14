package com.calculusmaster.endlessrpg.gameplay.loot;

import com.calculusmaster.endlessrpg.gameplay.enums.ElementType;
import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;

import java.util.SplittableRandom;

public class LootBuilder
{
    public static final SplittableRandom r = new SplittableRandom();

    public static LootItem create(LootType type, int level)
    {
        LootItem out = switch(type) {
            case SWORD -> LootBuilder.Sword(level);
            case WAND -> LootBuilder.Wand(level);
            case SHIELD -> LootBuilder.Shield(level);
            case HELMET -> LootBuilder.Helmet(level);
            case CHESTPLATE -> LootBuilder.Chestplate(level);
            case GAUNTLETS -> LootBuilder.Gauntlets(level);
            case LEGGINGS -> LootBuilder.Leggings(level);
            case BOOTS -> LootBuilder.Boots(level);
            case NONE -> throw new IllegalStateException("Unexpected Loot Type \"NONE\" in LootBuilder!");
        };

        out.getRequirements().addLevel(level);

        return out;
    }

    private static int r(int min, int max) { return r.nextInt(min, max); }

    private static int varyP(int input, int low, int high)
    {
        return (int)((new SplittableRandom().nextInt(low, high + 1)) / 100.0 * input);
    }

    private static int varyV(int input, int low, int high)
    {
        return new SplittableRandom().nextInt(input - low, input + high + 1);
    }

    private static int standard(int level)
    {
        return varyP(r(1, 5) + level, 90, 110);
    }

    private static int armor(int level)
    {
        return varyP(level, 110, 120);
    }

    public static LootItem Sword(int level)
    {
        int s = standard(level);

        LootItem sword = LootItem.create(LootType.SWORD)
            .addBoost(Stat.ATTACK, s);

        if(r.nextInt(100) < 40)
        {
            int boost = varyP(s, 20, 60);
            sword.addElementalDamage(ElementType.getRandom(), boost);
        }

        return sword;
    }

    public static LootItem Wand(int level)
    {
        int s = standard(level);

        LootItem wand = LootItem.create(LootType.WAND)
                .addBoost(Stat.ATTACK, (int)(0.25 * s))
                .addElementalDamage(ElementType.getRandom(), (int)(0.75 * s));

        wand.getRequirements()
                .addStat(Stat.INTELLECT, varyV(level * 2,  level / 2, level * 3));

        return wand;
    }

    public static LootItem Shield(int level)
    {
        LootItem shield = LootItem.create(LootType.SHIELD)
                .addBoost(Stat.DEFENSE, standard(level));

        shield.getRequirements()
                .addStat(Stat.DEFENSE, level - 1);

        return shield;
    }

    //Armor

    private static LootItem baseArmor(LootType type, int level)
    {
        LootItem armor = LootItem.create(type);

        int determinant = r.nextInt(100);

        int defense, health;

        //"Pooled" Method: 1 armor value, split into defense and health boosts
        if(determinant < 33)
        {
            int value = armor(level);
            int percDef = r.nextInt(100) + 1;

            defense = varyP((int)(value * percDef / 100.0), 80, 120);
            health = varyP((int)(value * (1 - percDef) / 100.0), 80, 120);
        }
        //"Independent" Method: 2 armor values, random percentage of each becomes a boost
        else if(determinant < 50)
        {
            int defVal = armor(level);
            int hpVal = armor(level);

            defense = varyP(defVal, 5, 75);
            health = varyP(hpVal, 5, 75);
        }
        //"Single" Method: Armor is synergized as HEALTH or DEFENSE, the other stat is randomly chosen to get boosted
        else
        {
            boolean isDefense = r.nextInt(100) < 50;

            int primaryValue = armor(level);
            int secondaryValue = r.nextInt(10) < 3 ? varyP(primaryValue, 2, 10) : 0;

            defense = isDefense ? primaryValue : secondaryValue;
            health = isDefense ? secondaryValue : primaryValue;
        }

        armor
                .addBoost(Stat.DEFENSE, defense)
                .addBoost(Stat.HEALTH, health);

        return armor;
    }

    public static LootItem Helmet(int level)
    {
        LootItem helmet = baseArmor(LootType.HELMET, level);

        return helmet;
    }

    public static LootItem Chestplate(int level)
    {
        LootItem chestplate = baseArmor(LootType.CHESTPLATE, level);

        return chestplate;
    }

    public static LootItem Gauntlets(int level)
    {
        LootItem gauntlets = baseArmor(LootType.GAUNTLETS, level);

        return gauntlets;
    }

    public static LootItem Leggings(int level)
    {
        LootItem leggings = baseArmor(LootType.LEGGINGS, level);

        return leggings;
    }

    public static LootItem Boots(int level)
    {
        LootItem boots = baseArmor(LootType.BOOTS, level);

        return boots;
    }
}
