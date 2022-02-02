package com.calculusmaster.endlessrpg.command.economy;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.gameplay.resources.enums.Resource;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.gameplay.world.shop.LocationShop;
import com.calculusmaster.endlessrpg.gameplay.world.shop.LocationShopCache;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Objects;
import java.util.concurrent.Executors;

public class CommandBuy extends Command
{
    public CommandBuy(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        if(!LocationShop.hasShop(Realm.CURRENT.getLocation(this.playerData.getLocationID())))
        {
            this.response = "This location does not have any Shops!";
            return this;
        }

        LocationShop shop = LocationShop.getShop(this.playerData.getLocationID());
        LocationShopCache cache = shop.getCacheView(this.player.getId());

        boolean loot = this.msg.length == 3 && this.msg[1].equals("loot") && this.isNumeric(2);
        boolean resources = this.msg.length >= 4 && this.msg[1].equals("resource") && this.isNumeric(2) && Resource.cast(this.msgMultiWordContent(3)) != null;

        if(loot)
        {
            int index = this.getInt(2) - 1;

            if(index < 0 || index >= shop.getLoot().size()) this.response = "Invalid Shop Entry index!";
            else
            {
                RPGCharacter active = this.playerData.getActiveCharacter();
                int cost = shop.getLoot().get(index).cost;

                if(active.getGold() < cost) this.response = "You need " + cost + " Gold to buy this Loot!";
                else
                {
                    LootItem copy = LootItem.copy(shop.getLoot().get(index).item);

                    active.removeGold(cost);
                    active.addLoot(copy.getLootID());

                    cache.purchaseLoot(copy.getLootID());

                    Executors.newSingleThreadScheduledExecutor().execute(() -> {
                        copy.upload();

                        active.updateGold();
                        active.updateLoot();
                    });

                    this.response = "You purchased `" + copy.getName() + "` for " + cost + " gold!";
                }
            }
        }
        else if(resources)
        {
            Resource resource = Objects.requireNonNull(Resource.cast(this.msgMultiWordContent(3)));
            int quantity = this.getInt(2);
            int unitCost = shop.getResourceCost(resource);

            if(!shop.getResources().containsKey(resource)) this.response = "The shop does not sell any `" + resource.getName() + "`!";
            else if(cache.getEffectiveResourceAmount(resource) <= 0) this.response = "The shop is out of stock for `" + resource.getName() + "`!";
            else if(cache.getEffectiveResourceAmount(resource) < quantity) this.response = "The shop does not have that much `" + resource.getName() + "` available!";
            else
            {
                RPGCharacter active = this.playerData.getActiveCharacter();
                int cost = unitCost * quantity;

                if(active.getGold() < cost) this.response = "Your character does not have enough gold to purchase " + quantity + " units of `" + resource.getName() + "` (Total Cost: %s, Balance: %s)~".formatted(cost, active.getGold());
                else
                {
                    active.removeGold(cost);
                    active.getResources().increase(resource, quantity);

                    cache.purchaseResource(resource, quantity);

                    Executors.newSingleThreadScheduledExecutor().execute(() -> {
                        active.updateGold();
                        active.updateResources();
                    });

                    this.response = "You purchased " + quantity + " units of `" + resource.getName() + "` for " + cost + " gold!";
                }
            }
        }
        else this.response = INVALID;

        return this;
    }
}
