package com.calculusmaster.endlessrpg.gameplay.spell.spells;

import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.gameplay.spell.Spell;

public class StrengthenSpell extends Spell
{
    @Override
    public String execute(RPGCharacter user, RPGCharacter target, RPGCharacter[] battlers, Battle battle)
    {
        int attackBoost = (int)(0.2 * user.getStat(Stat.ATTACK));

        user.addChange(Stat.ATTACK, attackBoost);

        return user.getName() + " boosted their Attack by 20%!";
    }

    @Override
    public String getName()
    {
        return "Strengthen";
    }

    @Override
    public String getDescription()
    {
        return "Boosts Attack by 20%.";
    }
}
