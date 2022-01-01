package com.calculusmaster.endlessrpg.gameplay.loot;

import com.calculusmaster.endlessrpg.gameplay.enums.LootComponent;
import com.calculusmaster.endlessrpg.util.Global;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class LootComponentsContainer
{
    private LinkedHashMap<LootComponent, Integer> components;

    public LootComponentsContainer()
    {
        this.components = new LinkedHashMap<>();
    }

    public LootComponentsContainer(LootComponent... components)
    {
        this();
        List.of(components).forEach(c -> this.components.put(c, 1));
    }

    public LootComponentsContainer with(LootComponent component, int amount)
    {
        this.components.put(component, amount);
        return this;
    }

    public boolean has(LootComponent component)
    {
        return this.components.containsKey(component);
    }

    public int getAmount(LootComponent component)
    {
        return this.components.get(component);
    }

    public boolean isEmpty()
    {
        return this.components.isEmpty();
    }

    public String getOverview()
    {
        return this.components.keySet().stream().map(l -> Global.normalize(l.toString()) + " – " + this.components.get(l)).collect(Collectors.joining("\n"));
    }
}
