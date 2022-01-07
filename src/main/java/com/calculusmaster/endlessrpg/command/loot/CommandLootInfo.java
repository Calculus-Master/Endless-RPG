package com.calculusmaster.endlessrpg.command.loot;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.character.RPGElementalContainer;
import com.calculusmaster.endlessrpg.gameplay.enums.ElementType;
import com.calculusmaster.endlessrpg.gameplay.enums.EquipmentType;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandLootInfo extends Command
{
    public CommandLootInfo(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        boolean info = this.msg.length == 2 && (this.isNumeric(1) || EquipmentType.parse(this.msg[1]) != null);

        if(info)
        {
            RPGCharacter active = this.playerData.getActiveCharacter();
            LootItem loot = null;

            if(this.isNumeric(1))
            {
                int lootIndex = this.getInt(1) - 1;

                if(lootIndex < 0 || lootIndex >= active.getLoot().size())
                {
                    this.response = "Invalid loot index!";
                    return this;
                }
                else loot = LootItem.build(active.getLoot().get(lootIndex));

                this.embed.setFooter((lootIndex + 1) + " / " + active.getLoot().size());
            }
            else if(EquipmentType.parse(this.msg[1]) != null)
            {
                loot = active.getEquipment().getLoot(EquipmentType.parse(this.msg[1]));

                if(loot.isEmpty())
                {
                    this.response = "Your character has nothing equipped in that slot!";
                    return this;
                }
            }

            if(loot == null || loot.isEmpty())
            {
                this.response = "An error has occurred trying to display Loot Information.";
                return this;
            }

            this.embed
                    .addField("Type", Global.normalize(loot.getLootType().toString()), true)
                    .addField("Equipment Slot", loot.getLootType().isArmor() ? Global.normalize(loot.getLootType().getCore().toString()) : "Left/Right Hand", true)
                    .addBlankField(true)
                    .addField("Requirements", loot.getRequirements().getOverview(), false)
                    .addField(this.getStatModifierField(loot))
                    .addField(this.getElementalModifierField("Elemental Damage Modifiers", loot.getElementalDamage()))
                    .addField(this.getElementalModifierField("Elemental Defense Modifiers", loot.getElementalDefense()));

            this.embed.setTitle(loot.getName() + loot.getTagOverview());
        }
        else this.response = INVALID;

        return this;
    }

    private MessageEmbed.Field getStatModifierField(LootItem loot)
    {
        StringBuilder stats = new StringBuilder();
        for(Stat s : Stat.values()) if(loot.getBoosts().get(s) != 0) stats.append(Global.normalize(s.toString())).append(": ").append(this.formatNumber(loot.getBoosts().get(s))).append("\n");

        if(!stats.isEmpty()) stats.deleteCharAt(stats.length() - 1);
        else stats.append("None");

        return new MessageEmbed.Field("Stat Modifiers", stats.toString(), false);
    }

    private MessageEmbed.Field getElementalModifierField(String name, RPGElementalContainer container)
    {
        StringBuilder elements = new StringBuilder();
        for(ElementType e : ElementType.values()) if(container.get(e) != 0) elements.append(e.getIcon().getAsMention()).append(": ").append(this.formatNumber(container.get(e))).append("\n");

        if(!elements.isEmpty()) elements.deleteCharAt(elements.length() - 1);
        else elements.append("None");

        return new MessageEmbed.Field(name, elements.toString(), false);
    }

    private String formatNumber(int number)
    {
        return number > 0 ? "+" + number : "" + number;
    }
}
