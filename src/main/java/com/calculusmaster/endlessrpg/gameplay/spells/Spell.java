package com.calculusmaster.endlessrpg.gameplay.spells;

import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;

public abstract class Spell
{
    private String name;

    public Spell(String name)
    {
        this.name = name;
    }

    public abstract String getSpellID();

    public abstract String execute(RPGCharacter user, RPGCharacter target, RPGCharacter[] battlers, Battle battle);

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
