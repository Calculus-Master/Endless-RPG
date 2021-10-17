package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.util.Global;

public enum Weather
{
    CLEAR("None"),
    OVERCAST("Light Damage is reduced by 10%", "Light Defense is reduced by 10%"),
    RAIN("None"); //TODO: Rain effects

    private final String effects;

    Weather(String... effects)
    {
        StringBuilder e = new StringBuilder();
        for(String s : effects) e.append("– ").append(s).append("\n");

        this.effects = e.deleteCharAt(e.length() - 1).toString();
    }

    public String getEffects()
    {
        return this.effects;
    }

    public static Weather cast(String input)
    {
        return Global.castEnum(input, values());
    }
}
