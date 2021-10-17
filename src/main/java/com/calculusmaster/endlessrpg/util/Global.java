package com.calculusmaster.endlessrpg.util;

import com.mongodb.BasicDBObject;

import java.util.LinkedHashMap;
import java.util.Random;

public class Global
{
    public static final Random RANDOM = new Random();

    public static <E extends Enum<E>> E castEnum(String input, E[] values)
    {
        for(E e : values) if(input.trim().toUpperCase().equals(e.toString())) return e;
        return null;
    }

    public static <K extends Enum<K>> BasicDBObject serializedMap(LinkedHashMap<K, Integer> map, K[] values)
    {
        BasicDBObject data = new BasicDBObject();
        for(K k : values) data.put(k.toString(), map.getOrDefault(k, 0));
        return data;
    }

    public static int randomValue(int min, int max)
    {
        return RANDOM.nextInt(max - min + 1) + min;
    }

    public static String formatNumber(int number)
    {
        return number > 0 ? "+" + number : "" + number;
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
