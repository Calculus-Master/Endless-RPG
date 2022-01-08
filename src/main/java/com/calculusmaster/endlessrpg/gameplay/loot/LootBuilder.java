package com.calculusmaster.endlessrpg.gameplay.loot;

import com.calculusmaster.endlessrpg.gameplay.enums.ElementType;
import com.calculusmaster.endlessrpg.gameplay.enums.LootTag;
import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.gameplay.resources.enums.RawResource;

import java.util.*;

public class LootBuilder
{
    public static final SplittableRandom r = new SplittableRandom();

    public static LootItem create(LootType type, int level)
    {
        LootItem out = switch(type) {
            case SWORD -> LootBuilder.RandomSword(level);
            case WAND -> LootBuilder.RandomWand(level);

            case SHIELD -> LootBuilder.RandomShield(level);

            case PICKAXE, FORAGING, ROD, AXE, HOE -> LootBuilder.createTool(type, level, r.nextInt(RawResource.MAX_TIER) + 1);

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

    public static LootItem createCrafted(LootType type, List<LootComponent> components, int level, String name)
    {
        LootItem loot = switch(type) {
            case SWORD -> LootBuilder.CraftedSword(level, components);
            //TODO: Temporarily just creates LootItem
            default -> LootItem.create(type);
        };

        if(name != null) loot.setName(name);

        loot.getRequirements().addLevel(level);
        loot.addTag(LootTag.CRAFTED, true);
        return loot;
    }

    public static LootItem createTool(LootType type, int level, int tier)
    {
        LootItem tool = LootItem.create(type);

        Stat boost = switch(type) {
            case PICKAXE -> Stat.MINING_POWER;
            case FORAGING -> Stat.FORAGING_POWER;
            case ROD -> Stat.FISHING_POWER;
            case AXE -> Stat.WOODCUTTING_POWER;
            case HOE -> Stat.FARMING_POWER;
            default -> throw new IllegalArgumentException("LootType " + type + " is not a Tool!");
        };

        //Base Power (based on Tier)
        int power = switch(tier) {
            case 1 -> 500;
            case 2 -> 1000;
            case 3 -> 2000;
            case 4 -> 4000;
            case 5 -> 8000;
            case 6 -> 16000;
            case 7 -> 24000;
            case 8 -> 40000;
            case 9 -> 64000;
            case 10 -> 100000;
            default -> throw new IllegalArgumentException("Invalid Tier! (" + tier + ")");
        } / 10;

        //Power Modifier (based on Level)
        power *= 1 + level * 0.03;

        tool.setBoost(boost, power);

        return tool;
    }

    //Helper

    private static int varyP(int input, int low, int high)
    {
        return (int)((r.nextInt(low, high + 1)) / 100.0 * input);
    }

    private static int varyV(int input, int low, int high)
    {
        return r.nextInt(input - low, input + high + 1);
    }

    private static int base(int level)
    {
        return r.nextInt(1, 5) + level;
    }

    private static int standard(int level)
    {
        return varyP(base(level), 90, 110);
    }

    private static int armor(int level)
    {
        return varyP(level, 110, 120);
    }

    //Weapons

    public static LootItem RandomSword(int level)
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

    public static LootItem CraftedSword(int level, List<LootComponent> components)
    {
        return RandomSword(level);
    }

    public static LootItem RandomWand(int level)
    {
        int s = standard(level);

        LootItem wand = LootItem.create(LootType.WAND)
                .setBoost(Stat.ATTACK, (int)(0.25 * s))
                .addElementalDamage(ElementType.getRandom(), (int)(0.75 * s));

        wand.getRequirements()
                .addStat(Stat.INTELLECT, varyV(level * 2,  level / 2, level * 3));

        return wand;
    }

    public static LootItem RandomShield(int level)
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

        LootStyle style = new WeightedRandom<LootStyle>()
                .with(LootStyle.ARMOR_BASE_POOLED, 3)
                .with(LootStyle.ARMOR_BASE_INDEPENDENT, 2)
                .with(LootStyle.ARMOR_BASE_SYNERGY, 5)
                .pull();

        int defense = 0, health = 0;

        switch(style)
        {
            //"Pooled" Method: 1 armor value, split into defense and health boosts
            case ARMOR_BASE_POOLED -> {
                int value = armor(level);
                int percDef = r.nextInt(100) + 1;

                defense = varyP((int)(value * percDef / 100.0), 80, 120);
                health = varyP((int)(value * (1 - percDef) / 100.0), 80, 120);
            }
            //"Independent" Method: 2 armor values, random percentage of each becomes a boost
            case ARMOR_BASE_INDEPENDENT -> {
                int defVal = armor(level);
                int hpVal = armor(level);

                defense = varyP(defVal, 5, 75);
                health = varyP(hpVal, 5, 75);
            }
            //"Synergy" Method: Armor is synergized as HEALTH or DEFENSE, the other stat is randomly chosen to get boosted
            case ARMOR_BASE_SYNERGY -> {
                boolean isDefense = r.nextInt(100) < 50;

                int primaryValue = armor(level);
                int secondaryValue = r.nextInt(10) < 3 ? varyP(primaryValue, 2, 10) : 0;

                defense = isDefense ? primaryValue : secondaryValue;
                health = isDefense ? secondaryValue : primaryValue;
            }
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
            LootStyle style = new WeightedRandom<LootStyle>()
                    .with(LootStyle.ELEMENTAL_ARMOR_LOW_PERCENT, 5)
                    .with(LootStyle.ELEMENTAL_ARMOR_POWER_PERCENT, 4)
                    .with(LootStyle.ELEMENTAL_ARMOR_PARTIAL_REPLACEMENT, 2)
                    .with(LootStyle.ELEMENTAL_ARMOR_FULL_REPLACEMENT, 1)
                    .pull();

            switch(style)
            {
                //Low percentage of defense
                case ELEMENTAL_ARMOR_LOW_PERCENT -> armor.addElementalDefense(e, varyP(defense, 10, 40));
                //Percentage of power (defense + health)
                case ELEMENTAL_ARMOR_POWER_PERCENT -> armor.addElementalDefense(e, varyP(power, 50, 100));
                //Partial replacement of defense - higher percentage of defense, and a certain amount of defense is removed
                case ELEMENTAL_ARMOR_PARTIAL_REPLACEMENT -> {
                    int transfer = varyP(defense, 50, 80);
                    int remove = varyP(transfer, 50, 90);

                    armor.addElementalDefense(e, transfer * 2);
                    armor.setBoost(Stat.DEFENSE, armor.getBoost(Stat.DEFENSE) - remove);
                }
                //Full replacement of defense - elemental defense is a high multiplier
                case ELEMENTAL_ARMOR_FULL_REPLACEMENT -> {
                    armor.addElementalDefense(e, varyP(defense, 200, 400));
                    armor.setBoost(Stat.DEFENSE, 0);
                }
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

    private enum LootStyle
    {
        ARMOR_BASE_POOLED,
        ARMOR_BASE_INDEPENDENT,
        ARMOR_BASE_SYNERGY,
        ELEMENTAL_ARMOR_LOW_PERCENT,
        ELEMENTAL_ARMOR_POWER_PERCENT,
        ELEMENTAL_ARMOR_PARTIAL_REPLACEMENT,
        ELEMENTAL_ARMOR_FULL_REPLACEMENT;
    }

    private static class WeightedRandom<T>
    {
        private final SplittableRandom r;
        private final Map<T, Integer> weights;

        WeightedRandom()
        {
            this.r = new SplittableRandom();
            this.weights = new HashMap<>();
        }

        WeightedRandom<T> with(T item, int weight)
        {
            this.weights.put(item, weight);
            return this;
        }

        T pull()
        {
            List<T> pool = new ArrayList<>();
            for(Map.Entry<T, Integer> entry : this.weights.entrySet()) for(int i = 0; i < entry.getValue(); i++) pool.add(entry.getKey());

            return pool.get(this.r.nextInt(pool.size()));
        }
    }
}
