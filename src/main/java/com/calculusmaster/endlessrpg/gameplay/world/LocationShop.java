package com.calculusmaster.endlessrpg.gameplay.world;

import com.calculusmaster.endlessrpg.gameplay.enums.LocationType;
import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.loot.LootBuilder;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SplittableRandom;
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
        Realm.CURRENT.getLocations().forEach(l -> {
            if(l.getType().equals(LocationType.TOWN)) LocationShop.create(l);
        });

        //TODO: Hub shops, Dungeon shops, Kingdom shop (?), general location shops (very small)?
    }

    public static LocationShop getShop(String locationID)
    {
        return SHOPS.get(locationID);
    }

    private Location location;

    private List<ShopEntry<LootItem>> loot;

    public static LocationShop create(Location location)
    {
        LocationShop shop = new LocationShop();

        shop.setLocation(location);
        shop.setInventory();

        SHOPS.put(location.getID(), shop);
        return shop;
    }

    public void setInventory()
    {
        final SplittableRandom r = new SplittableRandom();

        this.loot = new ArrayList<>();

        //Loot
        int amount = r.nextInt(5, 6 + Math.max(0, this.location.getLevel()));
        //TODO: Town level attribute, set by their location in the Realm Map (currently 0) OR maybe a Tier system?

        for(int i = 0; i < amount; i++)
        {
            LootItem loot = LootBuilder.create(LootType.getRandom(), r.nextInt(5, 50));
            int cost = new SplittableRandom().nextInt(100, 250) + loot.getBoosts().values().stream().mapToInt(x -> x * (new SplittableRandom().nextInt(2, 10))).sum();
            cost -= cost % 10;

            this.loot.add(new ShopEntry<>(loot, cost));
        }
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
