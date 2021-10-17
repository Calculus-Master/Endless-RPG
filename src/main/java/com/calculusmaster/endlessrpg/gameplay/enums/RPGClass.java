package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.util.Global;

import java.util.HashMap;
import java.util.Map;

import static com.calculusmaster.endlessrpg.gameplay.enums.Stat.*;

public enum RPGClass
{
    RECRUIT(),
    WARRIOR(Modifier.of(ATTACK, 1.05), Modifier.of(STRENGTH, 1.02)),
    TANK(Modifier.of(DEFENSE, 1.05)),
    KNIGHT(Modifier.of(ATTACK, 1.05), Modifier.of(DEFENSE, 1.05)),
    MAGE(Modifier.of(ATTACK, 1.02), Modifier.of(INTELLECT, 1.05)),
    WIZARD(Modifier.of(INTELLECT, 1.1));

    private final Map<Stat, Double> modifiers;
    RPGClass(Modifier... modifiers)
    {
        this.modifiers = new HashMap<>();
        for(Modifier m : modifiers) this.modifiers.put(m.stat, m.value);
    }

    public Map<Stat, Double> getModifiers()
    {
        return Map.copyOf(this.modifiers);
    }

    public static RPGClass cast(String input)
    {
        return Global.castEnum(input, values());
    }

    private static class Modifier
    {
        Stat stat;
        double value;

        static Modifier of(Stat s, double v)
        {
            Modifier m = new Modifier();
            m.stat = s;
            m.value = v;
            return m;
        }
    }
}
