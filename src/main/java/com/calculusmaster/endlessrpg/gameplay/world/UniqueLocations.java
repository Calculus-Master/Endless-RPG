package com.calculusmaster.endlessrpg.gameplay.world;

import com.calculusmaster.endlessrpg.gameplay.battle.enemy.EnemyArchetype;
import com.calculusmaster.endlessrpg.gameplay.enums.LocationType;
import com.calculusmaster.endlessrpg.gameplay.enums.Weather;
import com.calculusmaster.endlessrpg.gameplay.world.skills.RawResource;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public class UniqueLocations
{
    public static final List<Location> LOCATIONS = new ArrayList<>();

    public static void init()
    {
        LocationBuilder
                .of(LocationType.DESERT)
                .withName("The Desolate Fields")
                .withEnemy(EnemyArchetype.SKELETON)
                .withWeather(Weather.HARSH_SUN)
                .withResource(RawResource.FARMING_T9, 20)
                .register();

        LocationBuilder
                .of(LocationType.FOREST)
                .withName("The Dual Forest")
                .withEnemy(EnemyArchetype.TROLL)
                .withWeather(Weather.OVERCAST)
                .withResource(RawResource.WOODCUTTING_T7, 10)
                .withResource(RawResource.WOODCUTTING_T8, 10)
                .withResource(RawResource.WOODCUTTING_T9, 10)
                .register();
    }

    public static Location getRandom()
    {
        return LOCATIONS.get(new SplittableRandom().nextInt(LOCATIONS.size()));
    }

    private static class LocationBuilder
    {
        private Location location;

        static LocationBuilder of(LocationType type)
        {
            return new LocationBuilder(type);
        }

        LocationBuilder withName(String name)
        {
            this.location.setName(name + " :star2:");
            return this;
        }

        LocationBuilder withWeather(Weather weather)
        {
            this.location.setWeather(weather);
            return this;
        }

        LocationBuilder withEnemy(EnemyArchetype archetype)
        {
            this.location.setEnemyArchetype(archetype);
            return this;
        }

        LocationBuilder withResource(RawResource r, int amount)
        {
            this.location.getResources().set(r, amount);
            return this;
        }

        void register()
        {
            LOCATIONS.add(this.location);
        }

        private LocationBuilder(LocationType type)
        {
            this.location = Location.create(type);
        }
    }
}
