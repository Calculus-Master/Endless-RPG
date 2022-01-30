package com.calculusmaster.endlessrpg.command.activity;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.Dungeon;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.util.Coordinate;
import com.calculusmaster.endlessrpg.gameplay.battle.dungeon.util.Direction;
import com.calculusmaster.endlessrpg.gameplay.enums.LocationType;
import com.calculusmaster.endlessrpg.gameplay.world.Location;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CommandDungeon extends Command
{
    public CommandDungeon(MessageReceivedEvent event, String msg)
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
            if(Dungeon.isInDungeon(this.player.getId())) return this.invalid("You are already in a dungeon!");
            else if(Dungeon.isOnCooldown(this.player.getId())) return this.invalid("You cannot enter a Dungeon for another " + Dungeon.getDungeonCooldown(this.player.getId()));

            Location location = Realm.CURRENT.getLocation(this.playerData.getLocationID());

            if(!location.getType().equals(LocationType.DUNGEON) && !location.getType().equals(LocationType.FINAL_KINGDOM)) return this.invalid("This location does not have a dungeon!");

            List<Member> invalidMembers = new ArrayList<>();
            for(Member m : this.getMentions()) if(Dungeon.isInDungeon(m.getId()) || Dungeon.isOnCooldown(m.getId()) || !PlayerDataQuery.isRegistered(m.getId())) invalidMembers.add(m);

            if(!invalidMembers.isEmpty()) this.playerData.DM(invalidMembers.size() + " of the allies you invited could not join your Dungeon! (This means that they are either already in a Dungeon, or on Dungeon cooldown. Only the remaining " + (this.getMentions().size() - invalidMembers.size()) + " will be invited.");
            List<PlayerDataQuery> others = this.getMentions().stream().filter(m -> !invalidMembers.contains(m)).map(ISnowflake::getId).map(PlayerDataQuery::new).toList();

            Dungeon dungeon = location.getType().equals(LocationType.FINAL_KINGDOM)
                    ? Dungeon.createFinalKingdom(this.event, this.playerData, others)
                    : Dungeon.create(location, this.event, this.playerData, others);

            if(others.isEmpty())
            {
                this.embed = null;

                dungeon.sendEmbed(Dungeon.DungeonEmbed.START);

                dungeon.start();
            }
            else
            {
                this.response = "You await your allies... \n*(Dungeon Level: %s, Total Players: %s)*".formatted(dungeon.getLevel(), dungeon.getPlayers().size());

                others.forEach(p -> p.DM("**" + this.player.getName() + "** has invited you to join them in conquering `" + location.getName() + "`!\nTo join, use `r!dungeon accept`. To deny their request, use `r!dungeon deny`. *Note: If you deny the request you will not be able to join the Dungeon again!*"));
            }
        }
        else if(accept || deny || cancel)
        {
            Dungeon dungeon = Dungeon.instance(this.player.getId());

            if(dungeon == null) this.response = "You are not in a Dungeon!";
            else if((accept || deny) && dungeon.getLeader().data.getID().equals(this.player.getId())) this.response = "You are the leader! You cannot accept or deny a Dungeon request.";
            else if(cancel && !dungeon.getLeader().data.getID().equals(this.player.getId())) this.response = "Only the leader can cancel the Dungeon adventure!";
            else if((accept || deny) && !dungeon.getStatus().equals(Dungeon.DungeonStatus.WAITING_FOR_PLAYERS)) this.response = "The Dungeon has already started!";
            else if(cancel)
            {
                Dungeon.delete(this.player.getId());

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
            if(!Dungeon.isInDungeon(this.player.getId())) this.response = "You are not in a Dungeon!";
            else if(!Dungeon.instance(this.player.getId()).getStatus().equals(Dungeon.DungeonStatus.ADVENTURING)) this.response = "You have not entered the Dungeon yet (waiting for allies)!";
            else
            {
                Dungeon.instance(this.player.getId()).sendEmbed(Dungeon.DungeonEmbed.MAP_EXPLORED);

                this.embed = null;
            }
        }
        else if(move)
        {
            Dungeon dungeon = Dungeon.instance(this.player.getId());

            if(dungeon == null) this.response = "You are not in a Dungeon!";
            else if(!dungeon.getLeader().data.getID().equals(this.player.getId())) this.response = "Only the Dungeon Leader is able to move your group through the Dungeon!";
            else if(!dungeon.getStatus().equals(Dungeon.DungeonStatus.ADVENTURING)) this.response = "You have not entered the Dungeon yet (waiting for allies)!";
            else if(!dungeon.current().isComplete()) this.response = "You can only move after the current encounter has been completed!";
            else
            {
                Direction dir = Direction.cast(this.msg[2]);
                Coordinate target = dungeon.getPosition().shift(dir);

                if(!dungeon.isValidLocation(target)) this.response = "You cannot move in that direction!";
                else
                {
                    Executors.newSingleThreadScheduledExecutor().schedule(() -> dungeon.move(dir), 3, TimeUnit.SECONDS);

                    this.response = "Successfully moved " + dir.toString().toLowerCase() + "! What awaits you?";
                }
            }
        }
        else if(interact)
        {
            Dungeon dungeon = Dungeon.instance(this.player.getId());

            if(dungeon == null) this.response = "You are not in a Dungeon!";
            else if(!dungeon.getLeader().data.getID().equals(this.player.getId())) this.response = "Only the Dungeon Leader is able to move your group through the Dungeon!";
            else if(!dungeon.getStatus().equals(Dungeon.DungeonStatus.ADVENTURING)) this.response = "You have not entered the Dungeon yet (waiting for allies)!";
            else if(!dungeon.hasTag(Dungeon.DungeonMetaTag.AWAITING_INTERACTION)) this.response = "There's nothing to interact with!";
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
