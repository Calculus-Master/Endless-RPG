package com.calculusmaster.endlessrpg.command.economy;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandBalance extends Command
{
    public CommandBalance(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        RPGCharacter active = this.playerData.getActiveCharacter();

        this.embed
                .setTitle(this.player.getName() + "'s Balance")
                .addField(active.getName() + "'s Balance", "**" + active.getGold() + "** Gold!", false)
                .addField("Your Balance", "**" + this.playerData.getGold() + "** Gold!", false)
                .setFooter("Deposit Gold into your Player Bank at Towns using r!deposit.");

        return this;
    }
}
