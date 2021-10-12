package com.calculusmaster.endlessrpg.gameplay.spells;

import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;

public class SimpleAttackSpell extends Spell
{
    public SimpleAttackSpell()
    {
        super("Attack");
    }

    @Override
    public String getSpellID()
    {
        return Spell.SIMPLE_ATTACK_SPELL_ID;
    }

    @Override
    public String execute(RPGCharacter user, RPGCharacter target, RPGCharacter[] battlers, Battle battle)
    {
        int damage = user.getDamage(target);

        target.damage(damage);

        return user.getName() + " attacked " + target.getName() + " and dealt " + damage + " damage!";
    }
}
