package com.calculusmaster.endlessrpg.gameplay.resources.enums;

import com.calculusmaster.endlessrpg.gameplay.resources.ResourceTraitRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public interface Resource
{
    String getName();

    int getTier();

    ResourceTraitRegistry getTraits();

    static List<Resource> all()
    {
        List<Resource> combined = new ArrayList<>();

        combined.addAll(List.of(RawResource.values()));
        combined.addAll(List.of(RefinedResource.values()));

        return combined;
    }

    static Resource cast(String input)
    {
        RawResource raw = RawResource.cast(input);
        RefinedResource refined = RefinedResource.cast(input);

        if(raw != null) return raw;
        else if(refined != null) return refined;
        else return null;
    }

    public static final LinkedHashMap<Resource, ResourceTraitRegistry> RESOURCE_TRAITS = new LinkedHashMap<>();

    interface TraitRegistryEditor
    {
        ResourceTraitRegistry edit(ResourceTraitRegistry input);
    }
}
