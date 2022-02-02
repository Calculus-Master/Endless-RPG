package com.calculusmaster.endlessrpg.gameplay.world.shop;

import com.calculusmaster.endlessrpg.gameplay.resources.enums.Resource;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LocationShopCache
{
    private String playerID;
    private LocationShop shop;

    private List<String> purchasedLoot;
    private Map<Resource, Integer> purchasedResources;

    private ScheduledExecutorService thread;

    public LocationShopCache(String playerID, LocationShop shop)
    {
        this.playerID = playerID;
        this.shop = shop;

        this.purchasedLoot = new ArrayList<>();
        this.purchasedResources = new HashMap<>();

        shop.getResources().keySet().forEach(r -> this.purchasedResources.put(r, 0));

        this.thread = Executors.newScheduledThreadPool(1);
    }

    public boolean hasPurchased(String lootID)
    {
        return Collections.synchronizedList(this.purchasedLoot).contains(lootID);
    }

    public int getEffectiveResourceAmount(Resource r)
    {
        return this.shop.getResources().get(r) - Collections.synchronizedMap(this.purchasedResources).get(r);
    }

    public void purchaseLoot(String lootID)
    {
        this.purchasedLoot.add(lootID);

        //TODO: Should shops regenerate stuff? Maybe make this a random chance of occuring
        this.thread.schedule(() -> this.regenerateLoot(lootID), 10, TimeUnit.MINUTES);
    }

    public void purchaseResource(Resource r, int amount)
    {
        this.purchasedResources.put(r, amount);

        //TODO: Should shops regenerate stuff? Maybe make this a random chance of occuring
        this.thread.schedule(() -> this.regenerateResources(r, amount), 10, TimeUnit.MINUTES);
    }

    private void regenerateLoot(String lootID)
    {
        Collections.synchronizedList(this.purchasedLoot).remove(lootID);
    }

    private void regenerateResources(Resource r, int amount)
    {
        Collections.synchronizedMap(this.purchasedResources).put(r, Math.max(Collections.synchronizedMap(this.purchasedResources).get(r) - amount, 0));
    }
}
