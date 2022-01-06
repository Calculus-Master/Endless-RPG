package com.calculusmaster.endlessrpg.util;

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Global
{
    public static final ExecutorService CACHED_POOL = Executors.newCachedThreadPool();
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

    public static void delay(Runnable r, int time, TimeUnit units)
    {
        Executors.newSingleThreadScheduledExecutor().schedule(r, time, units);
    }

    public static int randomValue(int min, int max)
    {
        return RANDOM.nextInt(max - min + 1) + min;
    }

    public static String formatNumber(int number)
    {
        return number > 0 ? "+" + number : "" + number;
    }

    public static void optimizeLootDatabase()
    {
        List<String> keep = new ArrayList<>();
        Mongo.PlayerData.find().forEach(d -> keep.addAll(d.getList("loot", String.class)));
        Mongo.CharacterData.find().forEach(d -> keep.addAll(d.getList("loot", String.class)));

        List<String> delete = new ArrayList<>();
        Mongo.LootData.find().forEach(d -> {
            if(!keep.contains(d.getString("lootID"))) delete.add(d.getString("lootID"));
        });

        Mongo.LootData.deleteMany(Filters.in("lootID", delete));
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
