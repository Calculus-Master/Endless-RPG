package com.calculusmaster.endlessrpg.command.loot;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.command.core.CommandInvalid;
import com.calculusmaster.endlessrpg.gameplay.enums.LootComponentType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandComponentInfo extends Command
{
    public CommandComponentInfo(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        if(this.msg.length < 2) return this.invalid(CommandInvalid.INVALID);

        LootComponentType component = LootComponentType.cast(this.msgMultiWordContent(1));

        if(component == null) return this.invalid("Invalid Component Type!");
        else
        {
            this.embed.setTitle(component.getName() + " Component Information")
                    .addField("Material Amount", "`" + component.getMaterialAmount() + "`\n*How much of a resource is required to craft this component.*", false)
                    .addField("Gold Cost", "`" + component.getGoldCost() + " Gold`\n*How much gold is required to craft this component.*", false);
        }

        return this;
    }
}
