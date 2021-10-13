package com.calculusmaster.endlessrpg.command.character;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.EquipmentType;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandRemove extends Command
{
    public CommandRemove(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        boolean remove = this.msg.length == 2;

        if(remove)
        {
            EquipmentType slot = EquipmentType.parse(this.msg[1]);
            RPGCharacter active = this.playerData.getActiveCharacter();

            if(active.getEquipment().getEquipmentID(slot).equals(LootItem.EMPTY.getLootID())) this.response = "Your active character does not have anything equipped in that slot!";
            else
            {
                active.equipLoot(slot, LootItem.EMPTY.getLootID());
                active.updateEquipment();

                this.response = "Unequipped the Loot in your active character's `" + Global.normalize(slot.toString().replaceAll("_", " ")) + "` slot!";
            }
        }
        else this.response = INVALID;

        return this;
    }
}
