package com.calculusmaster.endlessrpg.command.world;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.gameplay.world.skills.GatherSession;
import com.calculusmaster.endlessrpg.gameplay.world.skills.GatheringSkill;
import com.calculusmaster.endlessrpg.gameplay.world.skills.RawResource;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
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
        boolean gather = this.msg.length == 2 && GatheringSkill.cast(this.msg[1]) != null;

        RPGCharacter active = this.playerData.getActiveCharacter();
        Location current = Realm.CURRENT.getLocation(this.playerData.getLocationID());

        if(status)
        {
            if(!GatherSession.END_TIMES.containsKey(active.getCharacterID())) this.response = active.getName() + " is not currently gathering any resources!";
            else this.response = active.getName() + " will be finished gathering resources in " + this.formatTime(GatherSession.END_TIMES.get(active.getCharacterID()).getDelay(TimeUnit.SECONDS)) + "!";
        }
        else if(gather)
        {
            //TODO: Direct selection of resource to gather?
            if(GatherSession.END_TIMES.containsKey(active.getCharacterID())) this.response = active.getName() + " is currently gathering resources!";
            else if(current.getResources().isEmpty()) this.response = current.getName() + " does not have any resources that can be gathered!";
            else if(!current.getResources().canGather(active)) this.response = active.getName() + " cannot gather any resources at " + current.getName() + "!";
            else if(Arrays.stream(RawResource.values()).filter(r -> current.getResources().get(r) != 0).filter(r -> r.getSkill().equals(GatheringSkill.cast(this.msg[1]))).toList().isEmpty()) this.response = current.getName() + " does not have any Resources of that Skill!";
            else
            {
                GatherSession g = GatherSession.create(this.playerData, current, GatheringSkill.cast(this.msg[1]));

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
