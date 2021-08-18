package com.calculusmaster.endlessrpg.gameplay.loot;

import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.util.Global;
import com.calculusmaster.endlessrpg.util.Mongo;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.*;

public abstract class LootItem
{
    private final String lootID;
    private final LootType lootType;
    private final String name;
    protected final LinkedHashMap<Stat, Integer> stats;

    protected LootItem(LootType lootType)
    {
        this.lootID = this.createLootID();
        this.lootType = lootType;
        this.name = lootType.getRandomName();
        this.stats = new LinkedHashMap<>();
    }
    
    protected void addStatBoosts(Stat.Pair... boosts)
    {
        for(Stat.Pair pair : boosts) this.stats.put(pair.stat, pair.value);
    }

    public void toDB()
    {
        Document data = new Document()
                .append("lootID", this.lootID)
                .append("name", this.name)
                .append("type", this.lootType)
                .append("stats", Global.coreStatsDB(this.stats));

        Mongo.LootData.insertOne(data);
    }

    public static void remove(String ID)
    {
        Mongo.LootData.deleteOne(Filters.eq("lootID", ID));
    }

    public LinkedHashMap<Stat, Integer> getBoosts()
    {
        return this.stats;
    }

    public int getBoost(Stat s)
    {
        return this.stats.getOrDefault(s, 0);
    }

    public LootType getLootType()
    {
        return this.lootType;
    }

    public String getName()
    {
        return this.name;
    }

    private String createLootID()
    {
        final StringBuilder s = new StringBuilder();
        final String pool = "abcdefghiklmnopqrstuvwxyz0123456789";
        final Random r = new Random();
        for(int i = 0; i < 32; i++) s.append(pool.charAt(r.nextInt(pool.length())));
        return s.toString();
    }
}
