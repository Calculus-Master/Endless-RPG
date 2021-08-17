package com.calculusmaster.endlessrpg.command.core;

import com.calculusmaster.endlessrpg.command.core.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandInvalid extends Command
{
    public CommandInvalid(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        this.response = "Invalid Command!";
        return this;
    }
}
