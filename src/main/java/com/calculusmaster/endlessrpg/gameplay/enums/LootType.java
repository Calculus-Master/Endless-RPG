package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.EndlessRPG;
import com.calculusmaster.endlessrpg.gameplay.loot.LootComponentsContainer;
import com.calculusmaster.endlessrpg.util.Global;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static com.calculusmaster.endlessrpg.gameplay.enums.LootComponentType.*;

public enum LootType
{
    //Empty
    NONE(null, "", new LootComponentsContainer()),

    //Weapons
    SWORD(CoreLootType.WEAPON, "sword_names", new LootComponentsContainer(BASIC_HANDLE, BASIC_BINDING, SWORD_BLADE)),
    WAND(CoreLootType.WEAPON, "wand_names", new LootComponentsContainer(BASIC_HANDLE, WAND_CORE)),

    //Defensive Weapons
    SHIELD(CoreLootType.WEAPON, "shield_names", new LootComponentsContainer()),

    //Tools TODO: Add Tool Names, Figure out name for Foraging Tool
    PICKAXE(CoreLootType.TOOL, "tool_names", new LootComponentsContainer()),
    FORAGING(CoreLootType.TOOL, "tool_names", new LootComponentsContainer()),
    ROD(CoreLootType.TOOL, "tool_names", new LootComponentsContainer()),
    AXE(CoreLootType.TOOL, "tool_names", new LootComponentsContainer()),
    HOE(CoreLootType.TOOL, "tool_names", new LootComponentsContainer()),

    //Armor
    HELMET(CoreLootType.HELMET, "helmet_names", new LootComponentsContainer()),
    CHESTPLATE(CoreLootType.CHESTPLATE, "chestplate_names", new LootComponentsContainer()),
    GAUNTLETS(CoreLootType.GAUNTLETS, "gauntlets_names", new LootComponentsContainer()),
    LEGGINGS(CoreLootType.LEGGINGS, "leggings_names", new LootComponentsContainer()),
    BOOTS(CoreLootType.BOOTS, "boots_names", new LootComponentsContainer())

    ;

    private static final Random r = new Random();

    private final CoreLootType coreType;
    private final String namesFile;
    private final LootComponentsContainer craftingComponents;

    LootType(CoreLootType coreType, String namesFile, LootComponentsContainer craftingComponents)
    {
        this.coreType = coreType;
        this.namesFile = namesFile;
        this.craftingComponents = craftingComponents;
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

    public LootComponentsContainer getCraftingComponents()
    {
        return this.craftingComponents;
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
        catch (NullPointerException e)
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
