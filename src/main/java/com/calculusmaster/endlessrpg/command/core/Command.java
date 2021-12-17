package com.calculusmaster.endlessrpg.command.core;

import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;
import com.calculusmaster.endlessrpg.mongo.ServerDataQuery;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public abstract class Command
{
    protected MessageReceivedEvent event;

    protected EmbedBuilder embed;
    protected String response;

    protected String[] raw;
    protected String[] msg;

    protected User player;
    protected Guild server;

    protected PlayerDataQuery playerData;
    protected ServerDataQuery serverData;

    public Command(MessageReceivedEvent event, String msg)
    {
        this.event = event;

        this.embed = new EmbedBuilder();
        this.response = "";

        this.raw = msg.split("\\s+");
        this.msg = msg.toLowerCase().split("\\s+");

        this.player = event.getAuthor();
        this.server = event.getGuild();

        this.playerData = new PlayerDataQuery(this.player.getId());
        this.serverData = new ServerDataQuery(this.server.getId());
    }

    //Responses
    protected static final String INVALID = "Invalid Command!";

    //For Commands
    protected Command invalid(String msg)
    {
        this.response = msg;
        return this;
    }

    protected boolean isNumeric(int index)
    {
        return this.msg[index].chars().allMatch(Character::isDigit);
    }

    protected boolean isNumericAll(int startIndex)
    {
        for(int i = startIndex; i < this.msg.length; i++) if(!this.isNumeric(i)) return false;
        return true;
    }

    protected int getInt(int index)
    {
        return Integer.parseInt(this.msg[index]);
    }

    protected String rawMultiWordContent(int start)
    {
        return this.multiWordContent(this.raw, start);
    }

    protected String msgMultiWordContent(int start)
    {
        return this.multiWordContent(this.msg, start);
    }

    private String multiWordContent(String[] array, int start)
    {
        StringBuilder s = new StringBuilder();
        for(int i = start; i < array.length; i++) s.append(array[i]).append(" ");
        return s.toString().trim();
    }

    protected List<Member> getMentions()
    {
        return this.event.getMessage().getMentionedMembers();
    }

    protected void send(String content)
    {
        this.event.getChannel().sendMessage(content).queue();
    }

    public abstract Command run();

    public void send()
    {
        if(this.response.isEmpty() && this.embed == null) return;

        if(!this.response.isEmpty()) this.event.getChannel().sendMessage(this.playerData.getMention() + ": " + this.response).queue();
        else this.event.getChannel().sendMessageEmbeds(this.buildResponseEmbed()).queue();
    }

    private MessageEmbed buildResponseEmbed()
    {
        this.embed.setAuthor("EndlessRPG");

        return this.embed.build();
    }
}
