package com.calculusmaster.endlessrpg.command;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.adventure.Adventure;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandAdventure extends Command
{
    public CommandAdventure(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        boolean status = this.msg.length == 2 && this.msg[1].equals("status");
        boolean start = this.msg.length == 2 && this.msg[1].equals("start");

        if(status)
        {
            String ID = this.playerData.getActiveCharacter().getCharacterID();

            if(!Adventure.isCharacterOnAdventure(ID)) this.response = "This character is not on any adventures!";
            else
            {
                this.response = "`" + Adventure.instance(ID).getRemainingTime() + "`";
            }
        }
        else if(start)
        {
            if(Adventure.isInAdventure(this.player.getId())) this.response = "You are already in an adventure! Use `r!adventure status` to check the status of it!";
            else
            {
                Adventure a = Adventure.create(this.playerData, 5);

                a.start();

                this.response = "Adventure started!";
            }
        }
        else this.response = INVALID;

        return this;
    }
}
