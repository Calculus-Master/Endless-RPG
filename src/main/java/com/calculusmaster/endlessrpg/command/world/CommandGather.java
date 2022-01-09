package com.calculusmaster.endlessrpg.command.world;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.command.core.CommandInvalid;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.gameplay.resources.enums.RawResource;
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
        boolean gather = this.msg.length >= 2 && RawResource.cast(this.msgMultiWordContent(1)) != null;

        RPGCharacter active = this.playerData.getActiveCharacter();
        Location location = Realm.CURRENT.getLocation(this.playerData.getLocationID());

        if(gather)
        {
            RawResource resource = Objects.requireNonNull(RawResource.cast(this.msgMultiWordContent(1)));

            if(GatherSession.isInSession(this.player.getId())) this.response = "You already have a character currently gathering resources!";
            else if(!location.getResources().has(resource)) this.response = location.getName() + " does not have any `" + resource.getName() + "`!";
            else if(active.getEquipment().getHands().stream().noneMatch(l -> l.getBoost(Stat.getRelevantToolStat(resource.getSkill())) != 0)) this.response = "You must equip a proper tool that can gather " + resource.getName() + "!";
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
