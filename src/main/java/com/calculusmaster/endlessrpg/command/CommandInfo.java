package com.calculusmaster.endlessrpg.command;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.EquipmentType;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandInfo extends Command
{
    public CommandInfo(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        RPGCharacter c = this.playerData.getActiveCharacter();

        this.embed
                .addField("Class", Global.normalize(c.getRPGClass().toString()), true)
                .addField("Experience", "**Level " + c.getLevel() + "**\n" + c.getExp() + " / " + c.getExpRequired(c.getLevel() + 1) + " XP", true)
                .addBlankField(true)
                .addField(this.getEquipmentField())
                .addField(this.getStatTitleField())
                .addField(this.getStatCoreField(c))
                .addField(this.getStatEffectiveField(c));

        this.embed.setTitle(c.getName() + " Info");

        return this;
    }

    private MessageEmbed.Field getEquipmentField()
    {
        StringBuilder content = new StringBuilder();
        RPGCharacter c = this.playerData.getActiveCharacter();

        for(EquipmentType e : EquipmentType.values())
        {
            LootItem loot = c.getEquipment().getEquipmentLoot(e);

            content.append("`").append(e.getStyledName()).append("`: ");

            if(loot.isEmpty()) content.append("None\n");
            else
            {
                content
                        .append(loot.getName())
                        .append("(").append(loot.getLootType().toString())
                        .append("), Boosts: ").append(loot.getBoosts()).append("\n");
            }
        }

        content.deleteCharAt(content.length() - 1);
        return new MessageEmbed.Field("Equipment", content.toString(), false);
    }

    private MessageEmbed.Field getStatTitleField()
    {
        StringBuilder content = new StringBuilder();
        for(Stat s : Stat.values()) content.append(Global.normalize(s.toString())).append("\n");
        content.deleteCharAt(content.length() - 1);
        return new MessageEmbed.Field("Stats", content.toString(), true);
    }

    private MessageEmbed.Field getStatCoreField(RPGCharacter c)
    {
        StringBuilder content = new StringBuilder();
        for(Stat s : Stat.values()) content.append(c.getCoreStat(s)).append("\n");
        content.deleteCharAt(content.length() - 1);
        return new MessageEmbed.Field("Core", content.toString(), true);
    }

    private MessageEmbed.Field getStatEffectiveField(RPGCharacter c)
    {
        StringBuilder content = new StringBuilder();
        for(Stat s : Stat.values()) content.append(c.getStat(s)).append("\n");
        content.deleteCharAt(content.length() - 1);
        return new MessageEmbed.Field("Effective", content.toString(), true);
    }
}
