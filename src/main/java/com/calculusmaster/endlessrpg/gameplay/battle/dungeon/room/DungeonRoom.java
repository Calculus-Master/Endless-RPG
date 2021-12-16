package com.calculusmaster.endlessrpg.gameplay.battle.dungeon.room;

import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.NewDungeon;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.util.Coordinate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public abstract class DungeonRoom
{
    private RoomType type;
    private boolean isCompleted;
    private List<Integer> validChoices;

    protected NewDungeon dungeon;
    protected Coordinate position;

    public DungeonRoom(RoomType type, int... validChoices)
    {
        this.type = type;
        this.isCompleted = false;

        this.validChoices = new ArrayList<>();
        for(int i : validChoices) this.validChoices.add(i);
    }

    //Abstract Methods
    public abstract String getDescription();

    public abstract List<String> getChoiceDescription();

    public abstract void execute(int choice);

    //Helpers
    protected <R extends DungeonRoom> R cast(Class<R> clazz)
    {
        return clazz.cast(this);
    }

    //Fields - Getters and Setters
    public boolean isComplete()
    {
        return this.isCompleted;
    }

    public void complete()
    {
        this.isCompleted = true;
    }

    public boolean isChoiceValid(int choice)
    {
        return this.validChoices.contains(choice);
    }

    public void setDungeon(NewDungeon dungeon)
    {
        this.dungeon = dungeon;
    }

    public void setPosition(Coordinate position)
    {
        this.position = position;
    }

    public RoomType getType()
    {
        return this.type;
    }

    public static DungeonRoom getRandomEncounterRoom()
    {
        List<RoomType> pool = new ArrayList<>();
        Arrays.stream(RoomType.values()).filter(rt -> rt.weight > 0).forEach(rt -> {
            for(int i = 0; i < rt.weight; i++) pool.add(rt);
        });

        return pool.get(new SplittableRandom().nextInt(pool.size())).create();
    }

    public enum RoomType
    {
        EMPTY(4, "E", EmptyRoom::new),
        SPAWN(0, "S", SpawnRoom::new),
        BATTLE(10, "F", BattleRoom::new),
        TREASURE(5, "T", TreasureRoom::createRandom),
        //LOOT(5, "L", ),
        //RESOURCES(5, "R", ),
        //DECISION(4, "C", ),
        BOSS(0, "B", BossRoom::new);

        private final Supplier<? extends DungeonRoom> supplier;
        private final String code;
        private final int weight;
        RoomType(int weight, String code, Supplier<? extends DungeonRoom> supplier) { this.weight = weight; this.code = code; this.supplier = supplier; }

        public DungeonRoom create()
        {
            return this.supplier.get();
        }

        public String code()
        {
            return this.code;
        }
    }
}
