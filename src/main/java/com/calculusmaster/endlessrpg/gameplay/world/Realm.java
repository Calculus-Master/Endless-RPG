package com.calculusmaster.endlessrpg.gameplay.world;

import com.calculusmaster.endlessrpg.EndlessRPG;
import com.calculusmaster.endlessrpg.gameplay.enums.LocationType;
import com.calculusmaster.endlessrpg.gameplay.enums.Weather;
import com.calculusmaster.endlessrpg.gameplay.resources.enums.RawResource;
import com.calculusmaster.endlessrpg.gameplay.world.skills.GatheringSkill;
import com.calculusmaster.endlessrpg.util.Global;
import com.calculusmaster.endlessrpg.util.Mongo;
import com.calculusmaster.endlessrpg.util.helpers.IDHelper;
import com.calculusmaster.endlessrpg.util.helpers.LoggerHelper;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class Realm
{
    public static Realm CURRENT;
    public static boolean LOCKDOWN;

    private String realmID;
    private String name;
    private List<Location> locations;
    private LinkedHashMap<String, List<String>> realmMap;
    private int time;

    public static void init()
    {
        CURRENT = Realm.build(Objects.requireNonNull(Mongo.RealmData.find().first()).getString("realmID"));
        LOCKDOWN = false;

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(Realm::realmUpdater, 1, 1, TimeUnit.HOURS);
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(Realm::cycleWeather, 2, 2, TimeUnit.HOURS);
    }

    public static void realmUpdater()
    {
        CURRENT.decreaseTime();

        if(CURRENT.getTime() <= 0) Realm.createNewRealm();
    }

    public static void createNewRealm()
    {
        LOCKDOWN = true;

        CURRENT.delete();
        CURRENT = Realm.create();
        CURRENT.upload();

        LocationResourceNodeCache.rebuild();

        LOCKDOWN = false;
    }

    public static void cycleWeather()
    {
        Realm.CURRENT.getLocations().stream()
                .filter(l -> !List.of(LocationType.HUB, LocationType.FINAL_KINGDOM).contains(l.getType()))
                .filter(l -> !l.isUnique())
                .forEach(l -> {
                    Weather target = Weather.getRandom();

                    //Drought can only hit locations that have farming resources
                    if(!l.getResources().has(GatheringSkill.FARMING) && target.equals(Weather.DROUGHT)) target = Weather.HARSH_SUN;

                    //Drought Effect: Remove farming resources
                    if(target.equals(Weather.DROUGHT) && l.getResources().has(GatheringSkill.FARMING))
                        RawResource
                                .getResources(GatheringSkill.FARMING)
                                .stream()
                                .filter(r -> l.getResources().has(r))
                                .forEach(r -> l.getResources().decrease(r, l.getResources().get(r)));

                    //Set and update Weather
                    l.setWeather(target);
                    l.updateWeather();
                });
    }

    private Realm() {}

    public static Realm create()
    {
        Realm r = new Realm();

        r.realmID = IDHelper.create(10);
        r.setName();
        r.createLocations();
        r.createRealmMap();
        r.setTime(24 * 7);

        return r;
    }

    public static Realm build(String realmID)
    {
        Document data = Objects.requireNonNull(Mongo.RealmData.find(Filters.eq("realmID", realmID)).first());

        Realm r = new Realm();

        r.realmID = realmID;
        r.name = data.getString("name");
        r.locations = data.getList("locations", String.class).stream().map(Location::build).toList();
        r.realmMap = r.buildRealmMap(data.get("realm_map", Document.class));
        r.time = data.getInteger("time");

        return r;
    }

    public void upload()
    {
        this.locations.forEach(Location::upload);

        Document data = new Document()
                .append("realmID", this.realmID)
                .append("name", this.name)
                .append("locations", this.locations.stream().map(Location::getID).toList())
                .append("realm_map", this.serializeRealmMap())
                .append("time", this.time);

        Mongo.RealmData.insertOne(data);

        Mongo.PlayerData.updateMany(Filters.exists("playerID"), Updates.set("location", this.locations.get(0).getID()));
        Mongo.PlayerData.updateMany(Filters.exists("playerID"), Updates.push("visited", this.locations.get(0).getID()));

        LoggerHelper.info(this.getClass(), "Realm Database Upload Complete!");
    }

    public void delete()
    {
        //this.locations.forEach(Location::delete);

        Mongo.RealmData.deleteOne(Filters.eq("realmID", this.realmID));

        Mongo.LocationData.deleteMany(Filters.exists("locationID"));

        Mongo.PlayerData.updateMany(Filters.exists("playerID"), Updates.set("visited", new JSONArray()));
    }

    public void decreaseTime()
    {
        this.time--;
        Mongo.RealmData.updateOne(Filters.eq("realmID", this.realmID), Updates.inc("time", -1));
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
        return this.locations.stream().filter(l -> l.getID().equals(locationID)).findFirst().orElse(null);
    }

    public Location getHub()
    {
        return this.locations.get(0);
    }

    public Location getFinalKingdom()
    {
        return this.locations.get(this.locations.size() - 1);
    }

    public List<Location> getLocations()
    {
        return this.locations;
    }

    public LinkedHashMap<String, List<String>> getRealmLayout()
    {
        return this.realmMap;
    }

    private LinkedHashMap<String, List<String>> buildRealmMap(Document document)
    {
        LinkedHashMap<String, List<String>> realmMap = new LinkedHashMap<>();

        document.keySet().forEach(s -> realmMap.put(s, document.getList(s, String.class)));

        return realmMap;
    }

    private Document serializeRealmMap()
    {
        Document serialized = new Document();
        for(Map.Entry<String, List<String>> e : this.realmMap.entrySet()) serialized.append(e.getKey(), e.getValue());
        return serialized;
    }

    private void createLocations()
    {
        int totalCount = new SplittableRandom().nextInt(25, 75);

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

        int unique = UniqueLocations.LOCATIONS.size() < 5 ? UniqueLocations.LOCATIONS.size() : new SplittableRandom().nextInt(1, 5);
        for(int i = 0; i < unique; i++)
        {
            Location l = UniqueLocations.getRandom();
            if(this.locations.stream().anyMatch(loc -> loc.getName().equals(l.getName()))) i--;
            else this.locations.add(l);
        }

        Collections.shuffle(this.locations);

        this.locations.add(0, Location.createRealmHub(this.name));
        this.locations.add(Location.createFinalKingdom(this.name));
    }

    private void createRealmMap()
    {
        final SplittableRandom random = new SplittableRandom();
        List<Location> all = new ArrayList<>(List.copyOf(this.locations));

        //Create columns

        List<List<Location>> columns = new ArrayList<>();

        //Hub
        columns.add(List.of(all.get(0)));
        all.remove(0);

        //Final Kingdom
        all.remove(all.size() - 1);

        while(!all.isEmpty())
        {
            int nodes = Math.min(new SplittableRandom().nextInt(1, 8), all.size());

            columns.add(List.copyOf(all.subList(0, nodes)));
            all.subList(0, nodes).clear();
        }

        //Final Kingdom
        columns.add(List.of(this.locations.get(this.locations.size() - 1)));

        //Resource Fixed Locations
        final Predicate<Location> fixedResourceLocations = l -> List.of(LocationType.HUB, LocationType.FINAL_KINGDOM, LocationType.DUNGEON).contains(l.getType()) || l.isUnique();

        //Apply T1 resources - the first 3 columns will guarantee a T1 resource in each
        List<RawResource> resourcesT1 = RawResource.getResources(1);
        for(List<Location> column : columns.subList(0, 3))
            for(Location l : column)
                if(!fixedResourceLocations.test(l))
                    for(int i = 0; i < random.nextInt(1, 3); i++)
                        l.getResources().set(resourcesT1.get(random.nextInt(resourcesT1.size())), random.nextInt(10, 20));

        //Apply resources – Dungeons have some High Tier resources
        int dungeonHighTier = random.nextInt(1, 4);
        for(Location l : all) if(l.getType().equals(LocationType.DUNGEON)) for(int i = 0; i < dungeonHighTier; i++) l.getResources().set(RawResource.getRandom(random.nextInt(RawResource.MAX_TIER - 5, RawResource.MAX_TIER)), 1);

        //Apply resources - Last set of locations guarantee some T10
        columns.get(columns.size() - 2).forEach(l -> l.getResources().set(RawResource.getRandom(RawResource.MAX_TIER), random.nextInt(1, 4)));

        for(List<Location> list : columns)
        {
            for(Location l : list)
            {
                if(!fixedResourceLocations.test(l))
                {
                    int amount = random.nextInt(1, 6);
                    for(int i = 0; i < amount; i++) l.getResources().set(RawResource.getRandom(), random.nextInt(1, 4));
                }
            }
        }

        //Set Location Levels to increase the further they are from the hub
        int effectiveLength = columns.size() - 1; //Excludes the Final Kingdom
        int thresholdHigh = effectiveLength - (int)(0.2 * effectiveLength); //Furthest % are High
        int thresholdLow = 1 + (int)(effectiveLength * 0.4); //Closest % are Low

        int[] thresholdHighLevels = {2, 4}; //Levels will be between these, inclusive
        int[] thresholdLowLevels = {3, 5}; //Levels will be between these, inclusive (and negated)

        for(int i = 0; i < columns.size(); i++)
        {
            if(i >= thresholdHigh)
            {
                for(Location low : columns.get(i))
                {
                    if(!fixedResourceLocations.test(low) && !low.getType().equals(LocationType.TOWN))
                        low.setLevel(random.nextInt(thresholdHighLevels[0], thresholdHighLevels[1] + 1));
                }
            }
            else if(i <= thresholdLow)
            {
                for(Location low : columns.get(i))
                {
                    if(!fixedResourceLocations.test(low) && !low.getType().equals(LocationType.TOWN))
                        low.setLevel(random.nextInt(thresholdLowLevels[0], thresholdLowLevels[1] + 1) * -1);
                }
            }
            //If the Location is between the thresholds, we use the randomly assigned level in the Location.create() method
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

    private void setTime(int time)
    {
        this.time = time;
    }

    private int getTime()
    {
        return this.time;
    }

    private void setName()
    {
        List<String> pool = new BufferedReader(new InputStreamReader(Objects.requireNonNull(EndlessRPG.class.getResourceAsStream("/names/realm_names.txt")))).lines().toList();
        this.name = Global.normalize(pool.get(new SplittableRandom().nextInt(pool.size())));
    }
}
