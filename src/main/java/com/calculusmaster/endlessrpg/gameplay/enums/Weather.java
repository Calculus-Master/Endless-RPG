package com.calculusmaster.endlessrpg.gameplay.enums;

import com.calculusmaster.endlessrpg.util.Global;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public enum Weather
{
    CLEAR(10, "None"),
    OVERCAST(10, "Light Damage is reduced by 10%", "Light Defense is reduced by 10%"),
    FOG(5, "Light Damage is reduced by 20%", "Light Defense is reduced by 15%", "Damage has a 25% chance to be reduced by 15%"),
    RAIN(10, "Water Damage is boosted by 10%", "Water Defense is boosted by 20%", "Light Damage is reduced by 10%", "Light Defense is reduced by 10%"),
    HARSH_SUN(2, "Light Damage is boosted by 20%", "Light Defense is boosted by 20%", "Fire Damage is boosted by 10%", "Fire Defense is boosted by 10%", "Water Damage is reduced by 10%", "Water Defense is reduced by 30%"),
    DROUGHT(2, "Water Damage is reduced by 70%", "Water Defense is reduced by 100%", "Any Farming Resources will be decimated."),
    WINDY(7, "Air Damage is boosted by 25%", "Air Defense is boosted by 25%", "Earth Damage is reduced by 5%", "Earth Defense is reduced by 5%");

    private final String effects;
    private final int weight;

    Weather(int weight, String... effects)
    {
        this.weight = weight;

        StringBuilder e = new StringBuilder();
        for(String s : effects) e.append("– ").append(s).append("\n");

        this.effects = e.deleteCharAt(e.length() - 1).toString();
    }

    public String getEffects()
    {
        return this.effects;
    }

    public static Weather getRandom()
    {
        final List<Weather> pool = new ArrayList<>();
        for(Weather w : values()) for(int i = 0; i < w.weight; i++) pool.add(w);
        return pool.get(new SplittableRandom().nextInt(pool.size()));
    }

    public static Weather cast(String input)
    {
        return Global.castEnum(input, values());
    }
}
