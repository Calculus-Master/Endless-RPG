package com.calculusmaster.endlessrpg.gameplay.character;

import com.calculusmaster.endlessrpg.gameplay.enums.EquipmentType;
import com.calculusmaster.endlessrpg.gameplay.enums.RPGClass;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.util.Global;
import com.calculusmaster.endlessrpg.util.Mongo;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Random;

public class RPGCharacter
{
    private String characterID;
    private Bson query;

    private String name;
    private RPGClass classRPG;
    private int level;
    private int experience;
    private final LinkedHashMap<Stat, Integer> stats = new LinkedHashMap<>();
    private RPGEquipment equipment;

    //Use Factory methods!
    private RPGCharacter() {}

    //Factory
    public static RPGCharacter create(String name)
    {
        RPGCharacter c = new RPGCharacter();

        c.setCharacterID();
        c.setName(name);
        c.setRPGClass(RPGClass.RECRUIT);
        c.setLevel(1);
        c.setExp(0);
        c.setCoreStats();
        c.setEquipment();

        return c;
    }

    public static RPGCharacter build(String characterID)
    {
        Document data = Objects.requireNonNull(Mongo.CharacterData.find(Filters.eq("characterID", characterID)).first(), "Could not build Character from ID: " + characterID);

        RPGCharacter c = new RPGCharacter();

        c.setCharacterID(characterID);
        c.setName(data.getString("name"));
        c.setRPGClass(RPGClass.cast(data.getString("class")));
        c.setLevel(data.getInteger("level"));
        c.setExp(data.getInteger("exp"));
        c.setCoreStats(data.get("stats", Document.class));
        c.setEquipment(data.get("equipment", Document.class));

        return c;
    }

    //Updates

    public void upload()
    {
        Document d = new Document()
                .append("characterID", this.characterID)
                .append("name", this.name)
                .append("class", this.classRPG.toString())
                .append("level", this.level)
                .append("exp", this.experience)
                .append("stats", Global.coreStatsDB(this.stats))
                .append("equipment", this.equipment.serialized());

        Mongo.CharacterData.insertOne(d);
    }

    public void delete()
    {
        Mongo.CharacterData.deleteOne(this.query);
    }

    public void completeUpdate()
    {
        this.delete();
        this.upload();
    }

    private void update(Bson... updates)
    {
        Mongo.CharacterData.updateOne(this.query, Arrays.asList(updates));
    }

    public void updateName()
    {
        this.update(Updates.set("name", this.name));
    }

    public void updateRPGClass()
    {
        this.update(Updates.set("class", this.classRPG.toString()));
    }

    public void updateExperience()
    {
        this.update(Updates.set("level", this.level), Updates.set("experience", this.experience));
    }

    public void updateEquipment()
    {
        this.update(Updates.set("equipment", this.equipment.serialized()));
    }

    //Equipment
    public RPGEquipment getEquipment()
    {
        return this.equipment;
    }

    public void setEquipment()
    {
        this.equipment = new RPGEquipment();
    }

    public void setEquipment(Document equipment)
    {
        this.equipment = new RPGEquipment(equipment);
    }

    public void equipLoot(EquipmentType type, String lootID)
    {
        this.equipment.setEquipmentID(type, lootID);
    }

    public LinkedHashMap<Stat, Integer> getEquipmentBoosts()
    {
        LinkedHashMap<Stat, Integer> boosts = new LinkedHashMap<>();

        for(EquipmentType type : EquipmentType.values())
        {
            LootItem loot = this.equipment.getEquipmentLoot(type);
            if(!loot.isEmpty()) for(Stat s : Stat.values()) boosts.put(s, boosts.getOrDefault(s, 0) + loot.getBoost(s));
        }

        return boosts;
    }

    //Stats - Effective
    public int getDamage(RPGCharacter other)
    {
        final Random r = new Random();

        int attack = this.getStat(Stat.ATTACK);
        int defense = other.getStat(Stat.DEFENSE);

        double randomATK = 0.75 + (1.25 - 0.75) * r.nextDouble();
        double randomDEF = 0.75 + (1.25 - 0.75) * r.nextDouble();

        boolean critATK = r.nextInt(20) < 1;
        boolean critDEF = r.nextInt(20) < 1;
        double critVal = 1.8;

        attack = (int)(attack * randomATK * (critATK ? critVal : 1.0));
        defense = (int)(defense * randomDEF * (critDEF ? critVal : 1.0));

        return attack - defense;
    }

    public int getStat(Stat s)
    {
        int core = this.getCoreStat(s);
        int boost = this.classRPG.getBoosts().getOrDefault(s, 0);
        int loot = this.getEquipmentBoosts().get(s);
        int level = this.level;

        boost *= level;

        if(s.equals(Stat.HEALTH))
        {
            return 10 + (core * 10) + (boost * 5) + loot;
        }
        else
        {
            return core + boost + loot;
        }
    }

    //Stats - Core
    public int getCoreStat(Stat s)
    {
        return this.stats.get(s);
    }

    private void setCoreStats(Document data)
    {
        for(Stat s : Stat.values()) this.stats.put(s, data.getInteger(s.toString()));
    }

    private void setCoreStats()
    {
        for(Stat s : Stat.values()) this.stats.put(s, 0);
    }

    //Experience
    public int getExp()
    {
        return this.experience;
    }

    public void setExp(int exp)
    {
        this.experience = exp;
    }

    public void addExp(int amount)
    {
        this.experience += amount;

        while(this.experience >= this.getExpRequired(this.level + 1))
        {
            this.experience -= this.getExpRequired(this.level + 1);
            this.level++;

            Stat s = Stat.values()[new Random().nextInt(Stat.values().length)];
            this.stats.put(s, this.stats.get(s) + 1);
        }
    }

    public int getExpRequired(int targetLevel)
    {
        //base * rate^(targetLevel - 1)
        int base = 175;
        double rate = 1.08;

        double required = base * Math.pow(rate, targetLevel - 1);
        return (int)required;
    }

    //Level
    public int getLevel()
    {
        return this.level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    //Class
    public RPGClass getRPGClass()
    {
        return this.classRPG;
    }

    public void setRPGClass(RPGClass classRPG)
    {
        this.classRPG = classRPG;
    }

    //Name
    public String getName()
    {
        return this.name;
    }

    private void setName(String name)
    {
        this.name = name;
    }

    //Character ID
    public String getCharacterID()
    {
        return this.characterID;
    }

    public void setCharacterID(String characterID)
    {
        this.characterID = characterID;
        this.query = Filters.eq("characterID", characterID);
    }

    private void setCharacterID()
    {
        final StringBuilder s = new StringBuilder();
        final String pool = "abcdefghiklmnopqrstuvwxyz0123456789";
        final Random r = new Random();
        for(int i = 0; i < 32; i++) s.append(pool.charAt(r.nextInt(pool.length())));
        this.setCharacterID(s.toString());
    }

    //toString
    @Override
    public String toString()
    {
        return "Character {characterID: %s, name: %s}: \n".formatted(this.characterID, this.name) +
                "RPG Class – %s\n".formatted(this.classRPG.toString()) +
                "Core Stats – %s\n".formatted(this.stats.toString());
    }
}