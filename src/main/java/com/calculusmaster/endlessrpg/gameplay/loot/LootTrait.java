package com.calculusmaster.endlessrpg.gameplay.loot;

import com.calculusmaster.endlessrpg.gameplay.loot.traits.AbstractLootTrait;
import com.calculusmaster.endlessrpg.gameplay.loot.traits.WiseLootTrait;
import com.calculusmaster.endlessrpg.util.Global;

import java.util.function.Supplier;

public enum LootTrait
{
    WISE(WiseLootTrait::new);

    private final Supplier<? extends AbstractLootTrait> supplier;

    LootTrait(Supplier<? extends AbstractLootTrait> supplier)
    {
        this.supplier = supplier;
    }

    public AbstractLootTrait get()
    {
        return this.supplier.get();
    }

    public static LootTrait cast(String input)
    {
        return Global.castEnum(input, values());
    }
}
