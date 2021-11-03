package com.calculusmaster.endlessrpg.command.economy;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.command.loot.CommandInventory;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandBank extends Command
{
    public CommandBank(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        if(!Realm.CURRENT.getLocation(this.playerData.getLocationID()).getType().isTown())
        {
            this.response = "You can only access your Bank in a Town or Hub!";
            return this;
        }

        int page = 10;
        int start = this.msg.length == 2 && this.isNumeric(1) ? (this.getInt(1) - 1) * page : 0;
        int end = Math.min(start + page, this.playerData.getLoot().size());

        String loot = CommandInventory.createLootListInfo(this.playerData.getLoot(), start, end);
        //TODO: Resources

        this.embed
                .setTitle(this.player.getName() + "'s Bank")
                .addField("Gold", "**" + this.playerData.getGold() + "**", false)
                .addField("Loot", loot, false)
                .setFooter("Deposit or Withdraw Gold, Loot and Resources using r!deposit or r!withdraw!");

        return this;
    }
}
