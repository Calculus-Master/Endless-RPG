package com.calculusmaster.endlessrpg.gameplay.battle.dungeon.map;

import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.util.Coordinate;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.util.Direction;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.util.MapCode;

import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public class CoreMapGenerator
{
    private final SplittableRandom random;

    public final int[][] map;
    public Coordinate spawn;
    public Coordinate boss;
    public int rooms;

    public final int rows;
    public final int columns;

    public CoreMapGenerator(int minSize, int maxSize)
    {
        this.random = new SplittableRandom();
        this.map = new int[this.random.nextInt(maxSize) + minSize][this.random.nextInt(maxSize) + minSize];

        this.spawn = null;
        this.boss = null;
        this.rooms = 0;

        this.rows = this.map.length;
        this.columns = this.map[0].length;

        this.generate();
    }

    private void generate()
    {
        //Pick Spawn Room
        Coordinate spawn = Coordinate.of(this.random.nextInt(this.rows), this.random.nextInt(this.columns));
        this.spawn = spawn;

        //Add Spawn Room to Map
        this.map[spawn.row][spawn.column] = MapCode.ROOM.ordinal();

        //Generate maximum number of Rooms
        final int total = this.rows * this.columns;
        this.rooms = (int)(total * 0.6);
        int currentRooms = 0;

        //Populate the dungeon with rooms until it hits the maximum count
        Coordinate current = spawn;
        while(currentRooms < this.rooms)
        {
            //Select room
            Coordinate room = current;

            //Find all valid positions
            List<Coordinate> emptyPositions = this.nearbyEmptyPositions(room);

            //If there is no valid position (i.e, all nearby spots have a room) skip to the next iteration of the loop
            if(!emptyPositions.isEmpty())
            {
                //Select a position
                Coordinate target = emptyPositions.get(this.random.nextInt(emptyPositions.size()));

                //Populate the position
                this.map[target.row][target.column] = MapCode.ROOM.ordinal();
                currentRooms++;

                current = target;
            }
            else current = this.randomRoom();
        }

        //Set the Spawn room to the correct code
        this.map[spawn.row][spawn.column] = MapCode.SPAWN.ordinal();

        this.addBossRoom();
    }

    public void print()
    {
        this.printMap();
        System.out.printf("Size: %s%n", this.rooms);
        System.out.printf("Spawn Position: (%s, %s)%n", this.spawn.row, this.spawn.column);
        System.out.printf("Boss Room: (%s, %s) (Distance: %s)%n", this.boss.row, this.boss.column, (int)(this.boss.distance(this.spawn)));
    }

    //Returns the location of the Boss Room (the furthest away from spawn)
    private void addBossRoom()
    {
        double max = 0;
        Coordinate boss = this.spawn;

        for(int r = 0; r < this.rows; r++)
        {
            for(int c = 0; c < this.columns; c++)
            {
                if(this.map[r][c] == MapCode.ROOM.ordinal())
                {
                    double distance = this.spawn.distance(Coordinate.of(r, c));

                    if(distance > max)
                    {
                        max = distance;
                        boss = Coordinate.of(r, c);
                    }
                }
            }
        }

        this.boss = boss;
        this.map[boss.row][boss.column] = MapCode.BOSS.ordinal();
    }

    //Returns a random room on the map
    private Coordinate randomRoom()
    {
        int outR = -1;
        int outC = -1;

        while(outR < 0 && outC < 0)
        {
            int r = this.random.nextInt(this.rows);
            int c = this.random.nextInt(this.columns);

            if(this.map[r][c] == MapCode.ROOM.ordinal())
            {
                outR = r;
                outC = c;
            }
        }

        return Coordinate.of(outR, outC);
    }

    //Returns all nearby positions that are not a room
    private List<Coordinate> nearbyEmptyPositions(Coordinate c)
    {
        return this.nearbyPositions(c).stream().filter(p -> this.map[p.row][p.column] == MapCode.EMPTY.ordinal()).collect(Collectors.toList());
    }

    //Returns all possible nearby positions that are not out of bounds
    public List<Coordinate> nearbyPositions(Coordinate pos)
    {
        return Arrays.stream(Direction.values())
                .map(pos::shift) //Coordinates of all nearby Directions
                .filter(c -> !c.isInvalid(this)) //Check validity
                .collect(Collectors.toList()); //Modifiable List
    }

    private void printMap()
    {
        StringBuilder sb = new StringBuilder("[\n");
        for(int[] ints : this.map) sb.append(Arrays.toString(ints)).append("\n");
        System.out.println(sb.append("]"));
    }
}
