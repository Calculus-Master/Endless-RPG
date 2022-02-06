package com.calculusmaster.endlessrpg.gameplay.spell.spells;

import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.StatusCondition;
import com.calculusmaster.endlessrpg.gameplay.spell.Spell;

import java.util.SplittableRandom;
import java.util.StringJoiner;

public class BurnSpell extends Spell
{
    @Override
    public String execute(RPGCharacter user, RPGCharacter target, RPGCharacter[] battlers, Battle battle)
    {
        StringJoiner result = new StringJoiner(" ");

        int damage = this.calculateDamage(user, target, battle);
        damage *= 0.25;

        target.damage(damage);
        result.add(user.getName() + " attacked " + target.getName() + " and dealt " + damage + " damage!");

        if(new SplittableRandom().nextInt(100) < 50)
        {
            target.addStatusCondition(StatusCondition.BURN);

            result.add(target.getName() + " is now burned!");
        }

        return result.toString();
    }

    @Override
    public String getName()
    {
        return "Burn";
    }

    @Override
    public String getDescription()
    {
        return "Deals some damage, and has a 50% chance to burn the target.";
    }
}
