package com.calculusmaster.endlessrpg.gameplay.enums;

public enum ElementType
{
    FIRE,
    WATER,
    EARTH,
    AIR,
    DARK,
    LIGHT;

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
