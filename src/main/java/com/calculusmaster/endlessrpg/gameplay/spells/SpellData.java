package com.calculusmaster.endlessrpg.gameplay.spells;

import java.util.function.Supplier;

public enum SpellData
{
    STRIKE("DEFAULT_ATTACK", StrikeSpell::new),
    FORTIFY("FORTIFY_DEFENSE", FortifySpell::new);

    private final String ID;
    private final Supplier<Spell> supplier;

    SpellData(String ID, Supplier<Spell> supplier)
    {
        this.ID = ID;
        this.supplier = supplier;
    }

    public String getID()
    {
        return this.ID;
    }

    public Spell getInstance()
    {
        return this.supplier.get();
    }

    public static Spell fromID(String spellID)
    {
        for(SpellData sd : values()) if(sd.getID().equals(spellID)) return sd.getInstance();
        return null;
    }
}
