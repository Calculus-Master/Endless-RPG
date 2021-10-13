package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.util.Global;

import java.util.Arrays;
import java.util.List;

public enum EquipmentType
{
    HELMET(LootType.CoreLootType.HELMET),
    CHESTPLATE(LootType.CoreLootType.CHESTPLATE),
    GAUNTLETS(LootType.CoreLootType.GAUNTLETS),
    LEGGINGS(LootType.CoreLootType.LEGGINGS),
    BOOTS(LootType.CoreLootType.BOOTS),
    LEFT_HAND(LootType.CoreLootType.WEAPON, LootType.CoreLootType.TOOL),
    RIGHT_HAND(LootType.CoreLootType.WEAPON, LootType.CoreLootType.TOOL);

    private List<LootType.CoreLootType> validLoot;

    EquipmentType(LootType.CoreLootType... validLoot)
    {
        this.validLoot = Arrays.asList(validLoot);
    }

    public boolean isHand()
    {
        return this.equals(LEFT_HAND) || this.equals(RIGHT_HAND);
    }

    public boolean isValidLoot(LootType loot)
    {
        return this.validLoot.contains(loot.getCore());
    }

    public static EquipmentType cast(String input)
    {
        return Global.castEnum(input, values());
    }

    public static EquipmentType parse(String input)
    {
        return switch(input.toLowerCase()) {
            case "helmet", "helm", "h" -> EquipmentType.HELMET;
            case "chestplate", "chest", "c" -> EquipmentType.CHESTPLATE;
            case "gauntlets", "gloves", "g" -> EquipmentType.GAUNTLETS;
            case "leggings", "legs", "l" -> EquipmentType.LEGGINGS;
            case "boots", "b" -> EquipmentType.BOOTS;
            case "left_hand", "lh", "left" -> EquipmentType.LEFT_HAND;
            case "right_hand", "rh", "right" -> EquipmentType.RIGHT_HAND;
            default -> null;
        };
    }

    public String getStyledName()
    {
        return Global.normalize(this.toString().replaceAll("_", " "));
    }
}
