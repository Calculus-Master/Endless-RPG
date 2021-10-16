package com.calculusmaster.endlessrpg.command.world;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.LinkedHashMap;
import java.util.List;

public class CommandLocation extends Command
{
    public CommandLocation(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        Location location = Realm.CURRENT.getLocation(this.playerData.getLocationID());

        this.embed
                .setTitle(Realm.CURRENT.getName())
                .addField("Current Location", "**" + location.getName() + "**\nType: " + Global.normalize(location.getType().toString().replaceAll("_", " ")), true)
                .addField("Current Time", "**" + Global.normalize(location.getTime().toString()) + "**", true)
                .addField("Current Weather", "**" + Global.normalize(location.getWeather().toString()) + "**", true)
                .addField(this.getVisitedOverview());

        //TODO: Detailed description of Location (with Type), detailed description of weather effects, detailed description of realm effects

        return this;
    }

    private MessageEmbed.Field getVisitedOverview()
    {
        StringBuilder content = new StringBuilder();
        LinkedHashMap<String, List<String>> layout = Realm.CURRENT.getRealmLayout();
        List<String> visited = this.playerData.getVisitedLocations();

        content.append("*This is a list of locations you have visited and the locations you are able to visit beyond those.*\n\n");

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

        return new MessageEmbed.Field("Realm Exploration Progress", content.toString(), false);
    }
}
