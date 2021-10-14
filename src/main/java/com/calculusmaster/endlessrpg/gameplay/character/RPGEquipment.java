package com.calculusmaster.endlessrpg.gameplay.character;

import com.calculusmaster.endlessrpg.gameplay.enums.EquipmentType;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.mongodb.BasicDBObject;
import org.bson.Document;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RPGEquipment
{
    private Map<EquipmentType, String> equipment;

    public RPGEquipment(Document equipment)
    {
        this();
        for(EquipmentType type : EquipmentType.values()) this.setEquipmentID(type, equipment.getString(type.toString()));
    }

    public RPGEquipment()
    {
        this.equipment = new HashMap<>();
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
        return this.equipment.get(type);
    }

    public void setEquipmentID(EquipmentType type, String lootID)
    {
        this.equipment.put(type, lootID);
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
        return Arrays.stream(EquipmentType.values()).map(this::getEquipmentID).filter(s -> !s.equals(LootItem.EMPTY.getLootID())).collect(Collectors.toList());
    }
}
