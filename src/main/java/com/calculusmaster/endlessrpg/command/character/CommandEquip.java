package com.calculusmaster.endlessrpg.command.character;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.EquipmentType;
import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

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
            RPGCharacter active = this.playerData.getActiveCharacter();

            if(target < 1 || target > this.playerData.getLoot().size()) this.response = "Invalid index!";
            else
            {
                LootItem loot = LootItem.build(this.playerData.getLoot().get(target - 1));

                EquipmentType slot;
                if(this.msg.length == 3) slot = EquipmentType.parse(this.msg[2]);
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
                else if(loot.getRequirements().getLevel() > active.getLevel()) this.response = "Your character needs to be Level " + loot.getRequirements().getLevel() + " to equip this item!";
                else if(this.playerData.getCharacterList().stream().map(RPGCharacter::build).anyMatch(c -> c.getEquipment().asList().contains(loot.getLootID()))) this.response = "Another one of your characters already has that Loot equipped!";
                else if(!loot.getRequirements().check(active)) this.response = active.getName() + " doesn't meet the requirements to equip this Loot item! Requirements: \n" + loot.getRequirements().getOverview();
                else
                {
                    //Basic Sword and Wand cannot be in both hands at once
                    if(slot.isHand() && loot.getLootType().isWeapon())
                    {
                        LootType check = loot.getLootType();
                        List<LootType> singleHandLootTypes = Arrays.asList(LootType.SWORD, LootType.WAND);

                        if(singleHandLootTypes.contains(check))
                        {
                            LootType right = active.getEquipment().getEquipmentLoot(EquipmentType.RIGHT_HAND).getLootType();
                            LootType left = active.getEquipment().getEquipmentLoot(EquipmentType.LEFT_HAND).getLootType();

                            boolean leftInvalid = slot.equals(EquipmentType.LEFT_HAND) && singleHandLootTypes.contains(right);
                            boolean rightInvalid = slot.equals(EquipmentType.RIGHT_HAND) && singleHandLootTypes.contains(left);

                            if(leftInvalid || rightInvalid)
                            {
                                this.response = "You cannot equip two basic %s loot items at the same time!".formatted(Global.normalize(check.toString()));
                                return this;
                            }
                        }
                    }

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
