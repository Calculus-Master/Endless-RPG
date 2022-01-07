package com.calculusmaster.endlessrpg.gameplay.character;

import com.calculusmaster.endlessrpg.gameplay.enums.EquipmentType;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import org.bson.Document;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class RPGEquipment
{
    private LinkedHashMap<EquipmentType, LootItem> equipment;

    public RPGEquipment(Document equipment)
    {
        this();
        for(EquipmentType type : EquipmentType.values()) this.setLoot(type, LootItem.build(equipment.getString(type.toString())));
    }

    public RPGEquipment()
    {
        this.equipment = new LinkedHashMap<>();
        for(EquipmentType type : EquipmentType.values()) this.setLoot(type, LootItem.EMPTY);
    }

    public Document serialized()
    {
        Document equipment = new Document();
        for(EquipmentType type : EquipmentType.values()) equipment.put(type.toString(), this.equipment.get(type).getLootID());
        return equipment;
    }

    public LootItem getLoot(EquipmentType type)
    {
        return this.equipment.get(type);
    }

    public void setLoot(EquipmentType type, LootItem loot)
    {
        this.equipment.put(type, loot);
    }

    public void remove(EquipmentType type)
    {
        this.equipment.put(type, LootItem.EMPTY);
    }

    public void remove(String lootID)
    {
        for(EquipmentType type : EquipmentType.values()) if(this.equipment.get(type).getLootID().equals(lootID)) this.remove(type);
    }

    public RPGElementalContainer combinedElementalDamage()
    {
        RPGElementalContainer damage = new RPGElementalContainer();
        for(EquipmentType type : EquipmentType.values()) if(!this.isEmpty(type)) damage.combine(this.equipment.get(type).getElementalDamage());
        return damage;
    }

    public RPGElementalContainer combinedElementalDefense()
    {
        RPGElementalContainer defense = new RPGElementalContainer();
        for(EquipmentType type : EquipmentType.values()) if(!this.isEmpty(type)) defense.combine(this.equipment.get(type).getElementalDefense());
        return defense;
    }

    public boolean isEmpty(EquipmentType slot)
    {
        return this.equipment.get(slot).isEmpty();
    }

    public List<LootItem> getList()
    {
        return Arrays.stream(EquipmentType.values()).map(this::getLoot).filter(l -> !l.isEmpty()).toList();
    }

    public List<String> getIDList()
    {
        return Arrays.stream(EquipmentType.values()).map(this::getLoot).map(LootItem::getLootID).filter(s -> !s.equals(LootItem.EMPTY.getLootID())).collect(Collectors.toList());
    }
}
