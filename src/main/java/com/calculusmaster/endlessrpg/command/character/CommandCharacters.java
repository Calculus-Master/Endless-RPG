package com.calculusmaster.endlessrpg.command.character;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandCharacters extends Command
{
    public CommandCharacters(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        StringBuilder out = new StringBuilder();

        for(int i = 0; i < this.playerData.getCharacterList().size(); i++)
            out.append(i + 1).append(": ").append(RPGCharacter.build(this.playerData.getCharacterList().get(i)).getListOverview()).append("\n");

        this.embed.setDescription(out.toString())
                .setTitle(this.player.getName() + "'s Characters")
                .setFooter("Selected: " + this.playerData.getSelected());

        return this;
    }
}
