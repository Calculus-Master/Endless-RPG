package com.calculusmaster.endlessrpg.gameplay.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public enum LootType
{
    SWORD(CoreLootType.WEAPON, "Omega", "Requiem of the Lost", "Persuasion", "Hopeless Reaver", "Twilight's Defender", "Barbarian Adamantite Sabre", "Knightly Adamantite Broadsword", "Silverlight, Executioner of the Corrupted", "Soul Reaper, Betrayer of Mourning", "Ragnarok, Memory of Eternal Bloodlust");

    private static final Random r = new Random();

    private final CoreLootType coreType;
    private final List<String> names;

    LootType(CoreLootType coreType, String... names)
    {
        this.coreType = coreType;
        this.names = Arrays.asList(names);
    }

    public boolean isWeapon()
    {
        return this.coreType.equals(CoreLootType.WEAPON);
    }

    public boolean isArmor()
    {
        return this.coreType.equals(CoreLootType.ARMOR);
    }

    public boolean isTool()
    {
        return this.coreType.equals(CoreLootType.TOOL);
    }

    public String getRandomName()
    {
        return this.names.isEmpty() ? "NOT NAMED LOOT ITEM" : this.names.get(r.nextInt(this.names.size()));
    }

    public enum CoreLootType
    {
        WEAPON,
        TOOL,
        ARMOR;
    }
}
