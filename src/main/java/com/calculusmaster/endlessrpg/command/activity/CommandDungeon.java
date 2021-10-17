package com.calculusmaster.endlessrpg.command.activity;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.battle.Dungeon;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.LocationType;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Objects;

public class CommandDungeon extends Command
{
    public CommandDungeon(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        boolean start = this.msg.length == 2 && (this.msg[1].equals("start") || this.msg[1].equals("enter"));
        boolean next = this.msg.length == 2 && this.msg[1].equals("next");

        if(start)
        {
            if(Dungeon.isInDungeon(this.player.getId()) || Battle.isInBattle(this.player.getId())) this.response = "You are already in another Dungeon or Battle!";
            else if(!Realm.CURRENT.getLocation(this.playerData.getLocationID()).getType().equals(LocationType.DUNGEON)) this.response = "You have to be at a Dungeon Location to enter a Dungeon!";
            else
            {
                int averageLevel = this.playerData.getCharacterList().stream().map(RPGCharacter::build).mapToInt(RPGCharacter::getLevel).sum() / this.playerData.getCharacterList().size();
                Dungeon d = Dungeon.create(this.playerData, Realm.CURRENT.getLocation(this.playerData.getLocationID()), averageLevel, this.event);
                d.sendStartEmbed();

                this.embed = null;
            }
        }
        else if(next)
        {
            if(!Dungeon.isInDungeon(this.player.getId())) this.response = "You are not currently in a dungeon!";
            else
            {
                Dungeon d = Objects.requireNonNull(Dungeon.instance(this.player.getId()));

                if(d.isActive()) this.response = "You are actively in a Dungeon Encounter! You must wait for its completion to move to the next one!";
                else
                {
                    d.nextEncounter();

                    this.embed = null;
                }
            }
        }
        else this.response = INVALID;

        return this;
    }
}