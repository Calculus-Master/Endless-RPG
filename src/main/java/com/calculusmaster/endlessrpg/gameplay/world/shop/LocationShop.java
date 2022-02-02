package com.calculusmaster.endlessrpg.gameplay.world.shop;

import com.calculusmaster.endlessrpg.gameplay.enums.LocationType;
import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.loot.LootBuilder;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.gameplay.resources.enums.Resource;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.util.Mongo;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LocationShop
{
    private static final LinkedHashMap<String, LocationShop> SHOPS = new LinkedHashMap<>();

    public static void init()
    {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(LocationShop::createShops, 0, 4, TimeUnit.HOURS);
    }

    public static void createShops()
    {
        List<String> players = new ArrayList<>();
        Mongo.PlayerData.find().forEach(d -> players.add(d.getString("playerID")));

        Realm.CURRENT.getLocations().forEach(l -> {
            //Remove current shop
            SHOPS.remove(l.getID());

            //Create new shops
            if(l.getType().equals(LocationType.TOWN))
            {
                //Create new shop
                LocationShop shop = LocationShop.create(l);

                //Initialize player caches
                shop.createCaches(players);
            }
        });

        //TODO: Hub shops, Dungeon shops, Kingdom shop (?), general location shops (very small)?
    }

    public static LocationShop getShop(String locationID)
    {
        return SHOPS.get(locationID);
    }

    public static boolean hasShop(Location location)
    {
        return SHOPS.containsKey(location.getID());
    }

    private Location location;

    private Map<String, LocationShopCache> caches;

    private List<ShopEntry<LootItem>> loot;
    private Map<Resource, Integer> resources;
    private Map<Resource, Integer> resourceCosts;

    public static LocationShop create(Location location)
    {
        LocationShop shop = new LocationShop();

        shop.setLocation(location);
        shop.setInventory();

        SHOPS.put(location.getID(), shop);
        return shop;
    }

    public LocationShopCache getCacheView(String playerID)
    {
        return this.caches.getOrDefault(playerID, new LocationShopCache(playerID, this));
    }

    public void createCaches(List<String> playerIDs)
    {
        this.caches = new HashMap<>();
        playerIDs.forEach(p -> this.caches.put(p, new LocationShopCache(p, this)));
    }

    public void setInventory()
    {
        final SplittableRandom r = new SplittableRandom();

        this.loot = new ArrayList<>();

        //Loot
        int lootAmount = r.nextInt(5, 6 + Math.max(0, this.location.getLevel()));
        //TODO: Town level attribute, set by their location in the Realm Map (currently 0) OR maybe a Tier system?

        for(int i = 0; i < lootAmount; i++)
        {
            LootItem loot = LootBuilder.create(LootType.getRandom(), r.nextInt(5, 50));
            int cost = new SplittableRandom().nextInt(100, 250) + loot.getBoosts().values().stream().mapToInt(x -> x * (new SplittableRandom().nextInt(2, 10))).sum();
            cost -= cost % 10;

            this.loot.add(new ShopEntry<>(loot, cost));
        }

        //Resources
        List<Resource> pool = new ArrayList<>(List.copyOf(Resource.all()));
        Collections.shuffle(pool);

        List<Resource> offerings = new ArrayList<>();

        int resourceAmount = r.nextInt(5, 15);
        for(int i = 0; i < resourceAmount; i++) offerings.add(pool.get(i));

        this.resources = new HashMap<>();
        offerings.forEach(res -> this.resources.put(res, r.nextInt(2, 20)));

        //Resources - Costs
        this.resourceCosts = new HashMap<>();
        this.resources.keySet().forEach(res -> {
            int tierPrice = 2 * (int)(Math.pow(res.getTier(), 2)) * 100;

            int price = r.nextInt((int)(tierPrice * 0.65), (int)(tierPrice * 1.2));
            price = r.nextInt((int)(price * 0.85), (int)(price * 1.15));
            price = r.nextInt((int)(price * 0.5), (int)(price * 1.5));

            price -= price % 10;

            this.resourceCosts.put(res, price);
        });
    }

    public int getResourceCost(Resource r)
    {
        return this.resourceCosts.get(r);
    }

    public Map<Resource, Integer> getResources()
    {
        return this.resources;
    }

    public List<ShopEntry<LootItem>> getLoot()
    {
        return this.loot;
    }

    private void setLocation(Location location)
    {
        this.location = location;
    }

    public Location getLocation()
    {
        return this.location;
    }

    public static final class ShopEntry<T>
    {
        public T item;
        public int cost;

        public ShopEntry(T item, int cost)
        {
            this.item = item;
            this.cost = cost;
        }
    }
}
