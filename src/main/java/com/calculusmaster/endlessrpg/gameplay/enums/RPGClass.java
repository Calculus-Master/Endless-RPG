package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.util.Global;

import java.util.HashMap;
import java.util.Map;

import static com.calculusmaster.endlessrpg.gameplay.enums.Stat.*;

public enum RPGClass
{
    RECRUIT(),
    WARRIOR(Stat.Pair.of(ATTACK, 1)),
    TANK(Stat.Pair.of(DEFENSE, 1));

    private final Map<Stat, Integer> statBoosts;
    RPGClass(Stat.Pair... pairs)
    {
        this.statBoosts = new HashMap<>();
        for(Stat.Pair pair : pairs) this.statBoosts.put(pair.stat, pair.value);
    }

    public Map<Stat, Integer> getBoosts()
    {
        return Map.copyOf(this.statBoosts);
    }

    public static RPGClass cast(String input)
    {
        return Global.castEnum(input, values());
    }
}
