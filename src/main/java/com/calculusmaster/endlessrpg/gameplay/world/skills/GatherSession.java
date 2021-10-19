package com.calculusmaster.endlessrpg.gameplay.world.skills;

import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.character.RPGRawResourceContainer;
import com.calculusmaster.endlessrpg.gameplay.enums.RPGClass;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GatherSession
{
    public static final List<GatherSession> GATHER_SESSIONS = new ArrayList<>();
    public static final Map<String, ScheduledFuture<?>> END_TIMES = new HashMap<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(5);

    //Core
    private RPGCharacter character;
    private PlayerDataQuery player;
    private Location location;
    private GatheringSkill skill;

    public static GatherSession create(PlayerDataQuery player, Location location, GatheringSkill skill)
    {
        GatherSession g = new GatherSession();

        g.setCharacter(player.getActiveCharacter());
        g.setPlayer(player);
        g.setLocation(location);
        g.setSkill(skill);

        GATHER_SESSIONS.add(g);
        return g;
    }

    public void start()
    {
        ScheduledFuture<?> end = SCHEDULER.schedule(this::complete, 5, TimeUnit.SECONDS);

        END_TIMES.put(this.character.getCharacterID(), end);
    }

    private void complete()
    {
        RPGRawResourceContainer output = this.location.getResources();

        for(RawResource r : RawResource.values())
        {
            if(output.has(r) && r.canGather(this.character) && r.getSkill().equals(this.skill))
            {
                int skill = this.character.getSkillLevel(r.getSkill());
                int required = r.getRequiredSkillLevel();

                int accuracy;

                if(skill / 10 == required / 10)
                {
                    accuracy = switch(skill % 10) {
                        case 0 -> 72;
                        case 1, 2, 3 -> 75;
                        case 4, 5, 6 -> 80;
                        case 7, 8, 9 -> 90;
                        default -> 70;
                    };
                }
                else accuracy = Math.min(100, 60 + skill - required);

                int maxYield = output.get(r);
                int actualYield = 0;

                for(int i = 0; i < maxYield; i++) if(new SplittableRandom().nextInt(100) < accuracy) actualYield++;

                if(
                        this.skill.equals(GatheringSkill.MINING) && this.character.getRPGClass().equals(RPGClass.ADEPT_MINER)
                        || this.skill.equals(GatheringSkill.FORAGING) && this.character.getRPGClass().equals(RPGClass.ADEPT_FORAGER)
                        || this.skill.equals(GatheringSkill.FISHING) && this.character.getRPGClass().equals(RPGClass.ADEPT_FISHER)
                        || this.skill.equals(GatheringSkill.WOODCUTTING) && this.character.getRPGClass().equals(RPGClass.ADEPT_LUMBERJACK)
                        || this.skill.equals(GatheringSkill.FARMING) && this.character.getRPGClass().equals(RPGClass.ADEPT_FARMER)
                        || this.skill.equals(GatheringSkill.HUNTING) && this.character.getRPGClass().equals(RPGClass.ADEPT_HUNTER))
                    actualYield *= 1.3;

                this.character.getRawResources().increase(r, actualYield);
                this.character.addSkillExp(r.getSkill(), new SplittableRandom().nextInt((int)(0.9 * r.getExp()), (int)(1.1 * r.getExp())) * this.character.getSkillLevel(r.getSkill()));
            }
        }

        this.character.completeUpdate();

        this.player.DM(this.character.getName() + " finished gathering resources! Here's what was obtained:\n\n" + output.getFullOverview());

        GATHER_SESSIONS.remove(this);

        END_TIMES.get(this.character.getCharacterID()).cancel(false);
        END_TIMES.remove(this.character.getCharacterID());
    }

    private void setLocation(Location location)
    {
        this.location = location;
    }

    private void setSkill(GatheringSkill skill)
    {
        this.skill = skill;
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
