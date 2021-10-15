package com.calculusmaster.endlessrpg.gameplay.spell;

import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.character.RPGElementalContainer;
import com.calculusmaster.endlessrpg.gameplay.enums.ElementType;
import com.calculusmaster.endlessrpg.gameplay.enums.EquipmentType;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;

public abstract class Spell
{
    public abstract String execute(RPGCharacter user, RPGCharacter target, RPGCharacter[] battlers, Battle battle);

    public abstract String getName();

    public abstract String getDescription();

    protected int calculateDamage(RPGCharacter user, RPGCharacter target)
    {
        RPGElementalContainer userElementalDamage = user.getEquipment().combinedElementalDamage();
        RPGElementalContainer targetElementalDefense = target.getEquipment().combinedElementalDefense();

        int attack = user.getStat(Stat.ATTACK);
        int defense = target.getStat(Stat.DEFENSE);

        //Weapon Affinity Stats
        LootItem leftHand = user.getEquipment().getEquipmentLoot(EquipmentType.LEFT_HAND);
        LootItem rightHand = user.getEquipment().getEquipmentLoot(EquipmentType.RIGHT_HAND);

        if(!leftHand.isEmpty())
        {
            switch(leftHand.getLootType())
            {
                case SWORD -> attack += (int)(attack * (user.getStat(Stat.STRENGTH) / 100.0));
                case WAND -> attack += (int)(attack * (user.getStat(Stat.INTELLECT) / 100.0));
            }
        }

        if(!rightHand.isEmpty())
        {
            switch(rightHand.getLootType())
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
