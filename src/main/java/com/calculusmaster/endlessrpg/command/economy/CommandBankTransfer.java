package com.calculusmaster.endlessrpg.command.economy;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.character.RPGRawResourceContainer;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.gameplay.world.skills.RawResource;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandBankTransfer extends Command
{
    public CommandBankTransfer(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        if(!Realm.CURRENT.getLocation(this.playerData.getLocationID()).getType().isTown())
        {
            this.response = "You can only update your Bank in a Town or Hub!";
            return this;
        }

        boolean deposit = this.msg[0].equals("deposit");
        boolean withdraw = this.msg[0].equals("withdraw");

        boolean gold = this.msg.length == 3 && List.of("gold", "g").contains(this.msg[1]) && (this.isNumericAll(2) || this.msg[2].equals("all"));
        boolean loot = this.msg.length >= 3 && List.of("loot", "l").contains(this.msg[1]) && (this.isNumericAll(2) || this.msg[2].equals("all"));
        boolean resources = this.msg.length >= 3 && List.of("resources", "res", "r").contains(this.msg[1]) && (this.msg[2].equals("all") || (this.msg.length == 4 && RawResource.cast(this.msg[2]) != null) && this.isNumeric(3));

        RPGCharacter active = this.playerData.getActiveCharacter();

        if(gold)
        {
            int amount = this.isNumeric(2) ? this.getInt(2) : (deposit ? active.getGold() : this.playerData.getGold());

            if(deposit)
            {
                if(amount == 0) this.response = active.getName() + " has no Gold!";
                else if(active.getGold() < amount) this.response = active.getName() + " does not have that much Gold!";
                else
                {
                    active.removeGold(amount);
                    active.updateGold();
                    this.playerData.addGold(amount);

                    this.response = "Successfully deposited " + amount + " Gold into your Bank!";
                }
            }
            else if(withdraw)
            {
                if(amount == 0) this.response = "You do not have any Gold!";
                else if(this.playerData.getGold() < amount) this.response = "You do not have that much Gold!";
                else
                {
                    active.addGold(amount);
                    active.updateGold();
                    this.playerData.removeGold(amount);

                    this.response = "Successfully withdrew " + amount + " Gold out of your Bank!";
                }
            }
        }
        else if(loot)
        {
            if(deposit && active.getLoot().isEmpty()) this.response = active.getName() + " has no Loot!";
            else if(withdraw && this.playerData.getLoot().isEmpty()) this.response = "You have no Loot!";
            else
            {
                boolean all = this.msg[2].equals("all");
                List<String> transfers = new ArrayList<>();

                if(all) transfers = deposit ? active.getLoot() : this.playerData.getLoot();
                else
                {
                    List<Integer> invalid = new ArrayList<>();

                    for(int i = 2; i < this.msg.length; i++)
                    {
                        int index = this.getInt(i) - 1;
                        List<String> source = deposit ? active.getLoot() : this.playerData.getLoot();
                        if(index >= 0 && index < source.size()) transfers.add(source.get(index));
                        else invalid.add(index + 1);
                    }

                    if(transfers.isEmpty())
                    {
                        this.response = "All provided Loot indices were invalid!";
                        return this;
                    }
                    else if(!invalid.isEmpty()) this.send("The following Loot indices were invalid and the Loot could not be transferred: `" + invalid.toString().replaceAll("\\[", "").replaceAll("]", "") + "`");
                }

                if(deposit && transfers.size() + this.playerData.getLoot().size() > this.playerData.getMaxLootAmount())
                {
                    this.response = "Could not deposit " + transfers.size() + " Loot Items because it would exceed your Bank's Loot capacity!";
                    return this;
                }
                else if(withdraw && transfers.size() + active.getLoot().size() > active.getMaxLootAmount())
                {
                    this.response = "Could not withdraw " + transfers.size() + " Loot Items because it would exceed " + active.getName() + "'s Loot capacity!";
                    return this;
                }

                if(deposit)
                {
                    for(String id : transfers)
                    {
                        active.removeLoot(id);
                        active.getEquipment().remove(id);
                        this.playerData.addLootItem(id);
                    }

                    this.response = all ? "Successfully deposited all of " + active.getName() + "'s Loot into your Bank!" : "Successfully deposited %s Loot Items into your Bank!".formatted(transfers.size());
                }
                else if(withdraw)
                {
                    for(String id : transfers)
                    {
                        active.addLoot(id);
                        this.playerData.removeLootItem(id);
                    }

                    this.response = all ? "Successfully transferred all of your Loot from your Bank to " + active.getName() + "!" : "Successfully withdrew %s Loot Items from your Bank!".formatted(transfers.size());
                }

                active.updateLoot();
                active.updateEquipment();
            }
        }
        else if(resources)
        {
            boolean all = this.msg[2].equals("all");
            RPGRawResourceContainer transfer;

            if(deposit)
            {
                transfer = active.getRawResources();

                if(transfer.isEmpty()) this.response = active.getName() + " does not have any Raw Resources!";
                else if(all)
                {
                    for(RawResource r : RawResource.values())
                    {
                        if(transfer.has(r))
                        {
                            active.getRawResources().decrease(r, transfer.get(r));
                            this.playerData.addResource(r, transfer.get(r));
                        }
                    }

                    active.updateRawResources();

                    this.response = "Successfully deposited all of " + active.getName() + "'s Raw Resources into your Bank!";
                }
                else
                {
                    RawResource r = Objects.requireNonNull(RawResource.cast(this.msg[2]));
                    int amount = this.getInt(3);

                    if(!transfer.has(r)) this.response = active.getName() + " does not have any `" + r.getName() + "`!";
                    else if(transfer.get(r) < amount) this.response = active.getName() + " only has " + transfer.get(r) + " `" + r.getName() + "`!";
                    else
                    {
                        active.getRawResources().decrease(r, amount);
                        this.playerData.addResource(r, amount);

                        active.updateRawResources();

                        this.response = "Successfully deposited " + amount + " `" + r.getName() + "` into your Bank!";
                    }
                }
            }
            else if(withdraw)
            {
                transfer = this.playerData.getResources();

                if(transfer.isEmpty()) this.response = "You do not have any Raw Resources!";
                else if(all)
                {
                    for(RawResource r : RawResource.values())
                    {
                        if(transfer.has(r))
                        {
                            active.getRawResources().increase(r, transfer.get(r));
                            this.playerData.removeResource(r, transfer.get(r));
                        }
                    }

                    active.updateRawResources();

                    this.response = "Successfully withdrew all Raw Resources from your Bank!";
                }
                else
                {
                    RawResource r = Objects.requireNonNull(RawResource.cast(this.msg[2]));
                    int amount = this.getInt(3);

                    if(!transfer.has(r)) this.response = "You do not have any `" + r.getName() + "`!";
                    else if(transfer.get(r) < amount) this.response = "You only have " + transfer.get(r) + " `" + r.getName() + "`!";
                    else
                    {
                        active.getRawResources().increase(r, amount);
                        this.playerData.removeResource(r, amount);

                        active.updateRawResources();

                        this.response = "Successfully withdrew " + amount + " `" + r.getName() + "` from your Bank!";
                    }
                }
            }
        }
        else this.response = INVALID;

        return this;
    }
}
