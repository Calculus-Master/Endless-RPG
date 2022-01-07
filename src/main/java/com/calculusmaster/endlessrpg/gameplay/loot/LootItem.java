package com.calculusmaster.endlessrpg.gameplay.loot;

import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacterRequirements;
import com.calculusmaster.endlessrpg.gameplay.character.RPGElementalContainer;
import com.calculusmaster.endlessrpg.gameplay.enums.ElementType;
import com.calculusmaster.endlessrpg.gameplay.enums.LootTag;
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
    private List<LootTag> tags;
    private LinkedHashMap<Stat, Integer> boosts;
    private EnumSet<LootTrait> traits;
    private RPGElementalContainer elementalDamage;
    private RPGElementalContainer elementalDefense;
    private RPGCharacterRequirements requirements;

    private LootItem() {}

    public static final LootItem EMPTY = new LootItem();

    static
    {
        EMPTY.setLootID("NONE");
        EMPTY.setLootType(LootType.NONE);
        EMPTY.setName("NONE");
        EMPTY.setTags();
        EMPTY.setBoosts();
        EMPTY.setTraits();
        EMPTY.setElementalDamage();
        EMPTY.setElementalDefense();
        EMPTY.setRequirements();
    }

    public static LootItem build(String lootID)
    {
        if(lootID.equals(EMPTY.getLootID())) return EMPTY;

        Document data = Objects.requireNonNull(Mongo.LootData.find(Filters.eq("lootID", lootID)).first());

        LootItem loot = new LootItem();

        loot.setLootID(lootID);
        loot.setLootType(LootType.cast(data.getString("type")));
        loot.setName(data.getString("name"));
        loot.setTags(data.getList("tags", String.class));
        loot.setBoosts(data.get("boosts", Document.class));
        loot.setTraits(data.getList("traits", String.class));
        loot.setElementalDamage(data.get("elementalDamage", Document.class));
        loot.setElementalDefense(data.get("elementalDefense", Document.class));
        loot.setRequirements(data.get("requirements", Document.class));

        return loot;
    }

    public static LootItem create(LootType type)
    {
        LootItem loot = new LootItem();

        loot.setLootID();
        loot.setLootType(type);
        loot.setName(type.getRandomName());
        loot.setTags();
        loot.setBoosts();
        loot.setTraits();
        loot.setElementalDamage();
        loot.setElementalDefense();
        loot.setRequirements();

        return loot;
    }

    public static LootItem copy(LootItem source)
    {
        LootItem copy = LootItem.create(source.getLootType());

        //Name
        copy.name = source.name;

        //Tags
        copy.tags = source.tags;

        //Boosts
        for(Map.Entry<Stat, Integer> e : source.getBoosts().entrySet()) copy.setBoost(e.getKey(), e.getValue());

        //Traits
        copy.traits = source.traits;

        //Elemental Damage
        copy.elementalDamage = source.elementalDamage;

        //Elemental Defense
        copy.elementalDefense = source.elementalDefense;

        //Requirements
        copy.requirements = source.requirements;

        return copy;
    }

    public void upload()
    {
        Document data = new Document()
                .append("lootID", this.lootID)
                .append("type", this.lootType.toString())
                .append("name", this.name)
                .append("tags", this.tags.stream().map(LootTag::toString).toList())
                .append("boosts", Global.serializedMap(this.boosts, Stat.values()))
                .append("traits", this.traits.stream().map(LootTrait::toString).toList())
                .append("elementalDamage", this.elementalDamage.serialized())
                .append("elementalDefense", this.elementalDefense.serialized())
                .append("requirements", this.requirements.serialized());

        Mongo.LootData.insertOne(data);
    }

    public static void delete(String ID)
    {
        Mongo.LootData.deleteOne(Filters.eq("lootID", ID));
    }

    public void updateTags()
    {
        Mongo.LootData.updateOne(Filters.eq("lootID", this.lootID), Updates.set("tags", this.tags.stream().map(LootTag::toString).toList()));
    }

    public void updateBoosts()
    {
        Mongo.LootData.updateOne(Filters.eq("lootID", this.lootID), Updates.set("boosts", Global.serializedMap(this.boosts, Stat.values())));
    }

    public void updateTraits()
    {
        Mongo.LootData.updateOne(Filters.eq("lootID", this.lootID), Updates.set("traits", this.traits.stream().map(LootTrait::toString).toList()));
    }

    public void updateElementalDamage()
    {
        Mongo.LootData.updateOne(Filters.eq("lootID", this.lootID), Updates.set("elementalDamage", this.elementalDamage.serialized()));
    }

    public void updateElementalDefense()
    {
        Mongo.LootData.updateOne(Filters.eq("lootID", this.lootID), Updates.set("elementalDefense", this.elementalDefense.serialized()));
    }

    //Requirements
    public void setRequirements(Document document)
    {
        this.requirements = RPGCharacterRequirements.read(document);
    }

    public void setRequirements()
    {
        this.requirements = new RPGCharacterRequirements();
    }

    public RPGCharacterRequirements getRequirements()
    {
        return this.requirements;
    }

    //Elemental Defense
    public LootItem addElementalDefense(ElementType element, int value)
    {
        this.elementalDefense.set(element, value);
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
    public LootItem addElementalDamage(ElementType element, int value)
    {
        this.elementalDamage.set(element, value);
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

    //Traits
    public void setTraits()
    {
        this.traits = EnumSet.noneOf(LootTrait.class);
    }

    public void setTraits(List<String> traits)
    {
        this.traits = traits.isEmpty() ? EnumSet.noneOf(LootTrait.class) : EnumSet.copyOf(traits.stream().map(LootTrait::cast).toList());
    }

    public void addTrait(LootTrait trait)
    {
        this.traits.add(trait);
    }

    public void removeTrait(LootTrait trait)
    {
        this.traits.remove(trait);
    }

    public EnumSet<LootTrait> getTraits()
    {
        return this.traits;
    }

    //Boosts
    public LootItem setBoost(Stat stat, int boost)
    {
        this.boosts.put(stat, boost);
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

        if(sb.isEmpty()) sb.append("None");
        else sb.delete(sb.length() - 2, sb.length());

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

    //Tags
    public String getTagOverview()
    {
        StringBuilder out = new StringBuilder();
        for(LootTag tag : this.tags) out.append(tag.getIcon());
        return out.toString();
    }

    public List<LootTag> getTags()
    {
        return this.tags;
    }

    public void setTags()
    {
        this.tags = new ArrayList<>(List.of(LootTag.RANDOMIZED));
    }

    public void setTags(List<String> tags)
    {
        this.tags = tags.stream().map(LootTag::cast).toList();
    }

    public void addTag(LootTag tag, boolean clear)
    {
        if(clear) this.tags.clear();

        this.tags.add(tag);
    }

    public void addTag(LootTag tag)
    {
        this.addTag(tag, false);
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
        for(int i = 0; i < 16; i++) s.append(pool.charAt(r.nextInt(pool.length())));
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
