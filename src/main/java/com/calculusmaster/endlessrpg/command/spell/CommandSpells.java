package com.calculusmaster.endlessrpg.command.spell;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.spell.Spell;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandSpells extends Command
{
    public CommandSpells(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        RPGCharacter active = this.playerData.getActiveCharacter();

        StringBuilder current = new StringBuilder();
        for(int i = 0; i < active.getSpells().size(); i++)
        {
            Spell s = active.getSpell(i);
            current.append(i + 1).append(": ").append(s.getName()).append(" - ").append(s.getDescription()).append("\n");
        }
        current.deleteCharAt(current.length() - 1);

        this.embed.addField("Active Spells", current.toString(), false);

        StringBuilder available = new StringBuilder();
        for(int i = 0; i < active.availableSpells().size(); i++)
        {
            Spell s = active.availableSpells().get(i).getInstance();
            if(active.getSpells().stream().noneMatch(spell -> spell.getName().equals(s.getName()))) available.append("*").append(s.getName()).append("* - ").append(s.getDescription()).append("\n");
        }
        if(!available.isEmpty()) available.deleteCharAt(available.length() - 1);
        else available.append("None");

        this.embed.addField("Available Spells", available.toString(), false);

        return this;
    }
}
