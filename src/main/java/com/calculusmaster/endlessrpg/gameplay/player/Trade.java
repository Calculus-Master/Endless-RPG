package com.calculusmaster.endlessrpg.gameplay.player;

import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Trade
{
    public static final List<Trade> TRADES = new ArrayList<>();

    private PlayerDataQuery[] players;
    private TradeOffer[] offers;
    private boolean[] confirms;

    private TradeStatus status;

    private MessageReceivedEvent sourceEvent;
    private Message output;

    public static Trade create(String player1, String player2, MessageReceivedEvent event)
    {
        Trade t = new Trade();

        t.setPlayers(player1, player2);
        t.setSourceEvent(event);
        t.setStatus(TradeStatus.WAITING);

        TRADES.add(t);
        return t;
    }

    //Embeds
    public void sendStartEmbed()
    {
        this.setup();

        EmbedBuilder embed = new EmbedBuilder();

        embed
                .setTitle("Trade Between " + this.players[0].getUsername() + " and " + this.players[1].getUsername())
                .addField(this.players[0].getUsername() + "'s Offer", this.offers[0].getOverview(), false)
                .addField(this.players[1].getUsername() + "'s Offer", this.offers[1].getOverview(), false);

        this.sourceEvent.getChannel().sendMessageEmbeds(embed.build()).queue(m -> this.output = m);
    }

    public void sendTradeUpdateEmbed()
    {
        EmbedBuilder embed = new EmbedBuilder();

        embed
                .setTitle("Trade Between " + this.players[0].getUsername() + " and " + this.players[1].getUsername())
                .addField(this.players[0].getUsername() + "'s Offer" + (this.confirms[0] ? " :white_check_mark:" : ""), this.offers[0].getOverview(), false)
                .addField(this.players[1].getUsername() + "'s Offer" + (this.confirms[1] ? " :white_check_mark:" : ""), this.offers[1].getOverview(), false);

        this.output.editMessageEmbeds(embed.build()).queue(m -> this.output = m);
    }

    //Confirm
    public void confirm(String player)
    {
        if(!this.offers[this.indexOf(player)].isValid())
        {
            this.unconfirm(player);

            this.offers[this.indexOf(player)].clear();

            this.players[this.indexOf(player)].DM("Your Trade Offer was Invalid! It has been cleared.");
        }
        else this.confirms[this.indexOf(player)] = true;
    }

    public void unconfirm(String player)
    {
        this.confirms[this.indexOf(player)] = false;
    }

    public void unconfirmAll()
    {
        this.confirms[0] = false;
        this.confirms[1] = false;
    }

    public boolean isComplete()
    {
        return this.confirms[0] && this.confirms[1];
    }

    public void complete()
    {
        this.offers[0].transfer(this.players[1]);

        this.offers[1].transfer(this.players[0]);

        this.output.editMessageEmbeds(new EmbedBuilder().setTitle("Trade Between " + this.players[0].getUsername() + " and " + this.players[1].getUsername()).setDescription("Trade Complete!").build()).queue();

        TRADES.remove(this);
    }

    private void setup()
    {
        this.offers = new TradeOffer[]{new TradeOffer(this.players[0]), new TradeOffer(this.players[1])};
        this.confirms = new boolean[2];

        this.output = null;
        this.setStatus(TradeStatus.TRADING);
    }

    private void setStatus(TradeStatus status)
    {
        this.status = status;
    }

    private void setSourceEvent(MessageReceivedEvent sourceEvent)
    {
        this.sourceEvent = sourceEvent;
    }

    private void setPlayers(String player1, String player2)
    {
        this.players = new PlayerDataQuery[]{new PlayerDataQuery(player1), new PlayerDataQuery(player2)};
    }

    private int indexOf(String player)
    {
        return this.players[0].getID().equals(player) ? 0 : 1;
    }

    public boolean isWaiting()
    {
        return this.status.equals(TradeStatus.WAITING);
    }

    public TradeOffer getOffer(String player)
    {
        return this.offers[this.indexOf(player)];
    }

    public static boolean isInTrade(String ID)
    {
        return TRADES.stream().anyMatch(b -> b.players[0].getID().equals(ID) || b.players[1].getID().equals(ID));
    }

    public static Trade instance(String ID)
    {
        return Trade.isInTrade(ID) ? TRADES.stream().filter(b -> b.players[0].getID().equals(ID) || b.players[1].getID().equals(ID)).collect(Collectors.toList()).get(0) : null;
    }

    public static void delete(String ID)
    {
        int index = -1;
        for(int i = 0; i < TRADES.size(); i++) if(TRADES.get(i).players[0].getID().equals(ID) || TRADES.get(i).players[1].getID().equals(ID)) index = i;
        if(index != -1) TRADES.remove(index);
    }

    private enum TradeStatus
    {
        WAITING,
        TRADING;
    }
}
