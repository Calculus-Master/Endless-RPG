package com.calculusmaster.endlessrpg.gameplay.spell;

import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.character.RPGElementalContainer;
import com.calculusmaster.endlessrpg.gameplay.enums.*;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;

import java.util.List;
import java.util.SplittableRandom;

public abstract class Spell
{
    public abstract String execute(RPGCharacter user, RPGCharacter target, RPGCharacter[] battlers, Battle battle);

    public abstract String getName();

    public abstract String getDescription();

    protected int calculateDamage(RPGCharacter user, RPGCharacter target, Battle battle)
    {
        final SplittableRandom r = new SplittableRandom();

        Weather weather = battle.getLocation().getWeather();
        Time time = battle.getLocation().getTime();

        int attack = user.getStat(Stat.ATTACK);
        int defense = target.getStat(Stat.DEFENSE);

        //Weapon Affinity Stats
        List<LootItem> weapons = List.of(user.getEquipment().getLoot(EquipmentType.LEFT_HAND), user.getEquipment().getLoot(EquipmentType.RIGHT_HAND));

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

            //If neither Character has Elemental ATK nor DEF, or the Attacker doesn't have Elemental ATK, just skip this element
            if(equipmentATK == 0) continue;

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

                    if(List.of(Weather.HARSH_SUN).contains(weather))
                    {
                        elementalATK *= 1.2;
                        elementalDEF *= 1.2;
                    }

                    if(List.of(Weather.FOG).contains(weather))
                    {
                        elementalATK *= 1.2;
                        elementalDEF *= 1.15;
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

                    if(List.of(Weather.HARSH_SUN).contains(weather))
                    {
                        elementalATK *= 0.9;
                        elementalDEF *= 0.7;
                    }
                }
                case FIRE -> {
                    if(List.of(Weather.HARSH_SUN).contains(weather))
                    {
                        elementalATK *= 1.1;
                        elementalDEF *= 1.1;
                    }
                }
            }

            damage += Math.max(0, elementalATK - elementalDEF);
        }

        //Raw Attack and Defense
        damage += Math.max(0, attack - defense);

        //Other Modifiers
        if(weather.equals(Weather.FOG) && r.nextInt(100) < 25) damage *= (int)(damage * 0.75);

        return damage;
    }
}
