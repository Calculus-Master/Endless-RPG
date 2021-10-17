package com.calculusmaster.endlessrpg.command.character;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.enums.RPGClass;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandClassInfo extends Command
{
    public CommandClassInfo(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        RPGClass clazz = this.msg.length == 1 ? this.playerData.getActiveCharacter().getRPGClass() : RPGClass.cast(this.msgMultiWordContent(1));

        if(clazz == null) this.response = "Invalid Class name!";
        else
        {
            this.embed
                    .setTitle(clazz.getName())
                    .setDescription("*" + clazz.getDescription() + "*")
                    .addField("Modifiers", clazz.getModifierOverview(), false)
                    .addField("Elemental Damage Modifiers", clazz.getElementalDamageModifierOverview(), false)
                    .addField("Elemental Defense Modifiers", clazz.getElementalDefenseModifierOverview(), false);
        }

        return this;
    }
}
