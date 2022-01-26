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
        //r!gather status
        boolean status = this.msg.length == 2 && this.msg[1].equals("status");

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
                this.embed = null;

                GatherSession gs = GatherSession.initiate(this.playerData, active, location, resource);
                gs.start();
            }
        }
        else if(status)
        {
            if(!GatherSession.isInSession(this.player.getId())) this.response = "Your character is not currently gathering any resources!";
            else
            {
                GatherSession g = GatherSession.instance(this.player.getId());

                this.embed
                        .setTitle("Gathering Status")
                        .setDescription("""
                                *How do Gathering Sessions work?*
                                - Every set time interval, your character will attempt to gather the Resource Node.
                                - The Resource Node has a fixed starting health.
                                - Each interval, when your character attempts to gather the resource, your character's Tool power will be subtracted from the health.
                                - Tool power is not fixed each interval; there is some variation.
                                - Once the Resource Node's health reaches zero, your character will have finished gathering the resource!
                                - Your character will receive a variable amount of resources from the session.
                                """)
                        .addField("Attempt Time", "Your character will attempt to gather a resource in **" + g.getRemainingTime() + "**!", false)
                        .addField("Resource Node Health", "The Resource Node has a remaining health of **" + g.getResourceNodeHealth() + "**.", false)
                        .addField("Tool Power", "Your Tool has a power of approximately **" + g.getToolPower() + "**.", false);
            }
        }
        else return this.invalid(CommandInvalid.INVALID);

        return this;
    }
}
