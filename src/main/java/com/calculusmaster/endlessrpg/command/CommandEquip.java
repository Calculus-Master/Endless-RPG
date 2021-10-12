package com.calculusmaster.endlessrpg.command;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.EquipmentType;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandEquip extends Command
{
    public CommandEquip(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        boolean equip = this.msg.length == 3 && this.isNumeric(1) && EquipmentType.cast(this.msg[2]) != null;

        if(equip)
        {
            int target = this.getInt(1);

            if(target < 1 || target > this.playerData.getLoot().size()) this.response = "Invalid index!";
            else
            {
                LootItem loot = LootItem.build(this.playerData.getLoot().get(target - 1));
                EquipmentType slot = EquipmentType.cast(this.msg[2]);

                if(!slot.isValidLoot(loot.getLootType())) this.response = "Loot of type `" + loot.getLootType() + "` cannot be equipped to your Character's " + slot.getStyledName() + " Equipment Slot!";
                else if(loot.getRequiredLevel() > this.playerData.getActiveCharacter().getLevel()) this.response = "Your character needs to be Level " + loot.getRequiredLevel() + " to equip this item!";
                else
                {
                    RPGCharacter active = this.playerData.getActiveCharacter();
                    active.equipLoot(slot, loot.getLootID());
                    active.updateEquipment();

                    this.response = "Equipped `" + loot.getName() + "` to your Character's " + slot.getStyledName() + " Equipment Slot!";
                }
            }
        }
        else this.response = INVALID;

        return this;
    }
}
