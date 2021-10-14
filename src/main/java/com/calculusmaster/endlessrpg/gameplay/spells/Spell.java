package com.calculusmaster.endlessrpg.gameplay.spells;

import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.character.RPGElementalContainer;
import com.calculusmaster.endlessrpg.gameplay.enums.ElementType;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;

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
