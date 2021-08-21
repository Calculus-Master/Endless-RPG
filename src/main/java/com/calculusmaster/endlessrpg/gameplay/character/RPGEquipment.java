package com.calculusmaster.endlessrpg.gameplay.character;

import com.calculusmaster.endlessrpg.gameplay.enums.EquipmentType;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.mongodb.BasicDBObject;
import org.bson.Document;

public class RPGEquipment
{
    private String helmet;
    private String chestplate;
    private String gauntlets;
    private String leggings;
    private String boots;

    private String leftHand;
    private String rightHand;

    public RPGEquipment(Document equipment)
    {
        for(EquipmentType type : EquipmentType.values()) this.setEquipmentID(type, equipment.getString(type.toString()));
    }

    public RPGEquipment()
    {
        for(EquipmentType type : EquipmentType.values()) this.setEquipmentID(type, LootItem.EMPTY.getLootID());
    }

    public BasicDBObject serialized()
    {
        BasicDBObject equipment = new BasicDBObject();
        for(EquipmentType type : EquipmentType.values()) equipment.put(type.toString(), this.getEquipmentID(type));
        return equipment;
    }

    public String getEquipmentID(EquipmentType type)
    {
        return switch(type) {
            case HELMET -> this.helmet;
            case CHESTPLATE -> this.chestplate;
            case GAUNTLETS -> this.gauntlets;
            case LEGGINGS -> this.leggings;
            case BOOTS -> this.boots;
            case LEFT_HAND -> this.leftHand;
            case RIGHT_HAND -> this.rightHand;
        };
    }

    public void setEquipmentID(EquipmentType type, String lootID)
    {
        switch(type)
        {
            case HELMET -> this.helmet = lootID;
            case CHESTPLATE -> this.chestplate = lootID;
            case GAUNTLETS -> this.gauntlets = lootID;
            case LEGGINGS -> this.leggings = lootID;
            case BOOTS -> this.boots = lootID;
            case LEFT_HAND -> this.leftHand = lootID;
            case RIGHT_HAND -> this.rightHand = lootID;
        }
    }

    public LootItem getEquipmentLoot(EquipmentType type)
    {
        return LootItem.build(this.getEquipmentID(type));
    }

    public void setEquipmentLoot(EquipmentType type, LootItem loot)
    {
        this.setEquipmentID(type, loot.getLootID());
    }

    public boolean isEmpty(EquipmentType type)
    {
        return this.getEquipmentID(type).equals(LootItem.EMPTY.getLootID());
    }
}
