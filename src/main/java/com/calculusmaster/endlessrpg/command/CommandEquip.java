package com.calculusmaster.endlessrpg.command;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
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
        boolean equip = this.msg.length >= 2 && this.isNumeric(1);

        if(Battle.isInBattle(this.player.getId())) this.response = "You cannot equip loot while in a battle!";
        else if(equip)
        {
            int target = this.getInt(1);

            if(target < 1 || target > this.playerData.getLoot().size()) this.response = "Invalid index!";
            else
            {
                LootItem loot = LootItem.build(this.playerData.getLoot().get(target - 1));

                EquipmentType slot;
                if(this.msg.length == 3) slot = switch(this.msg[2]) {
                    case "helmet", "helm", "h" -> EquipmentType.HELMET;
                    case "chestplate", "chest", "c" -> EquipmentType.CHESTPLATE;
                    case "gauntlets", "gloves", "g" -> EquipmentType.GAUNTLETS;
                    case "leggings", "legs", "l" -> EquipmentType.LEGGINGS;
                    case "boots", "b" -> EquipmentType.BOOTS;
                    case "left_hand", "lh", "left" -> EquipmentType.LEFT_HAND;
                    case "right_hand", "rh", "right" -> EquipmentType.RIGHT_HAND;
                    default -> null;
                };
                else if(!loot.getLootType().isArmor())
                {
                    this.response = "Cannot infer loot slot! Please specify Left Hand or Right Hand";
                    return this;
                }
                else slot = switch(loot.getLootType().getCore()) {
                    case HELMET -> EquipmentType.HELMET;
                    case CHESTPLATE -> EquipmentType.CHESTPLATE;
                    case GAUNTLETS -> EquipmentType.GAUNTLETS;
                    case LEGGINGS -> EquipmentType.LEGGINGS;
                    case BOOTS -> EquipmentType.BOOTS;
                    default -> throw new IllegalStateException("Cannot infer loot type destination for " + loot.getLootType().getCore() + "!");
                };

                if(slot == null) this.response = "Invalid slot!";
                else if(!slot.isValidLoot(loot.getLootType())) this.response = "Loot of type `" + loot.getLootType() + "` cannot be equipped to your Character's " + slot.getStyledName() + " Equipment Slot!";
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
