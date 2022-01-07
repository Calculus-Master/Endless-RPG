package com.calculusmaster.endlessrpg.command.world;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.command.core.CommandInvalid;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.resources.enums.Resource;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.gameplay.world.skills.GatherSession;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Objects;

public class CommandGather extends Command
{
    public CommandGather(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        //r!gather <resource>
        boolean gather = this.msg.length >= 2 && Resource.cast(this.msgMultiWordContent(1)) != null;

        RPGCharacter active = this.playerData.getActiveCharacter();
        Location location = Realm.CURRENT.getLocation(this.playerData.getLocationID());

        if(gather)
        {
            Resource resource = Objects.requireNonNull(Resource.cast(this.msgMultiWordContent(1)));

            //TODO: Valid Tool (Pick for Mining, Axe for Chopping, Rod for Fishing, ETC)

            if(GatherSession.isInSession(this.player.getId())) this.response = "You already have a character currently gathering resources!";
            else if(!location.getResources().has(resource)) this.response = location.getName() + " does not have any `" + resource.getName() + "`!";
            else
            {
                GatherSession gs = GatherSession.initiate(this.playerData, active, location, resource);
                gs.start();
            }
        }
        else return this.invalid(CommandInvalid.INVALID);

        return this;
    }
}
