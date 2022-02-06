package com.calculusmaster.endlessrpg.command.spell;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.spell.Spell;
import com.calculusmaster.endlessrpg.gameplay.spell.SpellData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class CommandSpells extends Command
{
    public static final Map<String, List<String>> SPELL_REQUESTS = new HashMap<>();

    public CommandSpells(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        RPGCharacter active = this.playerData.getActiveCharacter();

        StringJoiner current = new StringJoiner("\n");
        for(int i = 0; i < active.getSpells().size(); i++)
        {
            Spell s = active.getSpell(i);
            current.add((i + 1) + ": " + s.getName() + " - " + s.getDescription());
        }

        StringJoiner learn = new StringJoiner("\n");

        learn.add(active.getName() + " will be able to learn new spells at Level " + active.getNextSpellLevel() + "!");

        if(active.getNextSpellLevel() <= active.getLevel())
        {
            learn = new StringJoiner("\n");

            if(!SPELL_REQUESTS.containsKey(active.getCharacterID())) SPELL_REQUESTS.put(active.getCharacterID(), active.generateNextAvailableSpells());

            List<SpellData> spells = SPELL_REQUESTS.get(active.getCharacterID()).stream().map(SpellData::dataFromID).toList();

            learn.add("*You are able to learn a new Spell! Use p!learn <number>, where <number> is the number of the Spell in the list below.*\n");

            for(int i = 0; i < spells.size(); i++)
            {
                Spell s = spells.get(i).getInstance();
                learn.add((i + 1) + ": " + s.getName() + " – " + s.getDescription());
            }
        }

        this.embed
                .setTitle(active.getName() + "'s Active Spells")
                .setDescription(current.toString())
                .addField("New Spells", learn.toString(), false);

        return this;
    }
}
