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
            int boost = varyP(s, 75, 125);
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
                .addStat(Stat.INTELLECT, level * 3);

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

    public static LootItem Helmet(int level)
    {
        LootItem helmet = LootItem.create(LootType.HELMET)
                .addBoost(Stat.DEFENSE, armor(level));

        return helmet;
    }

    public static LootItem Chestplate(int level)
    {
        LootItem chestplate = LootItem.create(LootType.CHESTPLATE)
                .addBoost(Stat.DEFENSE, armor(level));

        return chestplate;
    }

    public static LootItem Gauntlets(int level)
    {
        LootItem gauntlets = LootItem.create(LootType.GAUNTLETS)
                .addBoost(Stat.DEFENSE, armor(level));

        return gauntlets;
    }

    public static LootItem Leggings(int level)
    {
        LootItem leggings = LootItem.create(LootType.LEGGINGS)
                .addBoost(Stat.DEFENSE, armor(level));

        return leggings;
    }

    public static LootItem Boots(int level)
    {
        LootItem boots = LootItem.create(LootType.BOOTS)
                .addBoost(Stat.DEFENSE, armor(level));

        return boots;
    }
}
