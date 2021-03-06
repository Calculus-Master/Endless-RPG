package com.calculusmaster.endlessrpg.gameplay.loot;

import com.calculusmaster.endlessrpg.gameplay.enums.LootComponentType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LootComponentsContainer
{
    private LinkedHashMap<LootComponentType, Integer> components;

    public LootComponentsContainer()
    {
        this.components = new LinkedHashMap<>();
    }

    public LootComponentsContainer(LootComponentType... components)
    {
        this();
        List.of(components).forEach(c -> this.components.put(c, 1));
    }

    public LootComponentsContainer with(LootComponentType component, int amount)
    {
        this.components.put(component, amount);
        return this;
    }

    public boolean matches(List<LootComponent> components)
    {
        for(Map.Entry<LootComponentType, Integer> e : this.components.entrySet())
            if(e.getValue() > components.stream().filter(component -> component.getType().equals(e.getKey())).count())
                return false;
        return true;
    }

    public boolean has(LootComponentType component)
    {
        return this.components.containsKey(component);
    }

    public int getAmount(LootComponentType component)
    {
        return this.components.get(component);
    }

    public boolean isEmpty()
    {
        return this.components.isEmpty();
    }

    public String getOverview()
    {
        return this.components.keySet().stream().map(l -> l.getName() + " – " + this.components.get(l)).collect(Collectors.joining("\n"));
    }
}
