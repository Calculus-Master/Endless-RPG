package com.calculusmaster.endlessrpg.gameplay.spells;

import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.character.RPGElementalContainer;
import com.calculusmaster.endlessrpg.gameplay.enums.ElementType;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;

public abstract class Spell
{
    private String name;

    public Spell(String name)
    {
        this.name = name;
    }

    public abstract String execute(RPGCharacter user, RPGCharacter target, RPGCharacter[] battlers, Battle battle);

    protected int calculateDamage(RPGCharacter user, RPGCharacter target)
    {
        RPGElementalContainer userElementalDamage = user.getEquipment().combinedElementalDamage();
        RPGElementalContainer targetElementalDefense = target.getEquipment().combinedElementalDefense();

        int attack = user.getStat(Stat.ATTACK);
        int defense = target.getStat(Stat.DEFENSE);

        int damage = 0;

        for(ElementType element : ElementType.values())
        {
            int eATK = (int)(userElementalDamage.get(element) * attack);
            int eDEF = (int)(targetElementalDefense.get(element) * defense);

            damage += Math.max(0, eATK - eDEF);
        }

        damage += Math.max(0, attack - defense);

        return damage;
    }

    //ID Mappings
    public static final String SIMPLE_ATTACK_SPELL_ID = "DEFAULT_ATTACK";

    public static Spell parse(String spellID)
    {
        return switch(spellID) {
            case SIMPLE_ATTACK_SPELL_ID -> new SimpleAttackSpell();
            default -> new SimpleAttackSpell();
        };
    }
}
