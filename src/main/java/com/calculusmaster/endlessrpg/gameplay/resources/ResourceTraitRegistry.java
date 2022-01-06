package com.calculusmaster.endlessrpg.gameplay.resources;

import com.calculusmaster.endlessrpg.gameplay.enums.LootComponentType;
import com.calculusmaster.endlessrpg.gameplay.loot.LootTrait;
import com.calculusmaster.endlessrpg.gameplay.resources.enums.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class ResourceTraitRegistry
{
    private final Resource resource;
    private final LinkedHashMap<LootComponentType, List<LootTrait>> traits;

    private ResourceTraitRegistry(Resource resource)
    {
        this.resource = resource;
        this.traits = new LinkedHashMap<>();

        Arrays.stream(LootComponentType.values()).forEach(lct -> this.traits.put(lct, new ArrayList<>()));
    }

    public static ResourceTraitRegistry of(Resource resource)
    {
        return new ResourceTraitRegistry(resource);
    }

    //Primary Accessor
    public List<LootTrait> get(LootComponentType component)
    {
        return this.traits.get(component);
    }

    //Traits for a specific Component Type
    public ResourceTraitRegistry withTrait(LootComponentType component, LootTrait... traits)
    {
        this.traits.put(component, List.of(traits));
        return this;
    }

    //Traits for all Component Types
    public ResourceTraitRegistry withTrait(LootTrait... traits)
    {
        this.traits.forEach((lct, list) -> list.addAll(List.of(traits)));
        return this;
    }

    //Traits for all Component Types of a specific Core Component Type
    public ResourceTraitRegistry withTrait(LootComponentType.CoreComponentType core, LootTrait... traits)
    {
        this.traits.forEach((lct, list) -> {
            if(lct.isGeneral()) list.addAll(List.of(traits));
        });
        return this;
    }

    public void register()
    {
        Resource.RESOURCE_TRAITS.put(this.resource, this);
    }
}
