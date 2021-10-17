package com.calculusmaster.endlessrpg.gameplay.battle;

import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.EquipmentType;
import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.enums.RPGClass;
import com.calculusmaster.endlessrpg.gameplay.loot.LootBuilder;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;

import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;

public class EnemyBuilder 
{
    //General
    public static void delete(RPGCharacter c)
    {
        for(EquipmentType type : EquipmentType.values())
        {
            String lootID = c.getEquipment().getEquipmentID(type);
            if(!lootID.equals(LootItem.EMPTY.getLootID())) LootItem.delete(lootID);
        }
    }
    
    //Archetypes
    public static RPGCharacter createDefault(int level)
    {
        RPGCharacter enemy = RPGCharacter.create("Adventure AI");

        final SplittableRandom r = new SplittableRandom();

        //Class - Not a Recruit
        enemy.setRPGClass(Arrays.copyOfRange(RPGClass.values(), 1, RPGClass.values().length)[r.nextInt(RPGClass.values().length - 1)]);

        //Level
        int targetLevel = Math.max(1, r.nextInt(2) + level - 1);
        while(enemy.getLevel() < targetLevel) enemy.addExp(enemy.getExpRequired(enemy.getLevel() + 1));

        //Weapon (TODO: Pick between different kinds of weapons)
        int weaponLevel = enemy.getLevel() + 1 + r.nextInt(3);
        List<LootType> weaponPool = Arrays.asList(LootType.SWORD, LootType.WAND);
        LootItem weapon = LootBuilder.reward(weaponPool.get(new SplittableRandom().nextInt(weaponPool.size())), weaponLevel);

        weapon.upload();
        switch(weapon.getLootType())
        {
            case SWORD, WAND -> {
                enemy.equipLoot(EquipmentType.RIGHT_HAND, weapon.getLootID());

                if(level > 25 && new SplittableRandom().nextInt(100) < 25)
                {
                    LootItem shield = LootBuilder.reward(LootType.SHIELD, weaponLevel + 1);
                    shield.upload();

                    enemy.equipLoot(EquipmentType.LEFT_HAND, shield.getLootID());
                }
            }
        }

        //Armor
        List<LootType> armorPool = Arrays.asList(LootType.HELMET, LootType.CHESTPLATE, LootType.GAUNTLETS, LootType.LEGGINGS, LootType.BOOTS);
        int armorLevel = Math.max(1, enemy.getLevel() - 1 + r.nextInt(3));
        int armorCount;
        if(level < 5) armorCount = 1;
        else if(level < 15) armorCount = 2;
        else if(level < 30) armorCount = 3;
        else if(level < 50) armorCount = 4;
        else armorCount = 5;
        for(int i = 0; i < armorCount; i++)
        {
            LootType armorType = armorPool.get(i);
            LootItem armor = LootBuilder.reward(armorType, armorLevel);

            armor.upload();
            enemy.equipLoot(EquipmentType.values()[i], armor.getLootID());
        }

        return enemy;
    }
}
