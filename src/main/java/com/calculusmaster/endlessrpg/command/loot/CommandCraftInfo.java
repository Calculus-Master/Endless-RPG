package com.calculusmaster.endlessrpg.command.loot;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.command.core.CommandInvalid;
import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandCraftInfo extends Command
{
    public CommandCraftInfo(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        if(this.msg.length != 2) return this.invalid(CommandInvalid.INVALID);

        LootType type = LootType.cast(this.msg[1]);

        if(type == null) return this.invalid("Invalid Loot Type!");

        this.embed
                .setTitle("Crafting: " + Global.normalize(type.toString()))
                .addField("Components", type.getCraftingComponents().isEmpty() ? "This Loot Type is uncraftable" : type.getCraftingComponents().getOverview(), false)
                .addField("Note", "*Components can be made from a variety of materials.*\nTo craft a Loot item, craft the required components with any material of your choosing, using `r!craft` and then combine them using `r!forge` to create your Loot Item!\n\nUncraftable Loot Types can only be obtained through other means such as exploration, combat, or adventures!", false);

        return this;
    }
}
