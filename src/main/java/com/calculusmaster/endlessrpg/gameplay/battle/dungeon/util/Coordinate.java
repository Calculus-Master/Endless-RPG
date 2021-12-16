package com.calculusmaster.endlessrpg.gameplay.battle.dungeon.util;

import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.map.CoreMapGenerator;

import java.util.Objects;

public final class Coordinate
{
    public int row, column;

    public static Coordinate of(int r, int c)
    {
        Coordinate coordinate = new Coordinate();
        coordinate.row = r;
        coordinate.column = c;
        return coordinate;
    }

    public double distance(Coordinate target)
    {
        return Math.pow(Math.pow(target.row - this.row, 2) + Math.pow(target.column - this.column, 2), 0.5);
    }

    public Coordinate shift(Direction dir)
    {
        return switch(dir) {
            case UP -> Coordinate.of(this.row - 1, this.column);
            case DOWN -> Coordinate.of(this.row + 1, this.column);
            case LEFT -> Coordinate.of(this.row, this.column - 1);
            case RIGHT -> Coordinate.of(this.row, this.column + 1);
        };
    }

    public boolean isInvalid(CoreMapGenerator map)
    {
        return this.row < 0 || this.row >= map.rows || this.column < 0 || this.column >= map.columns;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;

        if(o == null || getClass() != o.getClass()) return false;

        Coordinate that = (Coordinate)o;
        return this.row == that.row && this.column == that.column;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.row, this.column);
    }
}
