package com.calculusmaster.endlessrpg.command.activity;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.adventure.Adventure;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.SplittableRandom;

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
            else this.response = Adventure.instance(ID).getRemainingTime();

        }
        else if(start)
        {
            if(Adventure.isInAdventure(this.player.getId())) this.response = "You are already in an adventure! Use `r!adventure status` to check the status of it!";
            else
            {
                int length = new SplittableRandom().nextInt(4, 7);
                int level = Realm.CURRENT.getLocation(this.playerData.getLocationID()).getEffectiveLevel(this.playerData.getActiveCharacter());
                Adventure a = Adventure.create(this.playerData, length, level);

                a.start();

                this.response = "Level %s Adventure started (Length: %s)!".formatted(level, length);
            }
        }
        else this.response = INVALID;

        return this;
    }
}
