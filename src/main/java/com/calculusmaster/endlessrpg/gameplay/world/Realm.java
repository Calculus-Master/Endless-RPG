package com.calculusmaster.endlessrpg.gameplay.world;

import com.calculusmaster.endlessrpg.EndlessRPG;
import com.calculusmaster.endlessrpg.gameplay.enums.LocationType;
import com.calculusmaster.endlessrpg.util.Mongo;
import com.calculusmaster.endlessrpg.util.helpers.IDHelper;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Realm
{
    public static Realm CURRENT;

    private String realmID;
    private String name;
    private List<Location> locations;

    public static void init()
    {
        if(Mongo.RealmData.find().first() != null) Realm.build(Mongo.RealmData.find().first().getString("realmID")).delete();

        CURRENT = Realm.create();
        CURRENT.upload();

        Mongo.PlayerData.updateMany(Filters.exists("playerID"), Updates.set("location", CURRENT.getLocations().get(0).getID()));

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(Realm::init, 0, 1, TimeUnit.DAYS);
    }

    private Realm() {}

    public static Realm create()
    {
        Realm r = new Realm();

        r.realmID = IDHelper.create(10);
        r.setName();
        r.createLocations();

        return r;
    }

    public static Realm build(String realmID)
    {
        Document data = Objects.requireNonNull(Mongo.RealmData.find(Filters.eq("realmID", realmID)).first());

        Realm r = new Realm();

        r.realmID = realmID;
        r.name = data.getString("name");
        r.locations = data.getList("locations", String.class).stream().map(Location::build).toList();

        return r;
    }

    public void upload()
    {
        this.locations.forEach(Location::upload);

        Document data = new Document()
                .append("realmID", this.realmID)
                .append("name", this.name)
                .append("locations", this.locations.stream().map(Location::getID).toList());

        Mongo.RealmData.insertOne(data);
    }

    public void delete()
    {
        this.locations.forEach(Location::delete);

        Mongo.RealmData.deleteOne(Filters.eq("realmID", this.realmID));
    }

    public String getName()
    {
        return this.name;
    }

    public String getRealmID()
    {
        return this.realmID;
    }

    public Location getLocation(String locationID)
    {
        for(Location l : this.locations) if(l.getID().equals(locationID)) return l;
        return null;
    }

    public List<Location> getLocations()
    {
        return this.locations;
    }

    private void createLocations()
    {
        int totalCount = new SplittableRandom().nextInt(15, 30);

        int towns = totalCount / 5;
        int dungeons = 3;
        int biomes = totalCount - dungeons - towns;

        this.locations = new ArrayList<>();

        for(int i = 0; i < towns; i++) this.locations.add(Location.create(LocationType.TOWN));
        for(int i = 0; i < dungeons; i++) this.locations.add(Location.create(LocationType.DUNGEON));
        for(int i = 0; i < biomes; i++) this.locations.add(Location.create(LocationType.getRandomBiome()));
    }

    private void setName()
    {
        List<String> pool = new BufferedReader(new InputStreamReader(Objects.requireNonNull(EndlessRPG.class.getResourceAsStream("/names/realm_names.txt")))).lines().toList();
        this.name = pool.get(new SplittableRandom().nextInt(pool.size()));
    }
}
