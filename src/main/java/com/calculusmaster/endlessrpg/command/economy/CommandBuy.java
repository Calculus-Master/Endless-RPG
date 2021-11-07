package com.calculusmaster.endlessrpg.command.economy;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.enums.LocationType;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.gameplay.world.LocationShop;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandBuy extends Command
{
    public CommandBuy(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        if(!Realm.CURRENT.getLocation(this.playerData.getLocationID()).getType().equals(LocationType.TOWN))
        {
            this.response = "This location does not have any Shops!";
            return this;
        }

        LocationShop shop = LocationShop.getShop(this.playerData.getLocationID());

        boolean loot = this.msg.length == 3 && this.msg[1].equals("loot") && this.isNumeric(2);

        if(loot)
        {
            int index = this.getInt(2) - 1;

            if(index < 0 || index >= shop.getLoot().size()) this.response = "Invalid Shop Entry index!";
            else
            {
                int cost = shop.getLoot().get(index).cost;

                if(this.playerData.getGold() < cost) this.response = "You need " + cost + " Gold to buy this Loot!";
                else
                {
                    LootItem copy = LootItem.copy(shop.getLoot().get(index).item);
                    copy.upload();

                    this.playerData.addLootItem(copy.getLootID());
                    this.playerData.removeGold(cost);

                    this.response = "Bought \"" + copy.getName() + "\" for " + cost + " Gold!";
                }
            }
        }
        else this.response = INVALID;

        return this;
    }
}
