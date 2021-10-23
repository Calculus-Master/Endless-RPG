package com.calculusmaster.endlessrpg.command.economy;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.player.Trade;
import com.calculusmaster.endlessrpg.gameplay.player.TradeOffer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandTrade extends Command
{
    private static final List<String> TRADE_REQUESTS = new ArrayList<>();

    public CommandTrade(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        boolean start = !this.getMentions().isEmpty();

        boolean accept = this.msg.length == 2 && this.msg[1].equals("accept");
        boolean deny = this.msg.length == 2 && this.msg[1].equals("deny");

        boolean cancel = this.msg.length == 2 && this.msg[1].equals("cancel");

        boolean confirm = this.msg.length == 2 && this.msg[1].equals("confirm");
        boolean unconfirm = this.msg.length == 2 && this.msg[1].equals("unconfirm");

        boolean add = this.msg.length > 2 && this.msg[1].equals("add");
        boolean remove = this.msg.length > 2 && this.msg[1].equals("remove");
        boolean set = this.msg.length > 2 && this.msg[1].equals("set");

        if(start)
        {
            Member other = this.getMentions().get(0);

            if(Trade.isInTrade(this.player.getId())) this.response = "You are already in another Trade!";
            else if(Trade.isInTrade(other.getId())) this.response = other.getEffectiveName() + " is in another Trade!";
            else
            {
                Trade.create(this.player.getId(), other.getId(), this.event);
                TRADE_REQUESTS.add(other.getId());

                this.response = other.getAsMention() + "! " + this.player.getName() + " has invited you to a Trade! Type `r!trade accept` or `r!trade deny` to continue!";
            }
        }
        else if(accept || deny)
        {
            if(!Trade.isInTrade(this.player.getId()) || !TRADE_REQUESTS.contains(this.player.getId())) this.response = "You have not been invited to a Trade!";
            else if(Trade.isInTrade(this.player.getId()) && !Objects.requireNonNull(Trade.instance(this.player.getId())).isWaiting()) this.response = "You are already in a Trade!";
            else
            {
                TRADE_REQUESTS.remove(this.player.getId());

                if(accept)
                {
                    this.embed = null;
                    this.send("Trade request accepted!");

                    Objects.requireNonNull(Trade.instance(this.player.getId())).sendStartEmbed();
                }
                else if(deny)
                {
                    Trade.delete(this.player.getId());

                    this.response = "Trade request denied!";
                }
            }
        }
        else if(cancel)
        {
            if(!Trade.isInTrade(this.player.getId())) this.response = "You are not in a Trade!";
            else
            {
                Trade.delete(this.player.getId());

                this.response = "Trade cancelled!";
            }
        }
        else if(confirm || unconfirm)
        {
            if(!Trade.isInTrade(this.player.getId())) this.response = "You are not in a Trade!";
            else if(Trade.instance(this.player.getId()).isWaiting()) this.response = "The Trade has not started yet!";
            else
            {
                Trade t = Objects.requireNonNull(Trade.instance(this.player.getId()));

                if(confirm)
                {
                    t.confirm(this.player.getId());

                    if(t.isComplete()) t.complete();
                }
                else if(unconfirm) t.unconfirm(this.player.getId());
            }
        }
        else if(add || remove || set)
        {
            if(!Trade.isInTrade(this.player.getId())) this.response = "You are not in a Trade!";
            else if(Trade.instance(this.player.getId()).isWaiting()) this.response = "The Trade has not started yet!";
            else
            {
                Trade t = Objects.requireNonNull(Trade.instance(this.player.getId()));
                TradeOffer o = t.getOffer(this.player.getId());

                //r!trade <add:remove:set> <gold:loot:resources> <amount:ID>
                boolean gold = this.msg.length == 4 && List.of("gold", "g").contains(this.msg[2]) && this.isNumeric(3) && this.getInt(3) > 0;
                boolean loot = this.msg.length >= 4 && List.of("loot", "l", "gear").contains(this.msg[2]) && this.isNumericAll(3);
                boolean resources = false; //TODO: Resource Bank and Resource Trading

                if(gold)
                {
                    int amount = this.getInt(3);

                    if(this.playerData.getGold() < amount || (add && this.playerData.getGold() < amount + o.getGold())) this.response = "You do not have that much Gold!";
                    else
                    {

                        if(add) o.addGold(amount);
                        else if(remove) o.removeGold(amount);
                        else if(set) o.setGold(amount);

                        t.sendTradeUpdateEmbed();
                    }
                }
                else if(loot)
                {
                    if(set) this.response = "Subcommand 'set' is not supported for Loot!";
                    else
                    {
                        List<Integer> loots = new ArrayList<>();
                        for(int i = 3; i < this.msg.length; i++) loots.add(this.getInt(3));

                        for(int i : loots)
                        {
                            if(i >= 1 && i <= this.playerData.getLoot().size())
                            {
                                if(add) o.addLoot(this.playerData.getLoot().get(i - 1));
                                else if(remove) o.removeLoot(this.playerData.getLoot().get(i - 1));
                            }
                        }

                        t.sendTradeUpdateEmbed();
                    }
                }
                else if(resources)
                {
                    this.response = "Resource trading is currently not implemented!";
                }
                else
                {
                    this.response = INVALID;
                    return this;
                }

                t.unconfirmAll();
            }
        }
        else this.response = INVALID;

        return this;
    }
}
