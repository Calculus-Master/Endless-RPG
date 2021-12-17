package com.calculusmaster.endlessrpg.gameplay.battle.dungeon.room;

import com.calculusmaster.endlessrpg.EndlessRPG;
import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.Dungeon;
import com.calculusmaster.endlessrpg.gameplay.battle.enemy.EnemyBuilder;
import com.calculusmaster.endlessrpg.gameplay.battle.player.AIPlayer;
import com.calculusmaster.endlessrpg.gameplay.battle.player.UserPlayer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;

public class BossRoom extends DungeonRoom
{
    private String desc;
    private static final String GENERIC_DESC = "\n\n***A mysterious figure emerges...\nIt is the only obstacle between you and victory...***";

    public BossRoom()
    {
        super(RoomType.BOSS);

        //Set the description
        List<String> pool = new BufferedReader(new InputStreamReader(Objects.requireNonNull(EndlessRPG.class.getResourceAsStream("/descriptions/dungeon_boss.txt")))).lines().toList();
        String desc = pool.get(new SplittableRandom().nextInt(pool.size()));
        this.desc = desc.replaceAll("(\\.\\.)", "...\n\n").replaceAll("wait", "*Wait*");
    }

    @Override
    public String getDescription()
    {
        return this.desc + GENERIC_DESC;
    }

    @Override
    public List<String> getChoiceDescription()
    {
        return List.of();
    }

    private AIPlayer createBoss()
    {
        int total = this.dungeon.getPlayers().stream().mapToInt(d -> d.party.size()).sum();
        int teamWeight = (int)(0.35 * total);
        int rand = new SplittableRandom().nextInt(5, 11);
        return new AIPlayer(EnemyBuilder.createDefault(this.dungeon.getLevel() + teamWeight + rand));
    }

    @Override
    public void execute(int choice)
    {
        AIPlayer boss = this.createBoss();
        Battle b = Battle.createDungeon(this.dungeon.getPlayers().stream().map(UserPlayer::new).toList(), boss, this.dungeon.getLocation());

        b.setEvent(this.dungeon.getEvent());
        b.sendTurnEmbed();

        this.dungeon.addTag(Dungeon.DungeonMetaTag.AWAITING_BATTLE_RESULTS);
    }
}
