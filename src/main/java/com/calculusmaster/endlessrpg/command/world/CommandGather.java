package com.calculusmaster.endlessrpg.command.world;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.gameplay.world.skills.GatheringAdventure;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.TimeUnit;

public class CommandGather extends Command
{
    public CommandGather(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        boolean status = this.msg.length == 2 && this.msg[1].equals("status");

        RPGCharacter active = this.playerData.getActiveCharacter();

        if(status)
        {
            if(!GatheringAdventure.END_TIMES.containsKey(active.getCharacterID())) this.response = active.getName() + " is not currently gathering any resources!";
            else this.response = active.getName() + " will be finished gathering resources in " + this.formatTime(GatheringAdventure.END_TIMES.get(active.getCharacterID()).getDelay(TimeUnit.SECONDS));
        }
        else
        {
            //TODO: Direct selection of resource to gather?
            if(GatheringAdventure.END_TIMES.containsKey(active.getCharacterID())) this.response = active.getName() + " is currently gathering resources!";
            else
            {
                Location current = Realm.CURRENT.getLocation(this.playerData.getLocationID());
                GatheringAdventure g = GatheringAdventure.create(this.playerData, current);

                g.start();

                this.response = active.getName() + " has started gathering resources in " + current.getName() + "!";
            }
        }

        return this;
    }

    private String formatTime(long time)
    {
        int seconds = (int)(time % 60);
        int minutes = (int)(time / 60);

        return minutes + "M " + seconds + "S";
    }
}
