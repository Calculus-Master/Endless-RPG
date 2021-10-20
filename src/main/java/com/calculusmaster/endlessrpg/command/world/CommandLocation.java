package com.calculusmaster.endlessrpg.command.world;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.gameplay.world.skills.RawResource;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CommandLocation extends Command
{
    public CommandLocation(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        boolean visited = this.msg.length == 2 && (this.msg[1].equals("visited") || this.msg[1].equals("v"));

        if(visited)
        {
            this.response = "***Your Exploration of " + Realm.CURRENT.getName() + ":***\n\n" + this.getFullVisitedOverview();
            return this;
        }

        Location location = Realm.CURRENT.getLocation(this.playerData.getLocationID());

        this.embed
                .setTitle(Realm.CURRENT.getName())
                .addField("Current Location", "**" + location.getName() + "**\nType: " + Global.normalize(location.getType().toString().replaceAll("_", " ")), false)
                .addField("Current Weather", "**" + Global.normalize(location.getWeather().toString()) + "**\nEffects:\n" + location.getWeather().getEffects(), true)
                .addField("Current Time", "**" + Global.normalize(location.getTime().toString()) + "**\nEffects:\n" + location.getTime().getEffects(), true);

        this.embed.addField(this.getResourcesOverview(location));

        if(CommandTravel.TRAVEL_COOLDOWNS.containsKey(this.player.getId()) || CommandTravel.TRAVEL_TIME.containsKey(this.player.getId()))
        {
            StringBuilder lines = new StringBuilder();

            if(CommandTravel.TRAVEL_COOLDOWNS.containsKey(this.player.getId())) lines.append("Exhaustion: `").append(this.formatTime(CommandTravel.TRAVEL_COOLDOWNS.get(this.player.getId()).getDelay(TimeUnit.SECONDS))).append("`!\n");
            if(CommandTravel.TRAVEL_TIME.containsKey(this.player.getId())) lines.append("Currently Traveling! Arrival in `").append(this.formatTime(CommandTravel.TRAVEL_TIME.get(this.player.getId()).getDelay(TimeUnit.SECONDS))).append("`!\n");

            lines.deleteCharAt(lines.length() - 1);
            this.embed.addField("Travel Status", lines.toString(), false);
        }

        this.embed.addField(this.getVisitedOverview());

        //TODO: Detailed description of Location (with Type), detailed description of weather effects, detailed description of realm effects

        return this;
    }

    private MessageEmbed.Field getResourcesOverview(Location location)
    {
        StringBuilder content = new StringBuilder();

        if(location.getResources().isEmpty()) content.append("This location has no available resources!");
        else
        {
            for(RawResource r : RawResource.values()) if(location.getResources().has(r)) content.append("`").append(r.getName()).append("`: ").append(Global.normalize(r.getSkill().toString())).append(" - Tier ").append(r.getTier()).append(" (Requires Skill Level ").append((r.getTier() - 1) * 10).append(")\n");
            content.deleteCharAt(content.length() - 1);
        }

        return new MessageEmbed.Field("Resources", content.toString(), false);
    }

    private MessageEmbed.Field getVisitedOverview()
    {
        StringBuilder content = new StringBuilder();
        LinkedHashMap<String, List<String>> layout = Realm.CURRENT.getRealmLayout();
        List<String> visited = this.playerData.getVisitedLocations();

        content.append("*Note: This only shows your current location, and any locations it directly leads to (due to Discord Field character limits)! To view your whole exploration progress, use the `r!location visited` command!*\n\n");

        Location l = Realm.CURRENT.getLocation(this.playerData.getLocationID());

        content.append("**").append(l.getName()).append("** – ");

        if(layout.get(l.getID()).isEmpty()) content.append("None (Path End)");
        else
        {
            StringBuilder list = new StringBuilder();
            for(String next : layout.get(l.getID())) list.append(visited.contains(next) ? "" : "`").append(Realm.CURRENT.getLocation(next).getName()).append(visited.contains(next) ? "" : "`").append(" | ");
            content.append(list.delete(list.length() - 3, list.length()));
        }

        return new MessageEmbed.Field("Connected Locations", content.toString(), false);
    }

    private String getFullVisitedOverview()
    {
        StringBuilder content = new StringBuilder();
        LinkedHashMap<String, List<String>> layout = Realm.CURRENT.getRealmLayout();
        List<String> visited = this.playerData.getVisitedLocations();

        for(String s : visited)
        {
            Location l = Realm.CURRENT.getLocation(s);
            content.append("**").append(l.getName()).append("** – ");

            if(layout.get(s).isEmpty()) content.append("None (Path End)");
            else
            {
                StringBuilder list = new StringBuilder();
                for(String next : layout.get(s)) list.append(visited.contains(next) ? "" : "`").append(Realm.CURRENT.getLocation(next).getName()).append(visited.contains(next) ? "" : "`").append(" | ");
                content.append(list.delete(list.length() - 3, list.length()));
            }

            content.append("\n");
        }

        return content.toString();
    }

    private String formatTime(long time)
    {
        int seconds = (int)(time % 60);
        int minutes = (int)(time / 60);

        return minutes + "M " + seconds + "S";
    }
}
