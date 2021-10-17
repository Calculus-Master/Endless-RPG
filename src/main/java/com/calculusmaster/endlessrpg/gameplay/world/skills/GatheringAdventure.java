package com.calculusmaster.endlessrpg.gameplay.world.skills;

import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.character.RPGRawResourceContainer;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;

import java.util.*;
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

        //TODO: Weighted outputs? Dynamic EXP? Pick resource that gets gathered? Temporarily, just gives the full location's output
        RPGRawResourceContainer output = this.location.getResources();
        for(RawResource r : RawResource.values())
        {
            if(output.get(r) != 0)
            {
                this.character.getRawResources().increase(r, output.get(r));
                this.character.addSkillExp(r.getSkill(), new SplittableRandom().nextInt((int)(0.9 * r.getExp()), (int)(1.1 * r.getExp())) * this.character.getSkillLevel(r.getSkill()));
            }
        }

        this.player.DM(this.character.getName() + " finished gathering resources! Here's what was obtained:\n\n" + output.getFullOverview());

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
