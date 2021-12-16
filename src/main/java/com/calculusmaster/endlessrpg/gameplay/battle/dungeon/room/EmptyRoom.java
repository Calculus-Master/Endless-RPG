package com.calculusmaster.endlessrpg.gameplay.battle.dungeon.room;

import java.util.List;

public class EmptyRoom extends DungeonRoom
{
    public EmptyRoom()
    {
        super(RoomType.EMPTY);
    }

    @Override
    public String getDescription()
    {
        return "This room is empty.";
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
