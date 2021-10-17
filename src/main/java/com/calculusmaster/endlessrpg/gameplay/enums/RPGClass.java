package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.util.Global;

import java.util.HashMap;
import java.util.Map;

import static com.calculusmaster.endlessrpg.gameplay.enums.Stat.*;

public enum RPGClass
{
    RECRUIT("The most basic class."),
    WARRIOR("A new soldier, somewhat adequate in the art of melee combat.", Modifier.of(ATTACK, 1.05), Modifier.of(STRENGTH, 1.02)),
    TANK("A new frontline soldier, learning the ways of a strong Defense.", Modifier.of(DEFENSE, 1.05)),
    KNIGHT("A standard melee combatant, with training in both melee combat and Defense.", Modifier.of(ATTACK, 1.05), Modifier.of(DEFENSE, 1.05)),
    MAGE("A new magic user, somewhat adequate in the art of magical combat.", Modifier.of(ATTACK, 1.02), Modifier.of(INTELLECT, 1.05)),
    WIZARD("A standard magic user, confident in magical combat.", Modifier.of(INTELLECT, 1.1));

    private final String description;
    private final Map<Stat, Double> modifiers;
    RPGClass(String description, Modifier... modifiers)
    {
        this.description = description;
        this.modifiers = new HashMap<>();
        for(Modifier m : modifiers) this.modifiers.put(m.stat, m.value);
    }

    public String getDescription()
    {
        return this.description;
    }

    public Map<Stat, Double> getModifiers()
    {
        return Map.copyOf(this.modifiers);
    }

    public String getModifierOverview()
    {
        StringBuilder overview = new StringBuilder();

        for(Map.Entry<Stat, Double> e : this.modifiers.entrySet())
        {
            int percent = (int)((e.getValue() - 1.0) * 100);
            overview.append(Global.normalize(e.getKey().toString())).append(": ").append(percent > 0 ? "+" + percent + "%" : percent + "%").append("\n");
        }

        return overview.isEmpty() ? "None": overview.toString();
    }

    public static RPGClass cast(String input)
    {
        return Global.castEnum(input.replaceAll("_", " "), values());
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
