package com.calculusmaster.endlessrpg.command;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.adventure.Adventure;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Random;

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
                this.response = Adventure.instance(ID).getRemainingTime();
            }
        }
        else if(start)
        {
            if(Adventure.isInAdventure(this.player.getId())) this.response = "You are already in an adventure! Use `r!adventure status` to check the status of it!";
            else
            {
                int activeLevel = this.playerData.getActiveCharacter().getLevel();
                int max = activeLevel + (int)(activeLevel * 0.5);
                int min = Math.max(activeLevel - (int)(activeLevel * 0.25), 0);
                int size = new Random().nextInt(max - min + 1) + min;

                if(size < 4) size = 4;
                Adventure a = Adventure.create(this.playerData, size);

                a.start();

                this.response = "Adventure started (Length %s)!".formatted(size);
            }
        }
        else this.response = INVALID;

        return this;
    }
}
