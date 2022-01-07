package com.calculusmaster.endlessrpg.command.loot;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class CommandInventory extends Command
{
    public CommandInventory(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        RPGCharacter active = this.playerData.getActiveCharacter();

        int page = 10;
        int start = this.msg.length == 2 && this.isNumeric(1) ? (this.getInt(1) - 1) * page : 0;
        int end = Math.min(start + page, active.getLoot().size());

        String loot = CommandInventory.createLootListInfo(active.getLoot(), start, end);

        this.embed
                .setTitle(this.player.getName() + "'s Loot")
                .setDescription(loot)
                .setFooter("Showing %s to %s (Total: %s)".formatted(start + 1, end, active.getLoot().size()));

        return this;
    }

    public static String createLootListInfo(List<String> IDs, int start, int end)
    {
        StringBuilder list = new StringBuilder();

        for(int i = start; i < end; i++)
        {
            LootItem loot = LootItem.build(IDs.get(i));

            list
                    .append("**").append(i + 1).append(":** ")
                    .append("*").append(loot.getName()).append("*").append(loot.getTagOverview())
                    .append(" | ").append(Global.normalize(loot.getLootType().toString()))
                    .append(" | Boosts: ").append(loot.getBoostsOverview())
                    .append(" | Traits: ").append(loot.getTraits().size())
                    //.append(" | ").append(loot.getRequirements().check(active) ? ":white_check_mark:" : ":x:")
                    .append("\n");
        }

        return list.toString();
    }
}
