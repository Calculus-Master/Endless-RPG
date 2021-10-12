package com.calculusmaster.endlessrpg.command;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
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

            int move = this.getInt(1);
            int target = this.getInt(2) - 1;

            if(target < 0 || target > b.getBattlers().length || b.getBattlers()[target].getCharacterID().equals(b.getCurrentCharacter().getCharacterID())) this.response = "Invalid target!";
            //Spell system for move detection else if(move > Battle.getCurrentCharacter().moves().size() or something)
            else
            {
                //Temporary, move = 1 for basic attack
                move = 1;

                if(move == 1) b.submitTurn(target);

                this.embed = null;
            }
        }
        else this.response = INVALID;

        return this;
    }
}
