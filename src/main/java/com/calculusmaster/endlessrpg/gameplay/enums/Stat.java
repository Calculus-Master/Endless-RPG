package com.calculusmaster.endlessrpg.gameplay.enums;

public enum Stat
{
    HEALTH,
    ATTACK,
    DEFENSE,
    SPEED;

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