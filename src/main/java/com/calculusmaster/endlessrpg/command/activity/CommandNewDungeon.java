package com.calculusmaster.endlessrpg.command.activity;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.NewDungeon;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.util.Coordinate;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.util.Direction;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CommandNewDungeon extends Command
{
    public CommandNewDungeon(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        //r!dungeon enter <optional @co-op>
        boolean enter = this.msg.length >= 2 && this.msg[1].equals("enter");

        //r!dungeon <accept:deny>
        boolean accept = this.msg.length == 2 && this.msg[1].equals("accept");
        boolean deny = this.msg.length == 2 && this.msg[1].equals("deny");
        //r!dungeon cancel
        boolean cancel = this.msg.length == 2 && this.msg[1].equals("cancel");

        //r!dungeon map
        boolean map = this.msg.length == 2 && this.msg[1].equals("map");

        //r!dungeon move <direction>
        boolean move = this.msg.length == 3 && this.msg[1].equals("move") && Direction.cast(this.msg[2]) != null;

        //r!dungeon interact <choice>
        boolean interact = this.msg.length == 3 && this.msg[1].equals("interact") && this.isNumeric(2);

        if(enter)
        {
            if(NewDungeon.isInDungeon(this.player.getId()))
            {
                this.response = "You are already in a dungeon!";
                return this;
            }

            Location location = Realm.CURRENT.getLocation(this.playerData.getLocationID());
            List<PlayerDataQuery> others = this.getMentions().stream().map(ISnowflake::getId).map(PlayerDataQuery::new).toList();

            NewDungeon dungeon = NewDungeon.create(location, this.event, this.playerData, others);

            if(others.isEmpty())
            {
                this.embed = null;

                dungeon.sendEmbed(NewDungeon.DungeonEmbed.START);

                dungeon.start();
            }
            else
            {
                this.response = "You await your allies... (Dungeon Level: %s, Total Players: %s)".formatted(dungeon.getLevel(), dungeon.getPlayers().size());

                others.forEach(p -> p.DM("**" + this.player.getName() + "** has invited you to join them in conquering `" + location.getName() + "`!\nTo join, use `r!dungeon accept`. To deny their request, use `r!dungeon deny`. *Note: If you deny the request you will not be able to join the Dungeon again!*"));
            }
        }
        else if(accept || deny || cancel)
        {
            NewDungeon dungeon = NewDungeon.instance(this.player.getId());

            if(dungeon == null) this.response = "You are not in a Dungeon!";
            else if((accept || deny) && dungeon.getLeader().data.getID().equals(this.player.getId())) this.response = "You are the leader! You cannot accept or deny a Dungeon request.";
            else if(cancel && !dungeon.getLeader().data.getID().equals(this.player.getId())) this.response = "Only the leader can cancel the Dungeon adventure!";
            else if((accept || deny) && !dungeon.getStatus().equals(NewDungeon.DungeonStatus.WAITING_FOR_PLAYERS)) this.response = "The Dungeon has already started!";
            else if(cancel)
            {
                NewDungeon.delete(this.player.getId());

                this.response = "You retreated from the Dungeon! No rewards earned!";
            }
            else
            {
                dungeon.setPlayerAccepted(this.player.getId(), accept);

                if(accept) this.response = "You have joined **" + dungeon.getLeader().data.getUsername() + "** in the Dungeon!";
                else if(deny) this.response = "You rejected **" + dungeon.getLeader().data.getUsername() + "**'s request to join them in the Dungeon...You cannot join back!";
            }
        }
        else if(map)
        {
            if(!NewDungeon.isInDungeon(this.player.getId())) this.response = "You are not in a Dungeon!";
            else if(!NewDungeon.instance(this.player.getId()).getStatus().equals(NewDungeon.DungeonStatus.ADVENTURING)) this.response = "You have not entered the Dungeon yet (waiting for allies)!";
            else
            {
                NewDungeon.instance(this.player.getId()).sendEmbed(NewDungeon.DungeonEmbed.MAP_EXPLORED);

                this.embed = null;
            }
        }
        else if(move)
        {
            NewDungeon dungeon = NewDungeon.instance(this.player.getId());

            if(dungeon == null) this.response = "You are not in a Dungeon!";
            else if(!dungeon.getLeader().data.getID().equals(this.player.getId())) this.response = "Only the Dungeon Leader is able to move your group through the Dungeon!";
            else if(!dungeon.getStatus().equals(NewDungeon.DungeonStatus.ADVENTURING)) this.response = "You have not entered the Dungeon yet (waiting for allies)!";
            else if(!dungeon.current().isComplete()) this.response = "You can only move after the current encounter has been completed!";
            else
            {
                Direction dir = Direction.cast(this.msg[2]);
                Coordinate target = dungeon.getPosition().shift(dir);

                if(target.isInvalid(dungeon.getMap().core())) this.response = "You cannot move in that direction!";
                else
                {
                    Executors.newSingleThreadScheduledExecutor().schedule(() -> dungeon.move(dir), 3, TimeUnit.SECONDS);

                    this.response = "Successfully moved " + Global.normalize(dir.toString()) + "! What awaits you?";
                }
            }
        }
        else if(interact)
        {
            NewDungeon dungeon = NewDungeon.instance(this.player.getId());

            if(dungeon == null) this.response = "You are not in a Dungeon!";
            else if(!dungeon.getLeader().data.getID().equals(this.player.getId())) this.response = "Only the Dungeon Leader is able to move your group through the Dungeon!";
            else if(!dungeon.getStatus().equals(NewDungeon.DungeonStatus.ADVENTURING)) this.response = "You have not entered the Dungeon yet (waiting for allies)!";
            else if(!dungeon.hasTag(NewDungeon.DungeonMetaTag.AWAITING_INTERACTION)) this.response = "There's nothing to interact with!";
            else
            {
                int choice = this.getInt(2);

                if(!dungeon.isChoiceValid(choice)) this.response = "Invalid choice!";
                else
                {
                    dungeon.submitChoice(this.getInt(2));

                    this.embed = null;
                }
            }
        }
        else this.response = INVALID;

        return this;
    }
}
