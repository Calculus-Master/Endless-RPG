package com.calculusmaster.endlessrpg.util;

import com.calculusmaster.endlessrpg.command.core.CommandHandler;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;
import com.calculusmaster.endlessrpg.mongo.ServerDataQuery;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class Listener extends ListenerAdapter
{
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        //Check if the message was sent by a bot, and skip the listener if true
        if(event.getAuthor().isBot()) return;

        //If a link is sent, skip the listener
        if(event.getMessage().getContentRaw().toLowerCase().startsWith("http")) return;

        //If any attachment is sent, skip the listener
        if(event.getMessage().getAttachments().size() > 0) return;

        //Ignore DMs (temporary?)
        if(!event.isFromGuild()) return;

        //If the Realm is currently changing
        if(Realm.LOCKDOWN)
        {
            event.getMessage().reply("The world is fading...The tides are shifting...You are being whisked away to a new land...\n||The Realm is currently updating! You are unable to use any commands. Check back in a few minutes to continue your adventure!||").queue();
            return;
        }

        Guild server = event.getGuild();
        String msg = event.getMessage().getContentRaw().trim();

        String prefix = new ServerDataQuery(server.getId()).getPrefix();

        if(msg.split("\\s+")[0].toLowerCase().startsWith(prefix.toLowerCase()))
        {
            msg = msg.substring(prefix.length());

            if(!msg.startsWith("create") && !PlayerDataQuery.isRegistered(event.getAuthor().getId())) event.getChannel().sendMessage("<@" + event.getAuthor().getId() + ">: You need to create a Character first!").queue();
            else CommandHandler.parse(event, msg);
        }
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event)
    {
        ServerDataQuery.register(event.getGuild().getId());
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event)
    {
        ServerDataQuery.deregister(event.getGuild().getId());
    }
}
