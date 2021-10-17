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

        RPGElementalContainer userElementalDamage = user.getEquipment().combinedElementalDamage();
        RPGElementalContainer targetElementalDefense = target.getEquipment().combinedElementalDefense();

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

        //Equipment
        for(ElementType element : ElementType.values())
        {
            int eATK = userElementalDamage.get(element);
            int eDEF = targetElementalDefense.get(element);

            switch(element)
            {
                case LIGHT -> {
                    if(List.of(Weather.OVERCAST, Weather.RAIN).contains(weather))
                    {
                        eATK *= 0.9;
                        eDEF *= 0.9;
                    }

                    if(time.equals(Time.DAY))
                    {
                        eATK *= 1.02;
                        eDEF *= 1.04;
                    }
                }
                case DARK -> {
                    if(time.equals(Time.NIGHT))
                    {
                        eATK *= 1.04;
                        eDEF *= 1.02;
                    }
                }
                case WATER -> {
                    if(List.of(Weather.RAIN).contains(weather))
                    {
                        eATK *= 1.1;
                        eDEF *= 1.2;
                    }
                }
            }

            damage += Math.max(0, eATK - eDEF);
        }

        //Core Elemental Stats
        for(ElementType element : ElementType.values())
        {
            int eATK = (int)(user.getCoreElementalDamage().percent(element) * attack);
            int eDEF = (int)(target.getCoreElementalDefense().percent(element) * defense);

            damage += Math.max(0, eATK - eDEF);
        }

        //Raw Attack and Defense
        damage += Math.max(0, attack - defense);

        return damage;
    }
}
