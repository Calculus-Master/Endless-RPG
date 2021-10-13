package com.calculusmaster.endlessrpg.command.loot;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandInventory extends Command
{
    public CommandInventory(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        StringBuilder list = new StringBuilder();

        for(int i = 0; i < this.playerData.getLoot().size(); i++)
        {
            LootItem loot = LootItem.build(this.playerData.getLoot().get(i));

            list.append(i + 1).append(": ")
                    .append(loot.getName())
                    .append(" | Type: ").append(Global.normalize(loot.getLootType().toString()))
                    .append(" | Boosts: ").append(loot.getBoostsOverview())
                    .append(" | Min. Level: ").append(loot.getRequiredLevel())
                    .append("\n");
        }

        this.embed
                .setTitle(this.player.getName() + "'s Loot")
                .setDescription(list.toString());

        return this;
    }
}
