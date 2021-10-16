package com.calculusmaster.endlessrpg.gameplay.world;

import com.calculusmaster.endlessrpg.EndlessRPG;
import com.calculusmaster.endlessrpg.gameplay.enums.LocationType;
import com.calculusmaster.endlessrpg.gameplay.enums.Time;
import com.calculusmaster.endlessrpg.gameplay.enums.Weather;
import com.calculusmaster.endlessrpg.util.Mongo;
import com.calculusmaster.endlessrpg.util.helpers.IDHelper;
import com.mongodb.client.model.Filters;
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

    private Location() {}

    public static Location create(LocationType type)
    {
        Location l = new Location();

        l.locationID = IDHelper.create(6);
        l.setName();
        l.type = type;
        l.weather = Weather.values()[new SplittableRandom().nextInt(Weather.values().length)];

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

        return l;
    }

    public void upload()
    {
        Document data = new Document()
                .append("locationID", this.locationID)
                .append("name", this.name)
                .append("type", this.type.toString())
                .append("weather", this.weather.toString());

        Mongo.LocationData.insertOne(data);
    }

    public void delete()
    {
        Mongo.LocationData.deleteOne(Filters.eq("locationID", this.locationID));
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

    private void setName()
    {
        //TODO: Location Types, like cities, towns, forests, mines, caves, lakes, etc

        List<String> pool = new BufferedReader(new InputStreamReader(Objects.requireNonNull(EndlessRPG.class.getResourceAsStream("/names/location_names.txt")))).lines().toList();
        this.name = pool.get(new SplittableRandom().nextInt(pool.size()));
    }
}
