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
        else
        {
            if(!registered)
            {
                PlayerDataQuery.register(this.player.getId(), this.player.getName());
                this.playerData = new PlayerDataQuery(this.player.getId());
            }

            String gold = "";
            if(!this.playerData.getCharacterList().isEmpty())
            {
                int cost = (int)Math.pow(100 * this.playerData.getCharacterList().size(), 1.31);
                cost -= cost % 100;

                if(this.playerData.getGold() < cost)
                {
                    this.response = "You don't have enough Gold to create a new Character! You need `" + cost + "` Gold!";
                    return this;
                }
                else
                {
                    this.playerData.removeGold(cost);
                    gold = " (Cost: " + cost + " Gold)";
                }
            }

            RPGCharacter c = RPGCharacter.create(this.rawMultiWordContent(1));

            c.upload();
            this.playerData.addCharacter(c.getCharacterID());

            this.response = "`" + c.getName() + "` has joined the world!" + gold;
        }
        return this;
    }
}
