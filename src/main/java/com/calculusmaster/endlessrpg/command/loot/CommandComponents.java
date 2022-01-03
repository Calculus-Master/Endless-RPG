package com.calculusmaster.endlessrpg.command.loot;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.loot.LootComponent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class CommandComponents extends Command
{
    public CommandComponents(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        List<LootComponent> components = this.playerData.getComponents().stream().map(LootComponent::build).toList();

        StringBuilder desc = new StringBuilder();

        for(int i = 0; i < components.size(); i++)
            desc.append(i + 1).append(": ").append(components.get(i).getName()).append("\n");

        if(desc.isEmpty()) desc.append("You have no loot components. Craft more using `r!craft`!");

        this.embed.setTitle(this.playerData.getUsername() + "'s Loot Component Inventory")
                .setDescription(desc.toString());

        return this;
    }
}
