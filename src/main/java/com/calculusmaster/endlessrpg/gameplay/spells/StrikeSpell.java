package com.calculusmaster.endlessrpg.gameplay.spells;

import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;

public class StrikeSpell extends Spell
{
    public StrikeSpell()
    {
        super("Strike");
    }

    @Override
    public String execute(RPGCharacter user, RPGCharacter target, RPGCharacter[] battlers, Battle battle)
    {
        int damage = this.calculateDamage(user, target);

        target.damage(damage);

        return user.getName() + " attacked " + target.getName() + " and dealt " + damage + " damage!";
    }
}
