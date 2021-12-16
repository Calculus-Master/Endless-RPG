package com.calculusmaster.endlessrpg.gameplay.battle.dungeon.room;

import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.NewDungeon;
import com.calculusmaster.endlessrpg.gameplay.battle.player.AIPlayer;
import com.calculusmaster.endlessrpg.gameplay.battle.player.UserPlayer;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public class PilesOfGoldTreasureRoom extends TreasureRoom
{
    public PilesOfGoldTreasureRoom()
    {
        super(1, 2, 3);
    }

    @Override
    public String getDescription()
    {
        return "This room is filled with piles of gold.";
    }

    @Override
    public List<String> getChoiceDescription()
    {
        return List.of(
                "Ignore",
                "Steal a few pieces",
                "Attempt to steal everything"
        );
    }

    @Override
    public void execute(int choice)
    {
        switch(choice)
        {
            case 1 -> {
                this.dungeon.addResult("You safely ignore the gold and continue on.");

                this.dungeon.completeCurrentRoom();
            }
            case 2 -> {
                //Pick the room result
                int r = new SplittableRandom().nextInt(100);

                //Result 1: Success
                if(r < 40)
                {
                    int amount = new SplittableRandom().nextInt(100, 500);

                    this.dungeon.reward().gold += amount;
                    this.dungeon.addResult("You successfully stole " + amount + " gold!");
                    this.dungeon.completeCurrentRoom();
                }
                //Result 2: Random characters take damage
                else if(r < 80)
                {
                    int targetsCount = new SplittableRandom().nextInt(this.dungeon.getPlayers().size());
                    int healthPercentage = new SplittableRandom().nextInt(10, 30);

                    List<String> completed = new ArrayList<>();
                    while(completed.size() != targetsCount)
                    {
                        NewDungeon.DungeonPlayer player = this.dungeon.getPlayers().get(new SplittableRandom().nextInt(this.dungeon.getPlayers().size()));
                        RPGCharacter target = player.party.get(new SplittableRandom().nextInt(player.party.size()));

                        if(target.getHealth() > 0 && !completed.contains(target.getCharacterID()))
                        {
                            target.damage((int)(target.getHealth() * (double)(healthPercentage) / 100.));
                            completed.add(target.getCharacterID());
                        }
                    }

                    this.dungeon.addResult("The gold was a trap! " + targetsCount + " characters (" + String.join(", ", completed) + ") lost " + healthPercentage + "% of their health!");
                    this.dungeon.completeCurrentRoom();
                }
                //Result 3: Battle
                else
                {
                    int enemyCount = new SplittableRandom().nextInt(this.dungeon.getPlayers().size(), this.dungeon.getPlayers().size() * 2);
                    List<RPGCharacter> enemies = new ArrayList<>();
                    for(int i = 0; i < enemyCount; i++) enemies.add(this.dungeon.getLocation().getEnemyArchetype().create(this.dungeon.getLevel() - 1));
                    AIPlayer ai = new AIPlayer(enemies);

                    Battle b = Battle.createDungeon(this.dungeon.getPlayers().stream().map(UserPlayer::new).toList(), ai, this.dungeon.getLocation());

                    b.setEvent(this.dungeon.getEvent());
                    b.sendTurnEmbed();

                    this.dungeon.addResult("Some enemies appeared!");
                }
            }
        }
    }
}
