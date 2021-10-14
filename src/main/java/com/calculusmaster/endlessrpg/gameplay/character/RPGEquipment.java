package com.calculusmaster.endlessrpg.gameplay.character;

import com.calculusmaster.endlessrpg.gameplay.enums.EquipmentType;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.mongodb.BasicDBObject;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    public void remove(EquipmentType type)
    {
        this.setEquipmentID(type, LootItem.EMPTY.getLootID());
    }

    public void remove(String lootID)
    {
        for(EquipmentType type : EquipmentType.values()) if(this.getEquipmentID(type).equals(lootID)) this.remove(type);
    }

    public LootItem getEquipmentLoot(EquipmentType type)
    {
        return LootItem.build(this.getEquipmentID(type));
    }

    public RPGElementalContainer combinedElementalDamage()
    {
        RPGElementalContainer damage = new RPGElementalContainer();
        for(EquipmentType equipment : EquipmentType.values())
        {
            if(!this.isEmpty(equipment))
            {
                damage.combine(this.getEquipmentLoot(equipment).getElementalDamage());
            }
        }
        return damage;
    }

    public RPGElementalContainer combinedElementalDefense()
    {
        RPGElementalContainer defense = new RPGElementalContainer();
        for(EquipmentType equipment : EquipmentType.values()) if(!this.isEmpty(equipment)) defense.combine(this.getEquipmentLoot(equipment).getElementalDefense());
        return defense;
    }

    public boolean isEmpty(EquipmentType slot)
    {
        return this.getEquipmentID(slot).equals(LootItem.EMPTY.getLootID());
    }

    public List<String> asList()
    {
        return Arrays.stream(EquipmentType.values()).map(this::getEquipmentID).collect(Collectors.toList());
    }
}
