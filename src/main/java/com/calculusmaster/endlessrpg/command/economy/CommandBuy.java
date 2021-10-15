package com.calculusmaster.endlessrpg.command.economy;

import com.calculusmaster.endlessrpg.command.core.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandBuy extends Command
{
    public CommandBuy(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {

        return this;
    }
}
