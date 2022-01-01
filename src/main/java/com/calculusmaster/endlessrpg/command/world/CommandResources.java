package com.calculusmaster.endlessrpg.command.world;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.resources.container.RawResourceContainer;
import com.calculusmaster.endlessrpg.gameplay.world.skills.GatheringSkill;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandResources extends Command
{
    public CommandResources(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        RPGCharacter active = this.playerData.getActiveCharacter();

        RawResourceContainer resources = active.getRawResources();

        if(resources.isEmpty()) this.embed.setDescription("**" + active.getName() + " has no resources!**");
        else for(GatheringSkill s : GatheringSkill.values()) this.embed.addField(Global.normalize(s.toString()) + " Resources", resources.getOverview(s), true);

        this.embed.setTitle(active.getName() + "'s Raw Resources");

        return this;
    }
}
