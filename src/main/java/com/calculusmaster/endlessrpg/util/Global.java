package com.calculusmaster.endlessrpg.util;

import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.mongodb.BasicDBObject;

import java.util.LinkedHashMap;

public class Global
{
    public static <E extends Enum<E>> E castEnum(String input, E[] values)
    {
        for(E e : values) if(input.trim().toUpperCase().equals(e.toString())) return e;
        return null;
    }

    public static BasicDBObject coreStatsDB(LinkedHashMap<Stat, Integer> stats)
    {
        BasicDBObject data = new BasicDBObject();
        for(Stat s : Stat.values()) data.put(s.toString(), stats.get(s));
        return data;
    }

    public static String normalize(String input)
    {
        StringBuilder out = new StringBuilder();
        for(String s : input.split("\\s+")) out.append(pascal(s)).append(" ");
        return out.toString().trim();
	}

	private static String pascal(String s)
    {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
