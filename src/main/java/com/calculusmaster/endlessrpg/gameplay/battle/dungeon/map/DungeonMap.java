package com.calculusmaster.endlessrpg.gameplay.battle.dungeon.map;

import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.Dungeon;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.room.BossRoom;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.room.DungeonRoom;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.room.SpawnRoom;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.util.Coordinate;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.util.MapCode;

public class DungeonMap
{
    private final CoreMapGenerator core;
    private final DungeonRoom[][] rooms;

    public int rows;
    public int columns;

    public DungeonMap(CoreMapGenerator core)
    {
        this.core = core;
        this.rooms = new DungeonRoom[core.rows][core.columns];

        this.rows = this.core.rows;
        this.columns = this.core.columns;

        this.create();
    }

    private void create()
    {
        this.iterateRooms((r, c) -> this.rooms[r][c] = switch(MapCode.from(this.core.map[r][c])) {
            case EMPTY -> null;
            case ROOM -> DungeonRoom.getRandomEncounterRoom();
            case SPAWN -> new SpawnRoom();
            case BOSS -> new BossRoom();
        });
    }

    public void completeRoomSetup(Dungeon dungeon)
    {
        this.iterateRooms((r, c) -> {
            if(this.rooms[r][c] != null)
            {
                this.rooms[r][c].setDungeon(dungeon);
                this.rooms[r][c].setPosition(Coordinate.of(r, c));
            }
        });
    }

    private void iterateRooms(RowColumnConsumer consumer)
    {
        for(int r = 0; r < this.core.rows; r++) for(int c = 0; c < this.core.columns; c++) consumer.run(r, c);
    }

    private interface RowColumnConsumer { void run(int r, int c); }

    public Coordinate getSpawn()
    {
        return this.core.spawn;
    }

    public DungeonRoom getRoom(Coordinate c)
    {
        return this.rooms[c.row][c.column];
    }

    public CoreMapGenerator core()
    {
        return this.core;
    }

    public int getSize()
    {
        return this.core.rooms;
    }
}
