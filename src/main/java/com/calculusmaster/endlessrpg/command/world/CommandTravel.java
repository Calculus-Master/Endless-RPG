package com.calculusmaster.endlessrpg.command.world;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CommandTravel extends Command
{
    private static final ScheduledExecutorService THREAD_POOL = Executors.newScheduledThreadPool(5);

    public static final Map<String, ScheduledFuture<?>> TRAVEL_COOLDOWNS = new HashMap<>();
    public static final Map<String, ScheduledFuture<?>> TRAVEL_TIME = new HashMap<>();

    public CommandTravel(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        if(this.msg.length == 1) this.response = "Include either the name or ID of the Location you want to travel to!";
        else
        {
            String target = this.msgMultiWordContent(1);

            String current = this.playerData.getLocationID();
            List<String> possible = Realm.CURRENT.getRealmLayout().get(current);

            List<String> visited = this.playerData.getVisitedLocations();

            Location l = Realm.CURRENT.getLocation(target);
            if(l == null) for(Location realmLocation : Realm.CURRENT.getLocations()) if(realmLocation.getName().equalsIgnoreCase(target)) l = realmLocation;
            if(target.equalsIgnoreCase("hub")) l = Realm.CURRENT.getLocations().get(0);
            if(target.equalsIgnoreCase("latest")) l = Realm.CURRENT.getLocation(this.playerData.getVisitedLocations().get(this.playerData.getVisitedLocations().size() - 1));

            if(l == null)
            {
                this.response = "Invalid Location name or ID!";
                return this;
            }

            if(l.getID().equals(current)) this.response = "You are already at that Location!";
            else if(!l.getID().startsWith("HUB") && TRAVEL_COOLDOWNS.containsKey(this.player.getId())) this.response = "Your character is exhausted! You cannot travel for another " + this.formatTime(TRAVEL_COOLDOWNS.get(this.player.getId()).getDelay(TimeUnit.SECONDS)) + " minutes!";
            else if(TRAVEL_TIME.containsKey(this.player.getId())) this.response = "Your character is currently traveling to a location! They will arrive in " + this.formatTime(TRAVEL_TIME.get(this.player.getId()).getDelay(TimeUnit.SECONDS)) + " minutes!";
            else if(possible.contains(l.getID()) || visited.contains(l.getID()))
            {
                this.addTravelTime(l, visited, current);

                this.response = "Your character started traveling to " + l.getName() + "! Traveling will take about " + this.formatTime(TRAVEL_TIME.get(this.player.getId()).getDelay(TimeUnit.SECONDS)) + " minutes!";
                //TODO: Location travel requirements (fight an enemy to gain access, RPGCharacterRequirements field in Location objects)
            }
            else this.response = "You cannot travel to this Location! You must have visited a nearby Location to be able to travel to `%s`! (If the Location is visible using the Location command, you must travel to the previous node before you are allowed to travel to this one)".formatted(l.getName());
        }

        return this;
    }

    private void addTravelTime(Location l, List<String> visited, String current)
    {
        int speed = this.playerData.getActiveCharacter().getStat(Stat.SPEED);
        int time = Math.max(45 - (int)((Math.pow(speed, 1.2) / (1 + 2.6 * speed)) * 45), 5);

        time = new SplittableRandom().nextInt((int)(time * 0.9), (int)(time * 1.1));

        ScheduledFuture<?> travelTime = THREAD_POOL.schedule(() -> this.arrive(l, visited, current), time, TimeUnit.MINUTES);
        TRAVEL_TIME.put(this.player.getId(), travelTime);
    }

    private void arrive(Location l, List<String> visited, String current)
    {
        this.playerData.setLocation(l.getID());
        if(!visited.contains(l.getID())) this.playerData.addVisitedLocation(l.getID());

        this.playerData.DM("You successfully traveled from **" + Realm.CURRENT.getLocation(current).getName() + "** to **" + l.getName() + "**!");

        if(!l.getID().startsWith("HUB")) this.addCooldownTimer();

        TRAVEL_TIME.get(this.player.getId()).cancel(true);
        TRAVEL_TIME.remove(this.player.getId());
    }

    private void addCooldownTimer()
    {
        int stamina = this.playerData.getActiveCharacter().getStat(Stat.STAMINA);
        int time = Math.max(60 - (int)((Math.pow(stamina, 1.2) / (1 + 2.6 * stamina)) * 60), 5);

        time = new SplittableRandom().nextInt((int)(time * 0.9), (int)(time * 1.1));

        ScheduledFuture<?> cooldown = THREAD_POOL.schedule(() -> {
            Collections.synchronizedMap(TRAVEL_COOLDOWNS).get(this.player.getId()).cancel(true);
            Collections.synchronizedMap(TRAVEL_COOLDOWNS).remove(this.player.getId());
        }, time, TimeUnit.MINUTES);

        TRAVEL_COOLDOWNS.put(this.player.getId(), cooldown);
    }

    private String formatTime(long time)
    {
        int seconds = (int)(time % 60);
        int minutes = (int)(time / 60);

        return minutes + "M " + seconds + "S";
    }
}
