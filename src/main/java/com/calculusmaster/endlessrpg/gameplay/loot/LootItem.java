package com.calculusmaster.endlessrpg.gameplay.loot;

import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.util.Global;
import com.calculusmaster.endlessrpg.util.Mongo;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.util.*;

public class LootItem
{
    private String lootID;
    private LootType lootType;
    private String name;
    protected LinkedHashMap<Stat, Integer> boosts;

    private LootItem() {}

    public static LootItem build(String lootID)
    {
        Document data = Mongo.LootData.find(Filters.eq("lootID", lootID)).first();

        LootItem loot = new LootItem();

        loot.setLootID(lootID);
        loot.setLootType(LootType.cast(data.getString("type")));
        loot.setName(data.getString("name"));
        loot.setBoosts(data.get("boosts", Document.class));

        return loot;
    }

    public static LootItem create(LootType type)
    {
        LootItem loot = new LootItem();

        loot.setLootID();
        loot.setLootType(type);
        loot.setName(type.getRandomName());
        loot.setBoosts();

        return loot;
    }

    public void upload()
    {
        Document data = new Document()
                .append("lootID", this.lootID)
                .append("type", this.lootType)
                .append("name", this.name)
                .append("boosts", Global.coreStatsDB(this.boosts));

        Mongo.LootData.insertOne(data);
    }

    public static void delete(String ID)
    {
        Mongo.LootData.deleteOne(Filters.eq("lootID", ID));
    }

    public void updateBoosts()
    {
        Mongo.LootData.updateOne(Filters.eq("lootID", this.lootID), Updates.set("boosts", Global.coreStatsDB(this.boosts)));
    }

    //Boosts
    public LootItem withBoosts(Stat.Pair... boosts)
    {
        for(Stat.Pair pair : boosts) this.boosts.put(pair.stat, pair.value);
        return this;
    }

    public LinkedHashMap<Stat, Integer> getBoosts()
    {
        return this.boosts;
    }

    public int getBoost(Stat s)
    {
        return this.boosts.getOrDefault(s, 0);
    }

    private void setBoosts()
    {
        this.boosts = new LinkedHashMap<>();
    }

    private void setBoosts(Document serial)
    {
        this.setBoosts();

        for(String s : serial.keySet()) this.boosts.put(Stat.cast(s), serial.getInteger(s));
    }

    //Type
    private void setLootType(LootType type)
    {
        this.lootType = type;
    }

    public LootType getLootType()
    {
        return this.lootType;
    }

    //Name
    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
	}

	//ID
    private void setLootID()
    {
        final StringBuilder s = new StringBuilder();
        final String pool = "abcdefghiklmnopqrstuvwxyz0123456789";
        final Random r = new Random();
        for(int i = 0; i < 32; i++) s.append(pool.charAt(r.nextInt(pool.length())));
        this.lootID = s.toString();
    }

    private void setLootID(String lootID)
    {
        this.lootID = lootID;
    }

    public String getLootID()
    {
        return this.lootID;
    }
}
