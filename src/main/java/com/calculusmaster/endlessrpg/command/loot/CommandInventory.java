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
        int page = 10;
        int start = this.msg.length == 2 && this.isNumeric(1) ? (this.getInt(1) - 1) * page : 0;
        int end = Math.min(start + page, this.playerData.getLoot().size());

        StringBuilder list = new StringBuilder();
        for(int i = start; i < end; i++)
        {
            LootItem loot = LootItem.build(this.playerData.getLoot().get(i));

            list.append(i + 1).append(": ")
                    .append(loot.getName())
                    .append(" | Type: ").append(Global.normalize(loot.getLootType().toString()))
                    .append(" | Boosts: ").append(loot.getBoostsOverview())
                    .append(" | Min. Level: ").append(loot.getRequirements().getLevel())
                    .append("\n");
        }

        this.embed
                .setTitle(this.player.getName() + "'s Loot")
                .setDescription(list.toString())
                .setFooter("Showing %s to %s (Total: %s)".formatted(start + 1, end, this.playerData.getLoot().size()));

        return this;
    }
}
