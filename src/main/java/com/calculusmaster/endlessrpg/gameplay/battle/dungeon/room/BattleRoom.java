package com.calculusmaster.endlessrpg.gameplay.battle.dungeon.room;

import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.Dungeon;
import com.calculusmaster.endlessrpg.gameplay.battle.enemy.EnemyArchetype;
import com.calculusmaster.endlessrpg.gameplay.battle.player.AIPlayer;
import com.calculusmaster.endlessrpg.gameplay.battle.player.UserPlayer;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public class BattleRoom extends DungeonRoom
{
    private SplittableRandom random;
    private List<RPGCharacter> enemies;

    public BattleRoom()
    {
        super(RoomType.BATTLE);

        this.random = new SplittableRandom();
        this.enemies = new ArrayList<>();
    }

    @Override
    public String getDescription()
    {
        return "A group of enemies appeared!";
    }

    @Override
    public List<String> getChoiceDescription()
    {
        return List.of();
    }

    @Override
    public void execute(int choice)
    {
        this.createEnemies();
        AIPlayer enemies = new AIPlayer(this.enemies);

        Battle b = Battle.createDungeon(this.dungeon.getPlayers().stream().map(UserPlayer::new).toList(), enemies, this.dungeon.getLocation());

        b.setEvent(this.dungeon.getEvent());
        b.sendTurnEmbed();

        this.dungeon.addTag(Dungeon.DungeonMetaTag.AWAITING_BATTLE_RESULTS);
    }

    private void createEnemies()
    {
        int playerTotal = this.dungeon.getPlayers().stream().mapToInt(dp -> dp.party.size()).sum();
        int distanceSpawn = (int)(this.position.distance(this.dungeon.getMap().getSpawn()));

        int value;
        int totalEnemies;

        try
        {
            value = this.random.nextInt(distanceSpawn, (int)(distanceSpawn * 1.2)) + this.random.nextInt((int)(playerTotal * 0.5), (int)(playerTotal * 0.7));
            totalEnemies = this.random.nextInt((int)(value * 0.8), (int)(value * 1.2));
        }
        catch (IllegalArgumentException e)
        {
            value = distanceSpawn + playerTotal * 3 / 4;

            if(value > 3) totalEnemies = this.random.nextInt((int)(value * 0.8), (int)(value * 1.2));
            else totalEnemies = value;
        }

        for(int i = 0; i < totalEnemies; i++)
        {
            int level = this.random.nextInt((int)(this.dungeon.getLevel() * 0.8), (int)(this.dungeon.getLevel() * 1.3));
            EnemyArchetype enemyType = this.random.nextInt(100) < 80 ? this.dungeon.getLocation().getEnemyArchetype() : EnemyArchetype.RANDOM;

            this.enemies.add(enemyType.create(level));
        }
    }
}
