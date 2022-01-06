package com.calculusmaster.endlessrpg.command.loot;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.command.core.CommandInvalid;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.LootComponentType;
import com.calculusmaster.endlessrpg.gameplay.loot.LootComponent;
import com.calculusmaster.endlessrpg.gameplay.resources.enums.Resource;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.Executors;

public class CommandCraft extends Command
{
    public CommandCraft(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        //r!craft <component> <resource>

        if(this.msg.length != 3) return this.invalid(CommandInvalid.INVALID);

        LootComponentType component = LootComponentType.cast(this.msg[1]);
        Resource resource = Resource.cast(this.msg[2]);

        if(component == null) return this.invalid("Invalid Component Type!");
        else if(resource == null) return this.invalid("Invalid Resource Name!");

        RPGCharacter active = this.playerData.getActiveCharacter();

        if(!active.getResources().has(resource) || active.getResources().get(resource) < component.getMaterialAmount()) return this.invalid("You do not have enough " + resource.getName() + " to craft a " + resource.getName() + " " + component.getName() + "!");
        else if(active.getGold() < component.getGoldCost()) return this.invalid("You do not have enough gold to craft a " + resource.getName() + " " + component.getName() + "!");
        else
        {
            active.removeGold(component.getGoldCost());
            active.getResources().decrease(resource, component.getMaterialAmount());

            LootComponent crafted = LootComponent.create(component, resource);
            this.playerData.addComponent(crafted.getComponentID());

            Executors.newSingleThreadScheduledExecutor().execute(() -> {
                active.updateGold();
                active.updateResources();
                crafted.upload();
            });

            this.response = "You successfully crafted a " + crafted.getName() + "!\n\nThis component used " + component.getMaterialAmount() + " " + resource.getName() + ", and cost " + component.getGoldCost() + " Gold.";
        }

        return this;
    }
}
