package com.calculusmaster.endlessrpg.command.economy;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.enums.LocationType;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.gameplay.resources.enums.Resource;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.gameplay.world.shop.LocationShop;
import com.calculusmaster.endlessrpg.gameplay.world.shop.LocationShopCache;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        LocationShopCache cache = shop.getCacheView(this.player.getId());

        boolean loot = this.msg.length == 2 && this.msg[1].equals("loot");
        boolean resources = this.msg.length == 2 && this.msg[1].equals("resources");

        if(loot)
        {
            this.embed.setTitle("Shop Loot Items");

            for(int i = 0; i < shop.getLoot().size(); i++)
            {
                LootItem l = shop.getLoot().get(i).item;
                boolean bought = cache.hasPurchased(l.getLootID());

                this.embed.addField(
                        (bought ? "~~" : "") + (i + 1) + ": " + Global.normalize(l.getLootType().toString()) + (bought ? "~~" : ""),
                        "*" + l.getName() + "*\nCost: `" + shop.getLoot().get(i).cost + "` Gold\nBoosts: " + l.getBoostsOverview() + "\n*Requirements:\n" + l.getRequirements().getOverview() + "*",
                        true);
            }
        }
        else if(resources)
        {
            List<Resource> depleted = new ArrayList<>();
            shop.getResources().forEach((r, v) -> {
                int total = v;
                int actual = cache.getEffectiveResourceAmount(r);

                String q = "Quantity Available: **" + actual + "** / " + total;
                String c = "Unit Cost: **" + shop.getResourceCost(r) + "g**";

                if(actual > 0) this.embed.addField(r.getName(), q + "\n" + c, true);
                else depleted.add(r);
            });

            if(!depleted.isEmpty()) this.embed.addField("Resources No Longer Available", depleted.stream().map(Resource::getName).collect(Collectors.joining("\n")), false);
        }
        else this.response = INVALID;

        return this;
    }
}
