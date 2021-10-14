package com.calculusmaster.endlessrpg.gameplay.spell.spells;

import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.spell.Spell;

public class StrikeSpell extends Spell
{
    @Override
    public String execute(RPGCharacter user, RPGCharacter target, RPGCharacter[] battlers, Battle battle)
    {
        int damage = this.calculateDamage(user, target);

        target.damage(damage);

        return user.getName() + " attacked " + target.getName() + " and dealt " + damage + " damage!";
    }

    @Override
    public String getName()
    {
        return "Strike";
    }

    @Override
    public String getDescription()
    {
        return "Simple Attack on the target.";
    }
}
