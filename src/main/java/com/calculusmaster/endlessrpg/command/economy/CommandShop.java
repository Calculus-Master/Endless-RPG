package com.calculusmaster.endlessrpg.command.economy;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.loot.LootBuilder;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CommandShop extends Command
{
    public static final List<ShopEntry<LootItem>> SHOP_LOOT = new ArrayList<>();

    public CommandShop(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        boolean loot = this.msg.length == 2 && this.msg[1].equals("loot");

        if(loot)
        {
            this.embed.setTitle("Shop Loot Items");

            for(int i = 0; i < SHOP_LOOT.size(); i++)
            {
                LootItem l = SHOP_LOOT.get(i).item;
                this.embed.addField(
                        (i + 1) + ": " + Global.normalize(l.getLootType().toString()),
                        "*" + l.getName() + "*\nCost: `" + SHOP_LOOT.get(i).cost + "` Gold\nBoosts: " + l.getBoostsOverview() + "\n*Requirements:\n" + l.getRequirements().getOverview() + "*",
                        true);
            }
        }
        else this.response = INVALID;

        return this;
    }

    public static void init()
    {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(CommandShop::setShopLoot, 0, 4, TimeUnit.HOURS);
    }

    private static void setShopLoot()
    {
        SHOP_LOOT.clear();
        final SplittableRandom r = new SplittableRandom();

        int amount = r.nextInt(5, 10);
        for(int i = 0; i < amount; i++)
        {
            LootItem loot = LootBuilder.reward(LootType.getRandom(), r.nextInt(5, 50));
            int cost = loot.getBoosts().values().stream().mapToInt(x -> x).sum() * (new SplittableRandom().nextInt(2, 10) + 100);
            cost -= cost % 10;
            SHOP_LOOT.add(new ShopEntry<>(loot, cost));
        }
    }

    public static class ShopEntry<T>
    {
        public T item;
        public int cost;

        public ShopEntry(T item, int cost)
        {
            this.item = item;
            this.cost = cost;
        }
    }
}
