package com.calculusmaster.endlessrpg.gameplay.skills;

import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GatheringAdventure
{
    public static final List<GatheringAdventure> GATHERING_ADVENTURES = new ArrayList<>();
    public static final Map<String, ScheduledFuture<?>> END_TIMES = new HashMap<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(5);

    //Core
    private RPGCharacter character;
    private PlayerDataQuery player;
    private Location location;

    public static GatheringAdventure create(PlayerDataQuery player, Location location)
    {
        GatheringAdventure g = new GatheringAdventure();

        g.setCharacter(player.getActiveCharacter());
        g.setPlayer(player);
        g.setLocation(location);

        GATHERING_ADVENTURES.add(g);
        return g;
    }

    public void start()
    {
        ScheduledFuture<?> end = SCHEDULER.schedule(this::complete, 1, TimeUnit.HOURS);

        END_TIMES.put(this.character.getCharacterID(), end);
    }

    private void complete()
    {
        END_TIMES.get(this.character.getCharacterID()).cancel(true);
        END_TIMES.remove(this.character.getCharacterID());

        GATHERING_ADVENTURES.remove(this);



        this.character.completeUpdate();
    }

    private void setLocation(Location location)
    {
        this.location = location;
    }

    private void setPlayer(PlayerDataQuery player)
    {
        this.player = player;
    }

    private void setCharacter(RPGCharacter character)
    {
        this.character = character;
    }
}
