package com.calculusmaster.endlessrpg.command.activity;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.tasks.Quest;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class CommandQuests extends Command
{
    public CommandQuests(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        List<Quest> quests = this.playerData.getQuests();

        this.embed.setTitle(this.player.getName() + "'s Quests");

        if(quests.isEmpty()) this.embed.setDescription("You have no active Quests!");
        else
        {
            StringBuilder s = new StringBuilder();

            for(int i = 0; i < quests.size(); i++)
            {
                Quest q = quests.get(i);

                s
                        .append(i + 1).append(": ")
                        .append(q.getName())
                        .append(" | Tasks: ").append(q.getTasks().size())
                        .append(" | Level ").append(q.getLevel())
                        .append(" | Type: ").append(Global.normalize(q.getType().toString()))
                        .append("\n");
            }

            this.embed.setDescription(s.toString());
        }

        return this;
    }
}
