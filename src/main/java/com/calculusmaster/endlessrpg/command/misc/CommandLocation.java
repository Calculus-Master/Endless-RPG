package com.calculusmaster.endlessrpg.command.misc;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandLocation extends Command
{
    public CommandLocation(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        Realm realm = Realm.CURRENT;
        Location location = Realm.CURRENT.getLocation(this.playerData.getLocationID());

        this.embed
                .setTitle(realm.getName())
                .addField("Location", "**" + location.getName() + "**", true)
                .addField("Time", "**" + Global.normalize(location.getTime() + "**"), true)
                .addField("Weather", "**" + Global.normalize(location.getWeather() + "**"), true);

        //TODO: Detailed description of Location (with Type), detailed description of weather effects, detailed description of realm effects

        return this;
    }
}
