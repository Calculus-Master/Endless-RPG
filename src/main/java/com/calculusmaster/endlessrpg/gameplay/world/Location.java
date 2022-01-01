package com.calculusmaster.endlessrpg.gameplay.world;

import com.calculusmaster.endlessrpg.EndlessRPG;
import com.calculusmaster.endlessrpg.gameplay.battle.enemy.EnemyArchetype;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.LocationType;
import com.calculusmaster.endlessrpg.gameplay.enums.Time;
import com.calculusmaster.endlessrpg.gameplay.enums.Weather;
import com.calculusmaster.endlessrpg.gameplay.resources.container.RawResourceContainer;
import com.calculusmaster.endlessrpg.util.Mongo;
import com.calculusmaster.endlessrpg.util.helpers.IDHelper;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;

public class Location
{
    private String locationID;
    private String name;
    private LocationType type;
    private Weather weather;
    private EnemyArchetype enemy;
    private int level;
    private RawResourceContainer resources;

    private Location() {}

    public static Location create(LocationType type)
    {
        Location l = new Location();

        l.locationID = IDHelper.create(8);
        l.type = type;
        l.setName();
        l.weather = Weather.getRandom();
        l.enemy = EnemyArchetype.RANDOM;
        l.level = switch(type) { case DUNGEON -> new SplittableRandom().nextInt(1, 4); case TOWN -> 0; default -> new SplittableRandom().nextInt(0, 6) - 3; };
        l.resources = new RawResourceContainer();

        return l;
    }

    public static Location createRealmHub(String name)
    {
        Location l = new Location();

        l.locationID = "HUB-" + IDHelper.create(4);
        l.name = "Hub of " + name;
        l.type = LocationType.HUB;
        l.weather = Weather.CLEAR;
        l.enemy = EnemyArchetype.DEFAULT;
        l.level = 0;
        l.resources = new RawResourceContainer();

        return l;
    }

    public static Location createFinalKingdom(String name)
    {
        Location l = new Location();

        l.locationID = "KING-" + IDHelper.create(3);
        l.name = "Kingdom of " + name;
        l.type = LocationType.FINAL_KINGDOM;
        l.weather = Weather.CLEAR; //TODO: Harshest weather?
        l.enemy = EnemyArchetype.DEFAULT; //TODO: Special enemy type for Kingdoms?
        l.level = 10;
        l.resources = new RawResourceContainer();

        return l;
    }

    public static Location build(String locationID)
    {
        Document data = Objects.requireNonNull(Mongo.LocationData.find(Filters.eq("locationID", locationID)).first());

        Location l = new Location();

        l.locationID = locationID;
        l.name = data.getString("name");
        l.type = LocationType.cast(data.getString("type"));
        l.weather = Weather.cast(data.getString("weather"));
        l.enemy = EnemyArchetype.cast(data.getString("enemy"));
        l.level = data.getInteger("level");
        l.resources = new RawResourceContainer(data.get("resources", Document.class));

        return l;
    }

    public void upload()
    {
        Document data = new Document()
                .append("locationID", this.locationID)
                .append("name", this.name)
                .append("type", this.type.toString())
                .append("weather", this.weather.toString())
                .append("enemy", this.enemy.toString())
                .append("level", this.level)
                .append("resources", this.resources.serialized());

        Mongo.LocationData.insertOne(data);
    }

    public void delete()
    {
        Mongo.LocationData.deleteOne(Filters.eq("locationID", this.locationID));
    }

    public void updateWeather()
    {
        Mongo.LocationData.updateOne(Filters.eq("locationID", this.locationID), Updates.set("weather", this.weather.toString()));
    }

    public Time getTime()
    {
        int h = LocalDateTime.now().getHour();
        return h > 6 && h < 18 ? Time.DAY : Time.NIGHT;
    }

    public String getID()
    {
        return this.locationID;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public LocationType getType()
    {
        return this.type;
    }

    public void setType(LocationType type)
    {
        this.type = type;
    }

    public Weather getWeather()
    {
        return this.weather;
    }

    public void setWeather(Weather weather)
    {
        this.weather = weather;
    }

    public EnemyArchetype getEnemyArchetype()
    {
        return this.enemy;
    }

    public void setEnemyArchetype(EnemyArchetype enemy)
    {
        this.enemy = enemy;
    }

    public int getLevel()
    {
        return this.level;
    }

    public int getEffectiveLevel(RPGCharacter c)
    {
        return c.getLevel() + this.level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public RawResourceContainer getResources()
    {
        return this.resources;
    }

    private void setName()
    {
        //TODO: Location Types, like cities, towns, forests, mines, caves, lakes, etc
        String file = "/names/" + switch(this.getType()) {
            case TOWN -> "town_names.txt";
            case DUNGEON -> "dungeon_names.txt";
            default -> "location_names.txt";
        };

        List<String> pool = new BufferedReader(new InputStreamReader(Objects.requireNonNull(EndlessRPG.class.getResourceAsStream(file)))).lines().toList();
        this.name = pool.get(new SplittableRandom().nextInt(pool.size()));
    }
}
