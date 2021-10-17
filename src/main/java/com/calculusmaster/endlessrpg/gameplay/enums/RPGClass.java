package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.util.Global;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.calculusmaster.endlessrpg.gameplay.enums.ElementType.DARK;
import static com.calculusmaster.endlessrpg.gameplay.enums.Stat.*;

public enum RPGClass
{
    RECRUIT("Recruit", "The most basic class.",
            List.of(),
            List.of(),
            List.of()),
    WARRIOR("Warrior", "A new soldier, somewhat adequate in the art of melee combat.",
            List.of(Modifier.of(ATTACK, 1.05f), Modifier.of(STRENGTH, 1.02f)),
            List.of(),
            List.of()),
    TANK("Tank", "A new frontline soldier, learning the ways of a strong Defense.",
            List.of(Modifier.of(DEFENSE, 1.05f)),
            List.of(),
            List.of()),
    KNIGHT("Knight", "A standard melee combatant, with training in both melee combat and Defense.",
            List.of(Modifier.of(ATTACK, 1.05f), Modifier.of(DEFENSE, 1.05f)),
            List.of(),
            List.of()),
    MAGE("Mage", "A new magic user, somewhat adequate in the art of magical combat.",
            List.of(Modifier.of(ATTACK, 1.02f), Modifier.of(INTELLECT, 1.05f)),
            List.of(),
            List.of()),
    WIZARD("Wizard", "A standard magic user, confident in magical combat.",
            List.of(Modifier.of(INTELLECT, 1.1f)),
            List.of(),
            List.of()),
    DARK_KNIGHT("Dark Knight", "A standard melee combatant, swayed to the side of Darkness.",
            List.of(Modifier.of(ATTACK, 1.1f)),
            List.of(ElementalModifier.of(DARK, 1.2f)),
            List.of(ElementalModifier.of(DARK, 1.2f))),
    SCOUT("Scout", "A quick soldier, with not many other talents.",
            List.of(Modifier.of(SPEED, 1.3f), Modifier.of(DEFENSE, 0.8f)),
            List.of(),
            List.of());

    private final String name;
    private final String description;
    private final Map<Stat, Float> modifiers;
    private final Map<ElementType, Float> elementalDamage;
    private final Map<ElementType, Float> elementalDefense;

    RPGClass(String name, String description, List<Modifier> modifiers, List<ElementalModifier> elementalDamage, List<ElementalModifier> elementalDefense)
    {
        this.name = name;
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

    public Map<ElementType, Float> getElementalDamageModifiers()
    {
        return Map.copyOf(this.elementalDamage);
    }

    public Map<ElementType, Float> getElementalDefenseModifiers()
    {
        return Map.copyOf(this.elementalDefense);
    }

    public Map<Stat, Float> getModifiers()
    {
        return Map.copyOf(this.modifiers);
    }

    public String getName()
    {
        return this.name;
    }

    public String getModifierOverview()
    {
        StringBuilder overview = new StringBuilder();

        for(Map.Entry<Stat, Float> e : this.modifiers.entrySet())
        {
            long percent = Math.round((e.getValue() - 1.0f) * 100f + 0.0001f);
            overview.append(Global.normalize(e.getKey().toString())).append(": ").append(percent > 0 ? "+" + percent + "%" : percent + "%").append("\n");
        }

        return overview.isEmpty() ? "None": overview.toString();
    }

    public String getElementalDamageModifierOverview()
    {
        StringBuilder overview = new StringBuilder();

        for(Map.Entry<ElementType, Float> e : this.elementalDamage.entrySet())
        {
            long percent = Math.round((e.getValue() - 1.0f) * 100f + 0.0001f);
            overview.append(e.getKey().getIcon().getAsMention()).append(": ").append(percent > 0 ? "+" + percent + "%" : percent + "%").append("\n");
        }

        return overview.isEmpty() ? "None": overview.toString();
    }

    public String getElementalDefenseModifierOverview()
    {
        StringBuilder overview = new StringBuilder();

        for(Map.Entry<ElementType, Float> e : this.elementalDefense.entrySet())
        {
            long percent = Math.round((e.getValue() - 1.0f) * 100f + 0.0001f);
            overview.append(e.getKey().getIcon().getAsMention()).append(": ").append(percent > 0 ? "+" + percent + "%" : percent + "%").append("\n");
        }

        return overview.isEmpty() ? "None": overview.toString();
    }

    public static RPGClass cast(String input)
    {
        for(RPGClass clazz : values()) if(input.equalsIgnoreCase(clazz.getName()) || input.equalsIgnoreCase(clazz.toString())) return clazz;
        return Global.castEnum(input, values());
    }

    private static class ElementalModifier
    {
        ElementType element;
        float value;

        static ElementalModifier of(ElementType e, float v)
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
        float value;

        static Modifier of(Stat s, float v)
        {
            Modifier m = new Modifier();
            m.stat = s;
            m.value = v;
            return m;
        }
    }
}
