package com.calculusmaster.endlessrpg.gameplay.battle.dungeon.room;

import java.util.List;

public class BossRoom extends DungeonRoom
{
    public BossRoom()
    {
        super(RoomType.BOSS);
    }

    @Override
    public String getDescription()
    {
        return null; //TODO: Boss Room
    }

    @Override
    public List<String> getChoiceDescription()
    {
        return List.of();
    }

    @Override
    public void execute(int choice)
    {
        //TODO: Boss Room
        this.dungeon.getEvent().getChannel().sendMessage("Boss is WIP, defeated").queue();

        this.dungeon.completeCurrentRoom();
    }
}
