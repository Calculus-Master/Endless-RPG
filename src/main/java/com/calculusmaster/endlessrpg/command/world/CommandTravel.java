package com.calculusmaster.endlessrpg.command.world;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class CommandTravel extends Command
{
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

            Location l = Realm.CURRENT.getLocation(target);
            if(l == null) for(Location realmLocation : Realm.CURRENT.getLocations()) if(realmLocation.getName().equalsIgnoreCase(target)) l = realmLocation;
            if(target.equalsIgnoreCase("hub")) l = Realm.CURRENT.getLocations().get(0);

            if(l == null)
            {
                this.response = "Invalid Location name or ID!";
                return this;
            }

            String current = this.playerData.getLocationID();
            List<String> possible = Realm.CURRENT.getRealmLayout().get(current);

            List<String> visited = this.playerData.getVisitedLocations();

            if(possible.contains(l.getID()) || visited.contains(l.getID()))
            {
                this.playerData.setLocation(l.getID());
                if(!visited.contains(l.getID())) this.playerData.addVisitedLocation(l.getID());

                //TODO: Travel time (between locations)
                //TODO: Travel cooldown (so people can't spam through)
                //TODO: Location travel requirements (fight an enemy to gain access, RPGCharacterRequirements field in Location objects)
                this.response = "You successfully traveled from **" + Realm.CURRENT.getLocation(current).getName() + "** to **" + l.getName() + "**!";
            }
            else this.response = "You cannot travel to this Location! You must have visited a nearby Location to be able to travel to `%s`! (If the Location is visible using the Location command, you must travel to the previous node before you are allowed to travel to this one)".formatted(l.getName());
        }

        return this;
    }
}
