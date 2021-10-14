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

        if(delete)
        {
            int lootIndex = this.getInt(1) - 1;

            if(lootIndex < 0 || lootIndex >= this.playerData.getLoot().size()) this.response = "Invalid loot index!";
            else
            {
                LootItem loot = LootItem.build(this.playerData.getLoot().get(lootIndex));

                for(String c : this.playerData.getCharacterList())
                {
                    RPGCharacter character = RPGCharacter.build(c);
                    character.getEquipment().remove(loot.getLootID());
                    character.updateEquipment();
                }

                int goldReward = (new Random().nextInt(5) + 1) * loot.getBoosts().values().stream().mapToInt(x -> x).sum() + Arrays.stream(ElementType.values()).mapToInt(e -> loot.getElementalDamage().getRaw(e) + loot.getElementalDefense().getRaw(e)).sum();
                this.playerData.addGold(goldReward);

                LootItem.delete(loot.getLootID());

                this.response = "Deleted \"" + loot.getName() + "\"! You earned `" + goldReward + " Gold`! Loot Item was also unequipped from any characters that were using it.";
            }
        }
        else this.response = INVALID;

        return this;
    }
}
