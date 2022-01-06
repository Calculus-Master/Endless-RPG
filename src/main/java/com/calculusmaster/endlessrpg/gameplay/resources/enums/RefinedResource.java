package com.calculusmaster.endlessrpg.gameplay.resources.enums;

import com.calculusmaster.endlessrpg.gameplay.enums.LootComponentType;
import com.calculusmaster.endlessrpg.gameplay.loot.LootTrait;
import com.calculusmaster.endlessrpg.gameplay.resources.ResourceTraitRegistry;

public enum RefinedResource implements Resource
{
    REFINED_MINING_T1_INGOT(1, "Copper Ingot", r -> r.withTrait(LootComponentType.CoreComponentType.GENERAL, LootTrait.WISE));

    private final int tier;
    private final String name;

    private final ResourceTraitRegistry traits;

    RefinedResource(int tier, String name, TraitRegistryEditor editor)
    {
        this.tier = tier;
        this.name = name;

        this.traits = editor == null ? ResourceTraitRegistry.of(this) : editor.edit(ResourceTraitRegistry.of(this));
    }

    RefinedResource(int tier, String name)
    {
        this(tier, name, null);
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public int getTier()
    {
        return this.tier;
    }

    @Override
    public ResourceTraitRegistry getTraits()
    {
        return this.traits;
    }

    public static RefinedResource cast(String input)
    {
        for(RefinedResource r : RefinedResource.values()) if(r.toString().equalsIgnoreCase(input) || (!r.getName().isEmpty() && r.getName().equalsIgnoreCase(input))) return r;
        return null;
    }
}
