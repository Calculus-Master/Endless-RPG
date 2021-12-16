package com.calculusmaster.endlessrpg.gameplay.battle.dungeon.room;

import java.util.List;

public class DoubleChestTreasureRoom extends TreasureRoom
{
    public DoubleChestTreasureRoom()
    {
        super(1, 2, 3, 4);
    }

    @Override
    public String getDescription()
    {
        return "Two treasure chests stand in the middle of the room.";
    }

    @Override
    public List<String> getChoiceDescription()
    {
        return List.of(
                "Ignore",
                "Open the chest on the left",
                "Open the chest on the right",
                "Open both chests"
        );
    }

    @Override
    public void execute(int choice)
    {

    }
}
