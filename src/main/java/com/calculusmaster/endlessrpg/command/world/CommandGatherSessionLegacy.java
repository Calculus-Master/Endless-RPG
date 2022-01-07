package com.calculusmaster.endlessrpg.command.world;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.gameplay.world.skills.GatherSessionLegacy;
import com.calculusmaster.endlessrpg.gameplay.world.skills.GatheringSkill;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.TimeUnit;

public class CommandGatherSessionLegacy extends Command
{
    public CommandGatherSessionLegacy(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        boolean status = this.msg.length == 2 && this.msg[1].equals("status");
        boolean gather = this.msg.length == 2 && GatheringSkill.cast(this.msg[1]) != null;

        RPGCharacter active = this.playerData.getActiveCharacter();
        Location current = Realm.CURRENT.getLocation(this.playerData.getLocationID());

        if(status)
        {
            if(!GatherSessionLegacy.END_TIMES.containsKey(active.getCharacterID())) this.response = active.getName() + " is not currently gathering any resources!";
            else this.response = active.getName() + " will be finished gathering resources in " + this.formatTime(GatherSessionLegacy.END_TIMES.get(active.getCharacterID()).getDelay(TimeUnit.SECONDS)) + "!";
        }
        else if(gather)
        {
            if(GatherSessionLegacy.END_TIMES.containsKey(active.getCharacterID())) this.response = active.getName() + " is currently gathering resources!";
            else if(current.getResources().isEmpty()) this.response = current.getName() + " does not have any resources that can be gathered!";
            else if(!current.getResources().has(GatheringSkill.cast(this.msg[1]))) this.response = current.getName() + " has no " + Global.normalize(this.msg[1]) + " resources!";
            else if(!current.getResources().canGather(active)) this.response = active.getName() + " cannot gather any resources at " + current.getName() + "!";
            else
            {
                GatherSessionLegacy g = GatherSessionLegacy.create(this.playerData, current, GatheringSkill.cast(this.msg[1]));

                g.start();

                this.response = active.getName() + " has started gathering resources in " + current.getName() + "!";
            }
        }
        else this.response = INVALID;

        return this;
    }

    private String formatTime(long time)
    {
        int seconds = (int)(time % 60);
        int minutes = (int)(time / 60);

        return minutes + "M " + seconds + "S";
    }
}
