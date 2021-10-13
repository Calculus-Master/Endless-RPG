package com.calculusmaster.endlessrpg.command;

import com.calculusmaster.endlessrpg.command.core.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandSelect extends Command
{
    public CommandSelect(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        boolean select = this.msg.length == 2 && this.isNumeric(1);

        if(select)
        {
            int index = this.getInt(1) - 1;

            if(index < 0 || index >= this.playerData.getCharacterList().size()) this.response = "Invalid character number!";
            else
            {
                this.playerData.setSelected(index);

                this.response = "Your active Character is now Character #" + (index + 1) + "!";
            }
        }
        else this.response = INVALID;

        return this;
    }
}
