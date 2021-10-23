package com.calculusmaster.endlessrpg.gameplay.player;

import com.calculusmaster.endlessrpg.gameplay.character.RPGRawResourceContainer;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.gameplay.world.skills.RawResource;
import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;
import com.calculusmaster.endlessrpg.util.Global;

import java.util.ArrayList;
import java.util.List;

public class TradeOffer
{
    private PlayerDataQuery player;

    private int gold;
    private List<String> loot;
    private RPGRawResourceContainer resources;

    public TradeOffer(PlayerDataQuery player)
    {
        this.player = player;

        this.gold = 0;
        this.loot = new ArrayList<>();
        this.resources = new RPGRawResourceContainer();
    }

    public void clear()
    {
        this.clearGold();
        this.clearLoot();
        this.clearResources();
    }

    //Gold
    public void addGold(int amount)
    {
        this.gold += amount;
    }

    public void removeGold(int amount)
    {
        this.gold = Math.max(0, this.gold - amount);
    }

    public void setGold(int amount)
    {
        this.gold = amount;
    }

    public void clearGold()
    {
        this.gold = 0;
    }

    public int getGold()
    {
        return this.gold;
    }

    //Loot
    public void addLoot(String... lootIDs)
    {
        this.loot.addAll(List.of(lootIDs));
    }

    public void removeLoot(String... lootIDs)
    {
        this.loot.removeIf(s -> List.of(lootIDs).contains(s));
    }

    public void setLoot(String... lootIDs)
    {
        this.loot = new ArrayList<>(List.of(lootIDs));
    }

    public void clearLoot()
    {
        this.loot = new ArrayList<>();
    }

    public List<String> getLoot()
    {
        return this.loot;
    }

    //Resources
    public void addResource(RawResource r, int amount)
    {
        this.resources.increase(r, amount);
    }

    public void removeResource(RawResource r, int amount)
    {
        this.resources.decrease(r, amount);
    }

    public void setResource(RawResource r, int amount)
    {
        this.resources.set(r, amount);
    }

    public void clearResources()
    {
        this.resources = new RPGRawResourceContainer();
    }

    public RPGRawResourceContainer getResources()
    {
        return this.resources;
    }

    public boolean isValid()
    {
        return this.player.getGold() >= this.gold && this.player.getLoot().containsAll(this.loot);
    }

    public void transfer(PlayerDataQuery receiver)
    {
        if(this.gold != 0)
        {
            this.player.removeGold(this.gold);

            receiver.addGold(this.gold);
        }

        if(!this.loot.isEmpty())
        {
            for(String s : this.loot)
            {
                this.player.removeLootItem(s);

                receiver.addLootItem(s);
            }
        }

        //TODO: RawResource trading using the Resource Bank (Implemented Resource Bank)
    }

    public String getOverview()
    {
        List<String> components = new ArrayList<>();

        if(this.gold != 0) components.add("Gold: " + this.gold);

        if(!this.loot.isEmpty())
        {
            List<LootItem> builtLoot = this.loot.stream().map(LootItem::build).toList();

            for(LootItem l : builtLoot) components.add("%s | %s | %s | %s".formatted(
                    l.getName(),
                    "ID: " + l.getLootID(),
                    "Type: " + Global.normalize(l.getLootType().toString()),
                    "Boosts: " + l.getBoostsOverview()
            ));
        }

        if(!this.resources.isEmpty()) components.add(this.resources.getFullOverview());

        if(components.isEmpty()) components.add("Nothing");

        StringBuilder out = new StringBuilder();

        out.append("```\n");

        for(String s : components) out.append(s).append("\n");
        out.deleteCharAt(out.length() - 1);

        out.append("```");

        return out.toString();
    }
}
