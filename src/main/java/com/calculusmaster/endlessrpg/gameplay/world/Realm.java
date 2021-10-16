package com.calculusmaster.endlessrpg.gameplay.world;

import com.calculusmaster.endlessrpg.EndlessRPG;
import com.calculusmaster.endlessrpg.gameplay.enums.LocationType;
import com.calculusmaster.endlessrpg.util.Mongo;
import com.calculusmaster.endlessrpg.util.helpers.IDHelper;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Realm
{
    public static Realm CURRENT;

    private String realmID;
    private String name;
    private List<Location> locations;
    private LinkedHashMap<String, List<String>> realmMap;

    public static void init()
    {
        if(Mongo.RealmData.find().first() != null)
        {
            Realm.build(Mongo.RealmData.find().first().getString("realmID")).delete();
            Mongo.LocationData.deleteMany(Filters.exists("locationID"));
            Mongo.PlayerData.updateMany(Filters.exists("playerID"), Updates.set("visited", new JSONArray()));
        }

        CURRENT = Realm.create();
        CURRENT.upload();

        Mongo.PlayerData.updateMany(Filters.exists("playerID"), Updates.set("location", CURRENT.getLocations().get(0).getID()));
        Mongo.PlayerData.updateMany(Filters.exists("playerID"), Updates.push("visited", CURRENT.getLocations().get(0).getID()));

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(Realm::init, 1, 1, TimeUnit.DAYS);
    }

    private Realm() {}

    public static Realm create()
    {
        Realm r = new Realm();

        r.realmID = IDHelper.create(10);
        r.setName();
        r.createLocations();
        r.createRealmMap();

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

    public LinkedHashMap<String, List<String>> getRealmLayout()
    {
        return this.realmMap;
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

        Collections.shuffle(this.locations);

        this.locations.add(0, Location.createRealmHub(this.name));
    }

    private void createRealmMap()
    {
        List<Location> all = new ArrayList<>(List.copyOf(this.locations));

        List<List<Location>> columns = new ArrayList<>();

        while(!all.isEmpty())
        {
            int nodes = Math.min(new SplittableRandom().nextInt(1, 4), all.size());

            columns.add(List.copyOf(all.subList(0, nodes)));
            all.subList(0, nodes).clear();
        }

        this.realmMap = new LinkedHashMap<>();

        for(int i = 0; i < columns.size() - 1; i++)
        {
            List<Location> current = columns.get(i);
            List<Location> next = columns.get(i + 1);

            List<Integer> nums = new ArrayList<>();
            for(int k = 0; k < next.size(); k++) nums.add(k);

            while(!nums.isEmpty())
            {
                int rCurrent = new SplittableRandom().nextInt(current.size());
                int numsIndex = new SplittableRandom().nextInt(nums.size());

                String key = current.get(rCurrent).getID();
                String addition = next.get(nums.get(numsIndex)).getID();

                if(!this.realmMap.containsKey(key)) this.realmMap.put(key, new ArrayList<>());
                this.realmMap.get(key).add(addition);

                nums.remove(numsIndex);
            }

            for(Location l : current) if(!this.realmMap.containsKey(l.getID())) this.realmMap.put(l.getID(), new ArrayList<>());
        }

        for(Location l : columns.get(columns.size() - 1)) this.realmMap.put(l.getName(), new ArrayList<>());
    }

    private void setName()
    {
        List<String> pool = new BufferedReader(new InputStreamReader(Objects.requireNonNull(EndlessRPG.class.getResourceAsStream("/names/realm_names.txt")))).lines().toList();
        this.name = pool.get(new SplittableRandom().nextInt(pool.size()));
    }
}
