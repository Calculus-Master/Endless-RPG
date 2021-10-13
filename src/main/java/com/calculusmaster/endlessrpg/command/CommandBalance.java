package com.calculusmaster.endlessrpg.command;

import com.calculusmaster.endlessrpg.command.core.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandBalance extends Command
{
    public CommandBalance(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        this.embed
                .setTitle(this.player.getName() + "'s Gold")
                .setDescription("You have **" + this.playerData.getGold() + "** Gold!");

        return this;
    }
}
