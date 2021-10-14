package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.util.Global;

public enum Stat
{
    //Basic Stats
    HEALTH,
    ATTACK,
    DEFENSE,
    SPEED,

    //Weapon Affinity Stats
    STRENGTH,
    INTELLECT; //TODO: Add staves

    public static Stat cast(String input)
    {
        return Global.castEnum(input, values());
    }

    public static class Pair
    {
        public Stat stat;
        public int value;

        public static Pair of(Stat stat, int value)
        {
            Pair pair = new Pair();

            pair.stat = stat;
            pair.value = value;

            return pair;
        }
    }
}