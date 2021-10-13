package com.calculusmaster.endlessrpg.command.character;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;

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
        {
            RPGCharacter c = RPGCharacter.build(this.playerData.getCharacterList().get(i));

            String name = "\"" + c.getName() + "\"";
            String level = "Level: " + c.getLevel();
            String clazz = "Class: " + Global.normalize(c.getRPGClass().toString());
            String statTotal = "Power: " + Arrays.stream(Stat.values()).mapToInt(c::getStat).sum();

            out.append(i + 1).append(": ").append(name + " | " + level + " | " + clazz + " | " + statTotal).append("\n");
        }

        this.embed.setDescription(out.toString())
                .setTitle(this.player.getName() + "'s Characters")
                .setFooter("Selected: " + this.playerData.getSelected());

        return this;
    }
}
