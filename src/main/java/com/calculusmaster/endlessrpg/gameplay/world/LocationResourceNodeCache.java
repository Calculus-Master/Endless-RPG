package com.calculusmaster.endlessrpg.gameplay.world;

import com.calculusmaster.endlessrpg.gameplay.resources.enums.Resource;
import com.calculusmaster.endlessrpg.util.Mongo;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LocationResourceNodeCache
{
    public static Map<String, LocationResourceNodeCache> RESOURCE_NODES = new HashMap<>();
    private static final ScheduledExecutorService NODE_REGENERATOR = Executors.newScheduledThreadPool(1);

    //Static
    public static void init()
    {
        Mongo.PlayerData.find().forEach(d -> new LocationResourceNodeCache(d.getString("playerID")).register());
    }

    public static void rebuild()
    {
        RESOURCE_NODES.clear();
        LocationResourceNodeCache.init();
    }

    public static ResourceNodeCache getNodeCache(String playerID, Location location)
    {
        return RESOURCE_NODES.get(playerID).getCache().get(location.getID());
    }

    //Cache Class
    private final String playerID;
    private final LinkedHashMap<String, ResourceNodeCache> cache;

    public LocationResourceNodeCache(String playerID)
    {
        this.playerID = playerID;

        this.cache = new LinkedHashMap<>();
        Realm.CURRENT.getLocations().forEach(location -> this.cache.put(location.getID(), new ResourceNodeCache(location)));
    }

    public void register()
    {
        RESOURCE_NODES.put(this.playerID, this);
    }

    public LinkedHashMap<String, ResourceNodeCache> getCache()
    {
        return this.cache;
    }

    //Inner NodeCache Class
    public static class ResourceNodeCache
    {
        private final Location location;
        private final LinkedHashMap<Resource, Integer> nodes;

        public ResourceNodeCache(Location location)
        {
            this.location = location;

            this.nodes = new LinkedHashMap<>();
            Resource.all().stream().filter(r -> this.location.getResources().has(r)).forEach(r -> this.nodes.put(r, this.location.getResources().get(r)));
        }

        private Map<Resource, Integer> safe()
        {
            return Collections.synchronizedMap(this.nodes);
        }

        public boolean isEmpty()
        {
            return this.nodes.isEmpty() || this.safe().keySet().stream().allMatch(r -> this.getAmount(r) <= 0);
        }

        public boolean hasResource(Resource r)
        {
            return this.safe().containsKey(r);
        }

        public int getAmount(Resource r)
        {
            return this.safe().get(r);
        }

        public void decrease(Resource r, int amount)
        {
            this.safe().put(r, this.safe().get(r) - amount);
        }

        private void increase(Resource r, int amount)
        {
            this.safe().put(r, this.safe().get(r) + amount);
        }

        public void regenerate(Resource r, int amount)
        {
            NODE_REGENERATOR.schedule(() -> this.increase(r, amount), 5, TimeUnit.MINUTES);
        }
    }
}
