package com.calculusmaster.endlessrpg.command.loot;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.command.core.CommandInvalid;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.character.RPGResourceContainer;
import com.calculusmaster.endlessrpg.gameplay.enums.LootComponentType;
import com.calculusmaster.endlessrpg.gameplay.loot.LootComponent;
import com.calculusmaster.endlessrpg.gameplay.resources.enums.Resource;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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

        boolean town = Realm.CURRENT.getLocation(this.playerData.getLocationID()).getType().isTown();
        RPGCharacter active = this.playerData.getActiveCharacter();

        RPGResourceContainer source = town ? this.playerData.getResources() : active.getResources();
        int goldSource = town ? this.playerData.getGold() : active.getGold();

        if(!source.has(resource) || source.get(resource) < component.getMaterialAmount()) return this.invalid("You do not have enough " + resource.getName() + " to craft a " + resource.getName() + " " + component.getName() + "!");
        else if(goldSource < component.getGoldCost()) return this.invalid("You do not have enough gold to craft a " + resource.getName() + " " + component.getName() + "!");
        else
        {
            if(town) this.playerData.removeGold(component.getGoldCost());
            else
            {
                active.removeGold(component.getGoldCost());
                active.updateGold();
            }

            if(town) this.playerData.removeResource(resource, component.getMaterialAmount());
            else
            {
                active.getResources().decrease(resource, component.getMaterialAmount());
                active.updateResources();
            }

            LootComponent crafted = LootComponent.create(component, resource);

            crafted.upload();
            this.playerData.addComponent(crafted.getComponentID());

            this.response = "You successfully crafted a " + crafted.getName() + "!\n\nThis component used " + component.getMaterialAmount() + " " + resource.getName() + ", and cost " + component.getGoldCost() + " Gold.";
        }

        return this;
    }
}
