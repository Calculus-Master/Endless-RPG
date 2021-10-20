package com.calculusmaster.endlessrpg.command.activity;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Objects;

public class CommandBattle extends Command
{
    public CommandBattle(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        boolean pvp = this.msg.length == 2 && this.getMentions().size() > 0;
        boolean pve = this.msg.length == 2 && this.msg[1].equals("ai");
        boolean info = this.msg.length == 3 && this.msg[1].equals("info") && this.isNumeric(2);

        if(info)
        {
            if(!Battle.isInBattle(this.player.getId())) this.response = "You are not in a battle!";
            else
            {
                Battle b = Objects.requireNonNull(Battle.instance(this.player.getId()));
                int target = this.getInt(2) - 1;

                if(target < 0 || target >= b.getBattlers().length) this.response = "Invalid target!";
                else this.embed
                        .setTitle("Information for " + b.getBattlers()[target].getName())
                        .setDescription(b.getBattlers()[target].getSummary());
            }
        }
        else if(pvp)
        {
            String otherID = this.getMentions().get(0).getId();

            if(otherID.equals(this.player.getId())) this.response = "You can't battle yourself!";
            else if(Battle.isInBattle(this.player.getId())) this.response = "You are already in a battle! Complete it to start another battle!";
            else if(Battle.isInBattle(otherID)) this.response = this.getMentions().get(0).getNickname() + " is already in a battle! They must complete it before joining another battle!";
            else if(!PlayerDataQuery.isRegistered(otherID)) this.response = this.getMentions().get(0).getNickname() + " does not have any characters to battle with!";
            else
            {
                Battle b = Battle.createPVP(this.player.getId(), otherID);
                b.setEvent(this.event);

                b.sendTurnEmbed();
                this.embed = null;
            }
        }
        else if(pve)
        {
            if(Battle.isInBattle(this.player.getId())) this.response = "You are already in a battle! Complete it to start another battle!";
            else
            {
                Battle b = Battle.createPVE(this.player.getId(), Realm.CURRENT.getLocation(this.playerData.getLocationID()));
                b.setEvent(this.event);

                b.sendTurnEmbed();
                this.embed = null;
            }
        }
        else this.response = INVALID;

        return this;
    }
}
