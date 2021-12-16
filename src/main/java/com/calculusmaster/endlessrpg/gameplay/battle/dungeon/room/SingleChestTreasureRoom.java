package com.calculusmaster.endlessrpg.gameplay.battle.dungeon.room;

import java.util.List;
import java.util.SplittableRandom;

public class SingleChestTreasureRoom extends TreasureRoom
{
    public SingleChestTreasureRoom()
    {
        super(1, 2);
    }

    @Override
    public String getDescription()
    {
        return "A single treasure chest stands in the middle of the room.";
    }

    @Override
    public List<String> getChoiceDescription()
    {
        return List.of(
                "Ignore",
                "Open"
        );
    }

    @Override
    public void execute(int choice)
    {
        switch(choice)
        {
            case 1 -> {
                this.dungeon.addResult("You safely ignore the treasure chest and continue on.");

                this.dungeon.completeCurrentRoom();
            }
            case 2 -> {
                //Pick result
                int r = new SplittableRandom().nextInt(100);

                //Result 1: Success
                //Result 2: Battle, then success
                //Result 3: Mimic
            }
        }
    }
}
