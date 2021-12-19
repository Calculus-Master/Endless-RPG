package com.calculusmaster.endlessrpg.gameplay.tasks;

import com.calculusmaster.endlessrpg.gameplay.character.RPGRawResourceContainer;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.gameplay.world.skills.RawResource;
import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public enum Achievement
{
    CREATE_A_CHARACTER("A New Adventurer", "Create your first character.", new AchievementReward());

    private final String name;
    private final String description;
    private final AchievementReward reward;

    Achievement(String name, String desc, AchievementReward reward)
    {
        this.name = name;
        this.description = desc;
        this.reward = reward;
    }

    public void grant(PlayerDataQuery player)
    {
        player.DM("You earned an Achievement: `" + this.name + "` (" + this.description + ")\nYou earned the following rewards:\n" + this.reward.summary());

        final ExecutorService threads = Executors.newCachedThreadPool();

        if(this.reward.gold > 0) threads.execute(() -> player.addGold(this.reward.gold));

        if(!this.reward.loot.isEmpty()) threads.execute(() -> this.reward.loot.forEach(l -> {
            l.upload();
            player.addLootItem(l.getLootID());
        }));

        if(!this.reward.resources.isEmpty()) threads.execute(() -> Arrays.stream(RawResource.values()).forEach(r -> {
            if(this.reward.resources.has(r)) player.addResource(r, this.reward.resources.get(r));
        }));
    }

    private static class AchievementReward
    {
        int gold;
        List<LootItem> loot;
        RPGRawResourceContainer resources;

        {
            this.gold = 0;
            this.loot = new ArrayList<>();
            this.resources = new RPGRawResourceContainer();
        }

        String summary()
        {
            StringBuilder out = new StringBuilder();

            if(this.gold > 0) out.append("Gold: ").append(this.gold).append("\n");

            if(!this.loot.isEmpty()) out.append("Loot: ").append(this.loot.stream().map(LootItem::getName).collect(Collectors.joining(", "))).append("\n");

            if(!this.resources.isEmpty()) out.append("Resources: ").append(this.resources.getFullOverview()).append("\n");

            return out.toString();
        }

        AchievementReward withGold(int gold)
        {
            this.gold = gold;
            return this;
        }

        AchievementReward withLoot(LootItem... loot)
        {
            this.loot.addAll(List.of(loot));
            return this;
        }

        AchievementReward withResources(Supplier<RPGRawResourceContainer> resources)
        {
            this.resources = resources.get();
            return this;
        }
    }
}
