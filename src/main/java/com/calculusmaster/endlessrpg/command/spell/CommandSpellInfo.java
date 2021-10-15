package com.calculusmaster.endlessrpg.command.spell;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.spell.Spell;
import com.calculusmaster.endlessrpg.gameplay.spell.SpellData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandSpellInfo extends Command
{
    public CommandSpellInfo(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        boolean info = this.msg.length >= 2;

        if(info)
        {
            SpellData data = SpellData.dataFromID(this.msg[1]) != null ? SpellData.dataFromID(this.msg[1]) : SpellData.dataFromName(this.msgMultiWordContent(1));

            if(data == null) this.response = "Invalid Spell ID or Spell Name!";
            else
            {
                Spell spell = data.getInstance();

                this.embed
                        .setTitle(spell.getName() + " Info")
                        .setDescription(spell.getDescription())
                        .addField("Requirements", data.getRequirements().getOverview(), false)
                        .setFooter("ID: " + data.getID());
            }
        }
        else this.response = INVALID;

        return this;
    }
}
