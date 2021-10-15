package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.entities.Emote;

import java.util.SplittableRandom;

import static com.calculusmaster.endlessrpg.EndlessRPG.BOT_JDA;

public enum ElementType
{
    FIRE,
    WATER,
    EARTH,
    AIR,
    LIGHTNING,
    DARK,
    LIGHT;

    public Emote getIcon()
    {
        return BOT_JDA.getEmotesByName("element_" + this.toString().toLowerCase(), true).get(0);
    }

    public static ElementType cast(String input)
    {
        return Global.castEnum(input, values());
    }

    public static ElementType getRandom()
    {
        return values()[new SplittableRandom().nextInt(values().length)];
    }

    public static class Pair
    {
        public ElementType element;
        public int value;

        public static Pair of(ElementType element, int value)
        {
            Pair pair = new Pair();

            pair.element = element;
            pair.value = value;

            return pair;
        }
    }
}
