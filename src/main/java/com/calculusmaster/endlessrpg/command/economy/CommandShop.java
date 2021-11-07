package com.calculusmaster.endlessrpg.command.economy;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.enums.LocationType;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.gameplay.world.LocationShop;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandShop extends Command
{
    public CommandShop(MessageReceivedEvent event, String msg)
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

        boolean loot = this.msg.length == 2 && this.msg[1].equals("loot");

        if(loot)
        {
            this.embed.setTitle("Shop Loot Items");

            for(int i = 0; i < shop.getLoot().size(); i++)
            {
                LootItem l = shop.getLoot().get(i).item;
                this.embed.addField(
                        (i + 1) + ": " + Global.normalize(l.getLootType().toString()),
                        "*" + l.getName() + "*\nCost: `" + shop.getLoot().get(i).cost + "` Gold\nBoosts: " + l.getBoostsOverview() + "\n*Requirements:\n" + l.getRequirements().getOverview() + "*",
                        true);
            }
        }
        else this.response = INVALID;

        return this;
    }
}
