package com.calculusmaster.endlessrpg.command.activity;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.spells.Spell;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Objects;

public class CommandAttack extends Command
{
    public CommandAttack(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        //r!attack <move> <target>
        boolean attack = this.msg.length == 3 && this.isNumeric(1) && this.isNumeric(2);

        if(!Battle.isInBattle(this.player.getId())) this.response = "You are not in a battle!";
        else if(attack)
        {
            Battle b = Objects.requireNonNull(Battle.instance(this.player.getId()));
            RPGCharacter active = this.playerData.getActiveCharacter();

            int move = this.getInt(1);
            int target = this.getInt(2) - 1;

            if(!b.getCurrentCharacter().isOwnedBy(this.player.getId())) this.response = "It isn't your turn!";
            else if(target < 0 || target > b.getBattlers().length || b.getBattlers()[target].getCharacterID().equals(b.getCurrentCharacter().getCharacterID())) this.response = "Invalid target!";
            else if(move < 1 || move > active.getSpells().size())  this.response = "Invalid spell index!";
            else
            {
                Spell chosenSpell = active.getSpell(move - 1);

                b.submitTurn(target, chosenSpell);

                this.embed = null;
            }
        }
        else this.response = INVALID;

        return this;
    }
}
