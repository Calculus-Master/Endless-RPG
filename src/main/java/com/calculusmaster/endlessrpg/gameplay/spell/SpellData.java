package com.calculusmaster.endlessrpg.gameplay.spell;

import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacterRequirements;
import com.calculusmaster.endlessrpg.gameplay.enums.RPGClass;
import com.calculusmaster.endlessrpg.gameplay.spell.spells.FortifySpell;
import com.calculusmaster.endlessrpg.gameplay.spell.spells.StrikeSpell;

import java.util.function.Supplier;

public enum SpellData
{
    STRIKE("DEFAULT_ATTACK", StrikeSpell::new, new RPGCharacterRequirements()),
    FORTIFY("FORTIFY_DEFENSE", FortifySpell::new, new RPGCharacterRequirements().addClass(RPGClass.TANK));

    private final String ID;
    private final Supplier<Spell> supplier;
    private final RPGCharacterRequirements spellRequirements;

    SpellData(String ID, Supplier<Spell> supplier, RPGCharacterRequirements spellRequirements)
    {
        this.ID = ID;
        this.supplier = supplier;
        this.spellRequirements = spellRequirements;
    }

    public String getID()
    {
        return this.ID;
    }

    public Spell getInstance()
    {
        return this.supplier.get();
    }

    public RPGCharacterRequirements getRequirements()
    {
        return this.spellRequirements;
    }

    public static Spell fromID(String spellID)
    {
        for(SpellData sd : values()) if(sd.getID().equals(spellID)) return sd.getInstance();
        return null;
    }
}
