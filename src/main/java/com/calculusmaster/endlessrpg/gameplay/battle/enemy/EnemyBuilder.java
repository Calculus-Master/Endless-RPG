package com.calculusmaster.endlessrpg.gameplay.battle.enemy;

import com.calculusmaster.endlessrpg.EndlessRPG;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.EquipmentType;
import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.enums.RPGClass;
import com.calculusmaster.endlessrpg.gameplay.loot.LootBuilder;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
        RPGCharacter enemy = RPGCharacter.create(EnemyBuilder.getRandomName("bot_names"));

        EnemyBuilder.defaultClass(enemy);
        EnemyBuilder.defaultLevel(enemy, level);
        EnemyBuilder.defaultWeapons(enemy, level);
        EnemyBuilder.defaultArmor(enemy, level);

        return enemy;
    }

    //Helper
    private static void setLevel(RPGCharacter enemy, int target)
    {
        while(enemy.getLevel() < target) enemy.addExp(enemy.getExpRequired(enemy.getLevel() + 1));
    }

    private static String getRandomName(String file)
    {
        List<String> pool = new BufferedReader(new InputStreamReader(Objects.requireNonNull(EndlessRPG.class.getResourceAsStream("/names/" + file + ".txt")))).lines().toList();
        return pool.get(new SplittableRandom().nextInt(pool.size()));
    }

    //Default
    private static void defaultClass(RPGCharacter enemy)
    {
        enemy.setRPGClass(Arrays.copyOfRange(RPGClass.values(), 1, RPGClass.values().length)[new SplittableRandom().nextInt(RPGClass.values().length - 1)]);
    }

    private static void defaultLevel(RPGCharacter enemy, int level)
    {
        EnemyBuilder.setLevel(enemy, Math.max(1, new SplittableRandom().nextInt(level - 1, level + 2)));
    }

    private static void defaultWeapons(RPGCharacter enemy, int level)
    {
        //Weapon
        int weaponLevel = enemy.getLevel() + 1 + new SplittableRandom().nextInt(3);

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
    }

    private static void defaultArmor(RPGCharacter enemy, int level)
    {
        //Armor
        List<LootType> armorPool = Arrays.asList(LootType.HELMET, LootType.CHESTPLATE, LootType.GAUNTLETS, LootType.LEGGINGS, LootType.BOOTS);

        int armorLevel = Math.max(1, enemy.getLevel() - 1 + new SplittableRandom().nextInt(3));

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
    }
}
