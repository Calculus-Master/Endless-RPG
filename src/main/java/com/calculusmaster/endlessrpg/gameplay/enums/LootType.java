package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.EndlessRPG;
import com.calculusmaster.endlessrpg.util.Global;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public enum LootType
{
    NONE(null, ""),
    SWORD(CoreLootType.WEAPON, "sword_names"),
    HELMET(CoreLootType.HELMET, "helmet_names"),
    CHESTPLATE(CoreLootType.HELMET, "chestplate_names"),
    GAUNTLETS(CoreLootType.HELMET, "gauntlets_names"),
    LEGGINGS(CoreLootType.HELMET, "leggings_names"),
    BOOTS(CoreLootType.HELMET, "boots_names");

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

    public String getRandomName()
    {
        try
        {
            List<String> pool = Files.lines(Paths.get(EndlessRPG.class.getResource("/names/" + this.namesFile + ".txt").toURI())).toList();
            return pool.get(r.nextInt(pool.size()));
        }
        catch (Exception e)
        {
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
