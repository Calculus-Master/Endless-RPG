package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.EndlessRPG;
import com.calculusmaster.endlessrpg.util.Global;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public enum LootType
{
    NONE(null, ""),
    SWORD(CoreLootType.WEAPON, "sword_names"),
    WAND(CoreLootType.WEAPON, "wand_names"),
    SHIELD(CoreLootType.WEAPON, "shield_names"),
    HELMET(CoreLootType.HELMET, "helmet_names"),
    CHESTPLATE(CoreLootType.CHESTPLATE, "chestplate_names"),
    GAUNTLETS(CoreLootType.GAUNTLETS, "gauntlets_names"),
    LEGGINGS(CoreLootType.LEGGINGS, "leggings_names"),
    BOOTS(CoreLootType.BOOTS, "boots_names");

    private static final Random r = new Random();

    private final CoreLootType coreType;
    private final String namesFile;

    LootType(CoreLootType coreType, String namesFile)
    {
        this.coreType = coreType;
        this.namesFile = namesFile;
    }

    public boolean isWeapon()
    {
        return this.coreType.equals(CoreLootType.WEAPON);
    }

    public boolean isArmor()
    {
        return Arrays.asList(CoreLootType.HELMET, CoreLootType.CHESTPLATE, CoreLootType.GAUNTLETS, CoreLootType.LEGGINGS, CoreLootType.BOOTS).contains(this.coreType);
    }

    public boolean isTool()
    {
        return this.coreType.equals(CoreLootType.TOOL);
    }

    public CoreLootType getCore()
    {
        return this.coreType;
    }

    public static LootType getRandom()
    {
        LootType lootType;

        do { lootType = LootType.values()[r.nextInt(LootType.values().length)]; }
        while(lootType.equals(LootType.NONE));

        return lootType;
    }

    public String getRandomName()
    {
        try
        {
            List<String> pool = new BufferedReader(new InputStreamReader(Objects.requireNonNull(EndlessRPG.class.getResourceAsStream("/names/" + this.namesFile + ".txt")))).lines().toList();
            return pool.get(r.nextInt(pool.size()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "UNNAMED LOOT ITEM";
        }
    }

    public static LootType cast(String input)
    {
        return Global.castEnum(input, values());
    }

    public enum CoreLootType
    {
        WEAPON,
        TOOL,
        HELMET,
        CHESTPLATE,
        GAUNTLETS,
        LEGGINGS,
        BOOTS;
    }
}
