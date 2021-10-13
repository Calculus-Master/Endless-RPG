package com.calculusmaster.endlessrpg.command.loot;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGElementalContainer;
import com.calculusmaster.endlessrpg.gameplay.enums.ElementType;
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
        boolean info = this.msg.length == 2 && this.isNumeric(1);

        if(info)
        {
            int lootIndex = this.getInt(1) - 1;

            if(lootIndex < 0 || lootIndex >= this.playerData.getLoot().size()) this.response = "Invalid loot index!";
            else
            {
                LootItem loot = LootItem.build(this.playerData.getLoot().get(lootIndex));

                this.embed
                        .addField("Type", Global.normalize(loot.getLootType().toString()), true)
                        .addField("Equipment Slot", loot.getLootType().isArmor() ? Global.normalize(loot.getLootType().getCore().toString()) : "Left/Right Hand", true)
                        .addField("Requirements", "Level: " + loot.getRequiredLevel(), true) //TODO: Add more requirements (fully fledged - defense, elements, etc)
                        .addField(this.getStatModifierField(loot))
                        .addField(this.getElementalModifierField("Elemental Damage Modifiers", loot.getElementalDamage()))
                        .addField(this.getElementalModifierField("Elemental Defense Modifiers", loot.getElementalDefense()));

                this.embed
                        .setTitle(loot.getName())
                        .setFooter((lootIndex + 1) + " / " + this.playerData.getLoot().size());
            }
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
        for(ElementType e : ElementType.values()) if(container.getRaw(e) != 0) elements.append(e.getIcon().getAsMention()).append(": ").append(this.formatNumber(container.getRaw(e))).append("\n");

        if(!elements.isEmpty()) elements.deleteCharAt(elements.length() - 1);
        else elements.append("None");

        return new MessageEmbed.Field(name, elements.toString(), false);
    }

    private String formatNumber(int number)
    {
        return number > 0 ? "+" + number : "" + number;
    }
}