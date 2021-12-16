package com.calculusmaster.endlessrpg.gameplay.battle.dungeon.room;

import java.util.List;

public class SpawnRoom extends DungeonRoom
{
    public SpawnRoom()
    {
        super(RoomType.SPAWN);

        this.complete();
    }

    @Override
    public String getDescription()
    {
        return "This is the entrance to the Dungeon.";
    }

    @Override
    public List<String> getChoiceDescription()
    {
        return List.of();
    }

    @Override
    public void execute(int choice)
    {

    }
}
