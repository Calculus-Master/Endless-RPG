package com.calculusmaster.endlessrpg.command.loot;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.ElementType;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Random;

public class CommandDelete extends Command
{
    public CommandDelete(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        boolean delete = this.msg.length == 2 && this.msg[1].equals("delete") && this.isNumeric(1);
        boolean salvage = this.msg.length == 2 && this.msg[1].equals("salvage") && this.isNumeric(1); //TODO: NYI
        //TODO: Loot Salvaging (Connected with Loot Crafting System)

        if(delete)
        {
            RPGCharacter active = this.playerData.getActiveCharacter();
            int lootIndex = this.getInt(1) - 1;

            if(lootIndex < 0 || lootIndex >= active.getLoot().size()) this.response = "Invalid loot index!";
            else
            {
                LootItem loot = LootItem.build(active.getLoot().get(lootIndex));

                //TODO: When transfering loot between characters, make sure to unequip
                active.getEquipment().remove(loot.getLootID());
                active.updateEquipment();

                int goldReward = (new Random().nextInt(5) + 1) * loot.getBoosts().values().stream().mapToInt(x -> x).sum() + Arrays.stream(ElementType.values()).mapToInt(e -> loot.getElementalDamage().get(e) + loot.getElementalDefense().get(e)).sum();

                active.addGold(goldReward);
                active.removeLoot(loot.getLootID());
                active.updateLoot();

                LootItem.delete(loot.getLootID());

                this.response = "Deleted \"" + loot.getName() + "\"! You earned `" + goldReward + " Gold`!";
            }
        }
        else this.response = INVALID;

        return this;
    }
}
