package com.calculusmaster.endlessrpg.gameplay.world;

import com.calculusmaster.endlessrpg.EndlessRPG;
import com.calculusmaster.endlessrpg.gameplay.enums.LocationType;
import com.calculusmaster.endlessrpg.gameplay.world.skills.RawResource;
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
        int totalCount = new SplittableRandom().nextInt(20, 40);

        int towns = totalCount / 5;
        int dungeons = 3;
        int biomes = totalCount - dungeons - towns;

        this.locations = new ArrayList<>();

        for(int i = 0; i < towns; i++)
        {
            Location l = Location.create(LocationType.TOWN);
            if(this.locations.stream().anyMatch(loc -> loc.getName().equals(l.getName()))) i--;
            else this.locations.add(l);
        }

        for(int i = 0; i < dungeons; i++)
        {
            Location l = Location.create(LocationType.DUNGEON);
            if(this.locations.stream().anyMatch(loc -> loc.getName().equals(l.getName()))) i--;
            else this.locations.add(l);
        }

        for(int i = 0; i < biomes; i++)
        {
            Location l = Location.create(LocationType.getRandomBiome());
            if(this.locations.stream().anyMatch(loc -> loc.getName().equals(l.getName()))) i--;
            else this.locations.add(l);
        }

        Collections.shuffle(this.locations);

        this.locations.add(0, Location.createRealmHub(this.name));
        this.locations.add(Location.createFinalKingdom(this.name));
    }

    private void createRealmMap()
    {
        List<Location> all = new ArrayList<>(List.copyOf(this.locations));

        //Make sure the Realm has one each of T1 resources
        List<RawResource> guaranteed = new ArrayList<>(RawResource.getResources(1));
        while(!guaranteed.isEmpty())
        {
            Location pick = all.get(new SplittableRandom().nextInt(all.size()));
            if(!Arrays.asList(LocationType.HUB, LocationType.FINAL_KINGDOM, LocationType.DUNGEON).contains(pick.getType()))
            {
                pick.getResources().set(guaranteed.get(0), 1);
                guaranteed.remove(0);
            }
        }

        //Dungeons have some High Tier Resources
        int dungeonHighTier = new SplittableRandom().nextInt(1, 4);
        for(Location l : all) if(l.getType().equals(LocationType.DUNGEON)) for(int i = 0; i < dungeonHighTier; i++) l.getResources().set(RawResource.getRandom(new SplittableRandom().nextInt(RawResource.MAX_TIER - 5, RawResource.MAX_TIER)), 1);

        //Create columns

        List<List<Location>> columns = new ArrayList<>();

        //Hub
        columns.add(List.of(all.get(0)));
        all.remove(0);

        //Final Kingdom
        all.remove(all.size() - 1);

        while(!all.isEmpty())
        {
            int nodes = Math.min(new SplittableRandom().nextInt(1, 6), all.size());

            columns.add(List.copyOf(all.subList(0, nodes)));
            all.subList(0, nodes).clear();
        }

        //Final Kingdom
        columns.add(List.of(this.locations.get(this.locations.size() - 1)));

        //Apply resources - Last set of locations guarantee some T10, Realm will have at least one of each T1
        columns.get(columns.size() - 2).forEach(l -> l.getResources().set(RawResource.getRandom(RawResource.MAX_TIER), new SplittableRandom().nextInt(1, 4)));

        for(List<Location> list : columns)
        {
            for(Location l : list)
            {
                if(!Arrays.asList(LocationType.HUB, LocationType.FINAL_KINGDOM, LocationType.DUNGEON).contains(l.getType()))
                {
                    int amount = new SplittableRandom().nextInt(1, 6);
                    for(int i = 0; i < amount; i++) l.getResources().set(RawResource.getRandom(), new SplittableRandom().nextInt(1, 4));
                }
            }
        }

        //Create realm map

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

        for(Location l : columns.get(columns.size() - 1)) this.realmMap.put(l.getID(), new ArrayList<>());
    }

    private void setName()
    {
        List<String> pool = new BufferedReader(new InputStreamReader(Objects.requireNonNull(EndlessRPG.class.getResourceAsStream("/names/realm_names.txt")))).lines().toList();
        this.name = pool.get(new SplittableRandom().nextInt(pool.size()));
    }
}
