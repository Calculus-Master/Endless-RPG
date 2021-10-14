package com.calculusmaster.endlessrpg.gameplay.spells;

import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;

public class FortifySpell extends Spell
{
    public FortifySpell()
    {
        super("Fortify");
    }

    @Override
    public String execute(RPGCharacter user, RPGCharacter target, RPGCharacter[] battlers, Battle battle)
    {
        int defenseBoost = (int)(0.2 * user.getStat(Stat.DEFENSE));

        user.addStatChange(Stat.DEFENSE, defenseBoost);

        return user.getName() + " fortified their Defense by 20%!";
    }
}
