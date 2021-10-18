package com.calculusmaster.endlessrpg.command.character;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.RPGClass;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandClass extends Command
{
    public CommandClass(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        RPGCharacter active = this.playerData.getActiveCharacter();
        RPGClass current = active.getRPGClass();

        this.embed
                .setTitle("Class")
                .addField("Current", current.getName() + "\n*" + current.getDescription() + "*", false)
                .addField(this.getPossibleClassesField(active))
                .setFooter("To view more information about a Class, use the r!classinfo <className> Command!");

        return this;
    }

    private MessageEmbed.Field getPossibleClassesField(RPGCharacter active)
    {
        StringBuilder possible = new StringBuilder();

        for(RPGClass clazz : RPGClass.values())
        {
            if(clazz.getRequirements() != null && clazz.getRequirements().check(active))
                possible.append("**").append(clazz.getName()).append("** – ").append(clazz.getDescription()).append("\n");
        }

        if(possible.isEmpty()) possible.append("None");
        else possible.deleteCharAt(possible.length() - 1);

        return new MessageEmbed.Field("Potential Classes", possible.toString(), false);
    }
}
