package com.calculusmaster.endlessrpg.command.spell;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.command.core.CommandHandler;
import com.calculusmaster.endlessrpg.command.core.CommandInvalid;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.spell.SpellData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class CommandLearn extends Command
{
    public CommandLearn(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        RPGCharacter active = this.playerData.getActiveCharacter();

        boolean learn = this.msg.length == 2 && this.isNumeric(1);

        if(learn)
        {
            if(!CommandSpells.SPELL_REQUESTS.containsKey(active.getCharacterID()) && active.getLevel() >= active.getNextSpellLevel())
            {
                CommandHandler.parse(this.event, "spells");
                this.embed = null;
            }
            else if(!CommandSpells.SPELL_REQUESTS.containsKey(active.getCharacterID())) this.response = active.getName() + " will be able to learn new spells at Level " + active.getNextSpellLevel() + "!";
            else
            {
                List<String> spells = CommandSpells.SPELL_REQUESTS.get(active.getCharacterID());
                int index = this.getInt(1) - 1;

                if(index < 0 || index > spells.size()) this.response = "Invalid spell index!";
                else
                {
                    SpellData spell = SpellData.dataFromID(spells.get(index));
                    active.addSpell(spell.getID());
                    active.updateSpells();

                    CommandSpells.SPELL_REQUESTS.remove(active.getCharacterID());

                    this.response = active.getName() + " learned `" + spell.getInstance().getName() + "`!";
                }
            }
        }
        else this.response = CommandInvalid.INVALID;

        return this;
    }
}
