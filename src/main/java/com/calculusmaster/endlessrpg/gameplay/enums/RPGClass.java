package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.util.Global;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.calculusmaster.endlessrpg.gameplay.enums.ElementType.DARK;
import static com.calculusmaster.endlessrpg.gameplay.enums.Stat.*;

public enum RPGClass
{
    RECRUIT("The most basic class.",
            List.of(),
            List.of(),
            List.of()),
    WARRIOR("A new soldier, somewhat adequate in the art of melee combat.",
            List.of(Modifier.of(ATTACK, 1.05), Modifier.of(STRENGTH, 1.02)),
            List.of(),
            List.of()),
    TANK("A new frontline soldier, learning the ways of a strong Defense.",
            List.of(Modifier.of(DEFENSE, 1.05)),
            List.of(),
            List.of()),
    KNIGHT("A standard melee combatant, with training in both melee combat and Defense.",
            List.of(Modifier.of(ATTACK, 1.05), Modifier.of(DEFENSE, 1.05)),
            List.of(),
            List.of()),
    MAGE("A new magic user, somewhat adequate in the art of magical combat.",
            List.of(Modifier.of(ATTACK, 1.02), Modifier.of(INTELLECT, 1.05)),
            List.of(),
            List.of()),
    WIZARD("A standard magic user, confident in magical combat.",
            List.of(Modifier.of(INTELLECT, 1.1)),
            List.of(),
            List.of()),
    DARK_KNIGHT("A standard melee combatant, swayed to the side of Darkness",
            List.of(Modifier.of(ATTACK, 1.1)),
            List.of(ElementalModifier.of(DARK, 1.2)),
            List.of(ElementalModifier.of(DARK, 1.2)));

    private final String description;
    private final Map<Stat, Double> modifiers;
    private final Map<ElementType, Double> elementalDamage;
    private final Map<ElementType, Double> elementalDefense;

    RPGClass(String description, List<Modifier> modifiers, List<ElementalModifier> elementalDamage, List<ElementalModifier> elementalDefense)
    {
        this.description = description;

        this.modifiers = new HashMap<>();
        for(Modifier m : modifiers) this.modifiers.put(m.stat, m.value);

        this.elementalDamage = new HashMap<>();
        for(ElementalModifier m : elementalDamage) this.elementalDamage.put(m.element, m.value);

        this.elementalDefense = new HashMap<>();
        for(ElementalModifier m : elementalDefense) this.elementalDefense.put(m.element, m.value);
    }

    public String getDescription()
    {
        return this.description;
    }

    public Map<ElementType, Double> getElementalDamageModifiers()
    {
        return Map.copyOf(this.elementalDamage);
    }

    public Map<ElementType, Double> getElementalDefenseModifiers()
    {
        return Map.copyOf(this.elementalDefense);
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

    public String getElementalDamageModifierOverview()
    {
        StringBuilder overview = new StringBuilder();

        for(Map.Entry<ElementType, Double> e : this.elementalDamage.entrySet())
        {
            int percent = (int)((e.getValue() - 1.0) * 100);
            overview.append(e.getKey().getIcon().getAsMention()).append(": ").append(percent > 0 ? "+" + percent + "%" : percent + "%").append("\n");
        }

        return overview.isEmpty() ? "None": overview.toString();
    }

    public String getElementalDefenseModifierOverview()
    {
        StringBuilder overview = new StringBuilder();

        for(Map.Entry<ElementType, Double> e : this.elementalDefense.entrySet())
        {
            int percent = (int)((e.getValue() - 1.0) * 100);
            overview.append(e.getKey().getIcon().getAsMention()).append(": ").append(percent > 0 ? "+" + percent + "%" : percent + "%").append("\n");
        }

        return overview.isEmpty() ? "None": overview.toString();
    }

    public static RPGClass cast(String input)
    {
        return Global.castEnum(input, values());
    }

    private static class ElementalModifier
    {
        ElementType element;
        double value;

        static ElementalModifier of(ElementType e, double v)
        {
            ElementalModifier m = new ElementalModifier();
            m.element = e;
            m.value = v;
            return m;
        }
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
