package com.calculusmaster.endlessrpg.gameplay.battle.dungeon.room;

import java.util.List;
import java.util.SplittableRandom;

public abstract class TreasureRoom extends DungeonRoom
{
    public TreasureRoom(int... validChoices)
    {
        super(RoomType.TREASURE, validChoices);
    }

    public static TreasureRoom createRandom()
    {
        List<? extends TreasureRoom> pool = List.of(
                new PilesOfGoldTreasureRoom(),
                new SingleChestTreasureRoom(),
                new DoubleChestTreasureRoom()
        );

        return pool.get(new SplittableRandom().nextInt(pool.size()));
    }
}
