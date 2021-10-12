package com.calculusmaster.endlessrpg.command;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandCreate extends Command
{
    public CommandCreate(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        boolean registered = PlayerDataQuery.isRegistered(this.player.getId());

        if(this.msg.length == 1) this.response = "You need to include the character name!";
        else if(registered && !this.playerData.getCharacterList().isEmpty()) this.response = "You can have a maximum of 1 Character for now!";
        else
        {
            RPGCharacter c = RPGCharacter.create(this.rawMultiWordContent(1));

            if(!registered)
            {
                PlayerDataQuery.register(this.player.getId(), this.player.getName());
                this.playerData = new PlayerDataQuery(this.player.getId());
            }

            c.upload();
            this.playerData.addCharacter(c.getCharacterID());

            this.response = "`" + c.getName() + "` has joined the world!";
        }
        return this;
    }
}
