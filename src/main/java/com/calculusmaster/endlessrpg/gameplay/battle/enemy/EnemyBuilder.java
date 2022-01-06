package com.calculusmaster.endlessrpg.gameplay.battle.enemy;

import com.calculusmaster.endlessrpg.EndlessRPG;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.*;
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
    public static RPGCharacter createDragon(int level)
    {
        //TODO: Improve this and the Ruler, Dragons don't have any equipment
        RPGCharacter dragon = RPGCharacter.create(EnemyBuilder.getRandomName("dragon_names"));

        EnemyBuilder.setNaturalLevel(dragon, level * 10);

        dragon.increaseCoreStat(Stat.HEALTH, 200 + level * 5);

        ElementType affinity = ElementType.getRandom();

        for(ElementType element : ElementType.values())
        {
            int damage = new SplittableRandom().nextInt(level * 5, level * (affinity.equals(element) ? 15 : 7));
            dragon.getCoreElementalDamage().set(element, damage);

            int defense = new SplittableRandom().nextInt(level * 6, level * (affinity.equals(element) ? 15 : 9));
            dragon.getCoreElementalDefense().set(element, defense);
        }

        return dragon;
    }

    public static RPGCharacter createRuler(int level)
    {
        RPGCharacter ruler = RPGCharacter.create("Ruler");
        boolean magic = new SplittableRandom().nextInt(100) < 50;

        EnemyBuilder.setNaturalLevel(ruler, level + 15);
        EnemyBuilder.defaultClass(ruler); //TODO: Ruler Class

        LootItem weapon;
        if(magic)
        {
            weapon = LootBuilder.RandomWand(ruler.getLevel())
                    .setBoost(Stat.INTELLECT, ruler.getLevel() * 2)
                    .addElementalDamage(ElementType.getRandom(), ruler.getStat(Stat.ATTACK) / 2)
                    .addElementalDamage(ElementType.getRandom(), ruler.getStat(Stat.ATTACK) / 2)
                    .addElementalDefense(ElementType.getRandom(), ruler.getStat(Stat.ATTACK) / 2);
        }
        else
        {
            weapon = LootBuilder.RandomSword(ruler.getLevel())
                    .setBoost(Stat.STRENGTH, ruler.getLevel() * 3)
                    .addElementalDamage(ElementType.getRandom(), ruler.getStat(Stat.ATTACK) / 2);
        }

        weapon.upload();
        ruler.equipLoot(EquipmentType.RIGHT_HAND, weapon.getLootID());

        List<LootType> armorTypes = Arrays.asList(LootType.HELMET, LootType.CHESTPLATE, LootType.GAUNTLETS, LootType.LEGGINGS, LootType.BOOTS);
        for(LootType loot : armorTypes)
        {
            LootItem armor = LootBuilder.create(loot, ruler.getLevel())
                    .setBoost(Stat.HEALTH, 75)
                    .addElementalDefense(ElementType.getRandom(), 40)
                    .addElementalDefense(ElementType.getRandom(), 40)
                    .addElementalDefense(ElementType.getRandom(), 40)
                    .addElementalDefense(ElementType.getRandom(), 40);

            armor.upload();
            ruler.equipLoot(EquipmentType.values()[armorTypes.indexOf(loot)], armor.getLootID());
        }

        return ruler;
    }

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
    private static void setNaturalLevel(RPGCharacter enemy, int target)
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
        EnemyBuilder.setNaturalLevel(enemy, Math.max(1, new SplittableRandom().nextInt(level - 1, level + 2)));
    }

    private static void defaultWeapons(RPGCharacter enemy, int level)
    {
        //Weapon
        int weaponLevel = enemy.getLevel() > 3 ? new SplittableRandom().nextInt(enemy.getLevel() - 2, enemy.getLevel() + 2) : enemy.getLevel();

        List<LootType> weaponPool = Arrays.asList(LootType.SWORD, LootType.WAND);
        LootItem weapon = LootBuilder.create(weaponPool.get(new SplittableRandom().nextInt(weaponPool.size())), weaponLevel);

        weapon.upload();

        switch(weapon.getLootType())
        {
            case SWORD, WAND -> {
                enemy.equipLoot(EquipmentType.RIGHT_HAND, weapon.getLootID());

                if(level > 25 && new SplittableRandom().nextInt(100) < 25)
                {
                    LootItem shield = LootBuilder.create(LootType.SHIELD, weaponLevel + 1);
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
            LootItem armor = LootBuilder.create(armorType, armorLevel);

            armor.upload();
            enemy.equipLoot(EquipmentType.values()[i], armor.getLootID());
        }
    }
}
