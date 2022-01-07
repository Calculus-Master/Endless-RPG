package com.calculusmaster.endlessrpg.gameplay.world.skills;

import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.resources.enums.Resource;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.gameplay.world.LocationResourceNodeCache;
import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GatherSession
{
    public static final List<GatherSession> ACTIVE_SESSIONS = new ArrayList<>();
    private static final ScheduledExecutorService COMMON_EXECUTOR = Executors.newScheduledThreadPool(2);

    private PlayerDataQuery player;
    private RPGCharacter character;
    private Location location;
    private Resource resource;

    private ScheduledFuture<?> future;

    private int resourceHealth;
    private int toolPower;

    public static GatherSession initiate(PlayerDataQuery player, RPGCharacter active, Location location, Resource resource)
    {
        GatherSession session = new GatherSession();

        session.setPlayer(player, active);
        session.setLocation(location);
        session.setResource(resource);
        session.setup();

        ACTIVE_SESSIONS.add(session);
        return session;
    }

    //Main

    public void start()
    {
        this.future = COMMON_EXECUTOR.scheduleAtFixedRate(this::gather, 1, 1, TimeUnit.MINUTES);

        this.player.DM(this.character.getName() + " has started gathering " + this.resource.getName() + " in " + this.location.getName() + "!");
    }

    private void gather()
    {
        int effectiveToolPower = new SplittableRandom().nextInt((int)(this.toolPower * 0.8), (int)(this.toolPower * 1.2));

        this.resourceHealth -= effectiveToolPower;

        if(this.resourceHealth <= 0) this.complete();
    }

    private void complete()
    {
        this.future.cancel(true);

        LocationResourceNodeCache.ResourceNodeCache cache = LocationResourceNodeCache.getNodeCache(this.player.getID(), this.location);

        if(cache.getAmount(this.resource) <= 0) throw new IllegalStateException("Location has no resources!");

        int yield;

        //Randomized yield of 1-3 resources
        int rand = new SplittableRandom().nextInt(100);
        if(rand < 5) yield = 3;
        else if(rand < 25) yield = 2;
        else yield = 1;

        //Make sure randomized yield doesn't exceed what is available
        yield = Math.min(yield, cache.getAmount(this.resource));

        //Add resource yield to character
        this.character.getResources().increase(this.resource, yield);
        this.character.updateResources();

        //Update Location Cache
        cache.decrease(this.resource, yield);
        cache.regenerate(this.resource, yield);

        //Notify player if they extract all available resources
        if(cache.getAmount(this.resource) == 0) this.player.DM(this.location.getName() + " has no more " + this.resource.getName() + "!");

        this.player.DM(this.character.getName() + " gathered " + yield + " " + this.resource.getName() + "!");
    }

    //Internal

    private void setup()
    {
        //TODO: Hit power calculation based on Gathering Skill level, Tool quality/level, Resource tier
        this.resourceHealth = this.resource.toRaw().getNodeHealth();
        this.toolPower = 100;
    }

    private void setResource(Resource resource)
    {
        this.resource = resource;
    }

    private void setLocation(Location location)
    {
        this.location = location;
    }

    private void setPlayer(PlayerDataQuery player, RPGCharacter active)
    {
        this.player = player;
        this.character = active;
    }

    //Access
    public static boolean isInSession(String ID)
    {
        return GatherSession.instance(ID) != null;
    }

    public static GatherSession instance(String ID)
    {
        return ACTIVE_SESSIONS.stream().filter(gs -> gs.player.getID().equals(ID)).findFirst().orElse(null);
    }

    public static void delete(String ID)
    {
        ACTIVE_SESSIONS.removeIf(gs -> gs.player.getID().equals(ID));
    }
}
