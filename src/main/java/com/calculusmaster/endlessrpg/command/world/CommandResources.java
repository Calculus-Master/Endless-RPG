package com.calculusmaster.endlessrpg.command.world;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
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

        this.embed.setTitle(active.getName() + "'s Resources")
                .addField("Raw Resources", active.getResources().raw().getFullOverview(), false)
                .addField("Refined Resources", active.getResources().refined().getFullOverview(), false)
                .setDescription("These are " + active.getName() + "'s resources! They are susceptible to being lost if " + active.getName() + " is defeated. Store them in your Bank at any Town for safekeeping!");

        return this;
    }
}
