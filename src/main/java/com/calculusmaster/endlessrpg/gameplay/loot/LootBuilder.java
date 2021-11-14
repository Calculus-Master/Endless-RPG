package com.calculusmaster.endlessrpg.gameplay.loot;

import com.calculusmaster.endlessrpg.gameplay.enums.ElementType;
import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;

import java.util.*;

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
            .setBoost(Stat.ATTACK, s);

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
                .setBoost(Stat.ATTACK, (int)(0.25 * s))
                .addElementalDamage(ElementType.getRandom(), (int)(0.75 * s));

        wand.getRequirements()
                .addStat(Stat.INTELLECT, varyV(level * 2,  level / 2, level * 3));

        return wand;
    }

    public static LootItem Shield(int level)
    {
        LootItem shield = LootItem.create(LootType.SHIELD)
                .setBoost(Stat.DEFENSE, standard(level));

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

        switch(type)
        {
            case CHESTPLATE -> {
                defense = varyP(defense, 105, 120);
                health = varyP(defense, 115, 130);
            }
            case GAUNTLETS, BOOTS -> {
                defense = varyP(defense, 85, 90);
                health = varyP(defense, 75, 90);
            }
            case HELMET, LEGGINGS -> {
                defense = varyP(defense, 95, 105);
                health = varyP(defense, 90, 110);
            }
        }

        armor
                .setBoost(Stat.DEFENSE, defense)
                .setBoost(Stat.HEALTH, health);

        return armor;
    }

    private static LootItem basicArmorElementalModifiers(LootItem armor)
    {
        int numElements;
        int rand = r.nextInt(100);

        if(rand < 40) return armor;
        else if(rand < 80) numElements = 1;
        else if(rand < 90) numElements = 2;
        else if(rand < 95) numElements = 3;
        else numElements = 4;

        List<ElementType> elementsPool = new ArrayList<>(EnumSet.allOf(ElementType.class));
        Collections.shuffle(elementsPool);
        List<ElementType> elements = elementsPool.subList(0, numElements);

        int defense = armor.getBoost(Stat.DEFENSE);
        int power = defense + armor.getBoost(Stat.HEALTH);
        for(ElementType e : elements)
        {
            int style = r.nextInt(100);

            //If no defense, skip this element
            if(defense == 0) armor.addElementalDefense(e, 0);
            //Low percentage of defense
            else if(style < 50) armor.addElementalDefense(e, varyP(defense, 10, 40));
            //Percentage of power (defense + health)
            else if(style < 80) armor.addElementalDefense(e, varyP(power, 50, 100));
            //Partial replacement of defense - higher percentage of defense, and a certain amount of defense is removed
            else if(style < 95)
            {
                int transfer = varyP(defense, 50, 80);
                int remove = varyP(transfer, 50, 90);

                armor.addElementalDefense(e, transfer * 2);
                armor.setBoost(Stat.DEFENSE, armor.getBoost(Stat.DEFENSE) - remove);
            }
            //Full replacement of defense - elemental defense is a high multiplier
            else
            {
                armor.addElementalDefense(e, varyP(defense, 200, 400));
                armor.setBoost(Stat.DEFENSE, 0);
            }
        }

        return armor;
    }

    public static LootItem Helmet(int level)
    {
        LootItem helmet = baseArmor(LootType.HELMET, level);

        helmet = basicArmorElementalModifiers(helmet);

        return helmet;
    }

    public static LootItem Chestplate(int level)
    {
        LootItem chestplate = baseArmor(LootType.CHESTPLATE, level);

        chestplate = basicArmorElementalModifiers(chestplate);

        return chestplate;
    }

    public static LootItem Gauntlets(int level)
    {
        LootItem gauntlets = baseArmor(LootType.GAUNTLETS, level);

        gauntlets = basicArmorElementalModifiers(gauntlets);

        return gauntlets;
    }

    public static LootItem Leggings(int level)
    {
        LootItem leggings = baseArmor(LootType.LEGGINGS, level);

        leggings = basicArmorElementalModifiers(leggings);

        return leggings;
    }

    public static LootItem Boots(int level)
    {
        LootItem boots = baseArmor(LootType.BOOTS, level);

        boots = basicArmorElementalModifiers(boots);

        return boots;
    }
}
