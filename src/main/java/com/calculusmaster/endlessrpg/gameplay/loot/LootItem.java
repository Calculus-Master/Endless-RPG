package com.calculusmaster.endlessrpg.gameplay.loot;

import com.calculusmaster.endlessrpg.gameplay.character.RPGElementalContainer;
import com.calculusmaster.endlessrpg.gameplay.enums.ElementType;
import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.util.Global;
import com.calculusmaster.endlessrpg.util.Mongo;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class LootItem
{
    private String lootID;
    private LootType lootType;
    private String name;
    private int requiredLevel;
    private LinkedHashMap<Stat, Integer> boosts;
    private RPGElementalContainer elementalDamage;
    private RPGElementalContainer elementalDefense;

    private LootItem() {}

    public static final LootItem EMPTY = new LootItem();

    static
    {
        EMPTY.setLootID("NONE");
        EMPTY.setLootType(LootType.NONE);
        EMPTY.setName("NONE");
        EMPTY.setBoosts();
    }

    public static LootItem build(String lootID)
    {
        if(lootID.equals(EMPTY.getLootID())) return EMPTY;

        Document data = Mongo.LootData.find(Filters.eq("lootID", lootID)).first();

        LootItem loot = new LootItem();

        loot.setLootID(lootID);
        loot.setLootType(LootType.cast(data.getString("type")));
        loot.setName(data.getString("name"));
        loot.setRequiredLevel(data.getInteger("requiredLevel"));
        loot.setBoosts(data.get("boosts", Document.class));
        loot.setElementalDamage(data.get("elementalDamage", Document.class));
        loot.setElementalDefense(data.get("elementalDefense", Document.class));

        return loot;
    }

    public static LootItem create(LootType type)
    {
        LootItem loot = new LootItem();

        loot.setLootID();
        loot.setLootType(type);
        loot.setName(type.getRandomName());
        loot.setRequiredLevel(0);
        loot.setBoosts();
        loot.setElementalDamage();
        loot.setElementalDefense();

        return loot;
    }

    public void upload()
    {
        Document data = new Document()
                .append("lootID", this.lootID)
                .append("type", this.lootType.toString())
                .append("name", this.name)
                .append("requiredLevel", this.requiredLevel)
                .append("boosts", Global.coreStatsDB(this.boosts))
                .append("elementalDamage", this.elementalDamage.serialized())
                .append("elementalDefense", this.elementalDefense.serialized());

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

    public void updateElementalDamage()
    {
        Mongo.LootData.updateOne(Filters.eq("lootID", this.lootID), Updates.set("elementalDamage", this.elementalDamage.serialized()));
    }

    public void updateElementalDefense()
    {
        Mongo.LootData.updateOne(Filters.eq("lootID", this.lootID), Updates.set("elementalDefense", this.elementalDefense.serialized()));
    }

    //Create
    @Deprecated
    public static LootItem createSword(int attack)
    {
        return LootItem.create(LootType.SWORD).withBoosts(Stat.Pair.of(Stat.ATTACK, attack));
    }

    //Elemental Defense
    public LootItem withElementalDefenseModifiers(ElementType.Pair... modifiers)
    {
        for(ElementType.Pair modifier : modifiers) this.elementalDefense.set(modifier.element, modifier.value);
        return this;
    }

    public RPGElementalContainer getElementalDefense()
    {
        return this.elementalDefense;
    }

    public void setElementalDefense()
    {
        this.elementalDefense = new RPGElementalContainer();
    }

    public void setElementalDefense(Document serialized)
    {
        this.elementalDefense = new RPGElementalContainer(serialized);
    }

    //Elemental Damage
    public LootItem withElementalDamageModifiers(ElementType.Pair... modifiers)
    {
        for(ElementType.Pair modifier : modifiers) this.elementalDamage.set(modifier.element, modifier.value);
        return this;
    }

    public RPGElementalContainer getElementalDamage()
    {
        return this.elementalDamage;
    }

    public void setElementalDamage()
    {
        this.elementalDamage = new RPGElementalContainer();
    }

    public void setElementalDamage(Document serialized)
    {
        this.elementalDamage = new RPGElementalContainer(serialized);
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

    public String getBoostsOverview()
    {
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<Stat, Integer> entry : this.getBoosts().entrySet()) if(entry.getValue() != 0) sb.append(Global.normalize(entry.getKey().toString())).append(" (").append(entry.getValue()).append("), ");
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
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

    //Required Level
    private void setRequiredLevel(int requiredLevel)
    {
        this.requiredLevel = requiredLevel;
    }

    public int getRequiredLevel()
    {
	    return this.requiredLevel;
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

    public boolean isEmpty()
    {
        return this.lootID.equals(EMPTY.getLootID());
    }
}
