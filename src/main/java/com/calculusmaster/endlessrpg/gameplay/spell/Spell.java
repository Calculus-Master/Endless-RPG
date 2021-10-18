package com.calculusmaster.endlessrpg.gameplay.spell;

import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.character.RPGElementalContainer;
import com.calculusmaster.endlessrpg.gameplay.enums.*;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;

import java.util.Arrays;
import java.util.List;

public abstract class Spell
{
    public abstract String execute(RPGCharacter user, RPGCharacter target, RPGCharacter[] battlers, Battle battle);

    public abstract String getName();

    public abstract String getDescription();

    protected int calculateDamage(RPGCharacter user, RPGCharacter target, Battle battle)
    {
        Weather weather = battle.getLocation().getWeather();
        Time time = battle.getLocation().getTime();

        int attack = user.getStat(Stat.ATTACK);
        int defense = target.getStat(Stat.DEFENSE);

        //Weapon Affinity Stats
        List<LootItem> weapons = Arrays.asList(user.getEquipment().getEquipmentLoot(EquipmentType.LEFT_HAND), user.getEquipment().getEquipmentLoot(EquipmentType.RIGHT_HAND));

        for(LootItem l : weapons)
        {
            switch(l.getLootType())
            {
                case SWORD -> attack += (int)(attack * (user.getStat(Stat.STRENGTH) / 100.0));
                case WAND -> attack += (int)(attack * (user.getStat(Stat.INTELLECT) / 100.0));
            }
        }

        int damage = 0;

        RPGElementalContainer userEquipmentED = user.getEquipment().combinedElementalDamage();
        RPGElementalContainer targetEquipmentED = target.getEquipment().combinedElementalDefense();

        //Elemental Damage
        for(ElementType element : ElementType.values())
        {
            //Equipment - Flat values
            int equipmentATK = userEquipmentED.get(element);
            int equipmentDEF = targetEquipmentED.get(element);

            //Core - Percent of Attack/Defense
            double coreATK = user.getCoreElementalDamage().percent(element);
            double coreDEF = target.getCoreElementalDefense().percent(element);

            //Class - Percent Modifier of Total Elemental Attack/Defense
            double classATK = user.getRPGClass().getElementalDamageModifiers().getOrDefault(element, 1.0f);
            double classDEF = target.getRPGClass().getElementalDefenseModifiers().getOrDefault(element, 1.0f);

            int elementalATK = (int)(classATK * (equipmentATK + coreATK * attack));
            int elementalDEF = (int)(classDEF * (equipmentDEF + coreDEF * attack));

            switch(element)
            {
                case LIGHT -> {
                    if(List.of(Weather.OVERCAST, Weather.RAIN).contains(weather))
                    {
                        elementalATK *= 0.9;
                        elementalDEF *= 0.9;
                    }

                    if(time.equals(Time.DAY))
                    {
                        elementalATK *= 1.02;
                        elementalDEF *= 1.04;
                    }
                }
                case DARK -> {
                    if(time.equals(Time.NIGHT))
                    {
                        elementalATK *= 1.04;
                        elementalDEF *= 1.02;
                    }
                }
                case WATER -> {
                    if(List.of(Weather.RAIN).contains(weather))
                    {
                        elementalATK *= 1.1;
                        elementalDEF *= 1.2;
                    }
                }
            }

            damage += Math.max(0, elementalATK - elementalDEF);
        }

        //Raw Attack and Defense
        damage += Math.max(0, attack - defense);

        return damage;
    }
}
