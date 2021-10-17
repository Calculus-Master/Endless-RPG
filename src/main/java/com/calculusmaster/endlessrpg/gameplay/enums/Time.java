package com.calculusmaster.endlessrpg.gameplay.enums;

public enum Time
{
    DAY("Light Damage is boosted by 2%", "Light Defense is boosted by 4%"),
    NIGHT("Dark Damage is boosted by 4%", "Dark Defense is boosted by 2%");

    private final String effects;

    Time(String... effects)
    {
        StringBuilder e = new StringBuilder();
        for(String s : effects) e.append("– ").append(s).append("\n");

        this.effects = e.deleteCharAt(e.length() - 1).toString();
    }

    public String getEffects()
    {
        return this.effects;
    }
}
