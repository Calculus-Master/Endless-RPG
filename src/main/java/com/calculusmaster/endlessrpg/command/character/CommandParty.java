package com.calculusmaster.endlessrpg.command.character;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandParty extends Command
{
    public static final int PARTY_SIZE_LIMIT = 6;

    public CommandParty(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        //r!party
        boolean view = this.msg.length == 1;
        //r!party add <number>
        boolean add = this.msg.length >= 3 && this.msg[1].equals("add") && this.isNumericAll(2);
        //r!party remove <number>
        boolean remove = this.msg.length >= 3 && this.msg[1].equals("remove") && this.isNumericAll(2);
        //r!party clear
        boolean clear = this.msg.length == 2 && this.msg[1].equals("clear");
        //r!party set <partyIndex> <number>
        boolean set = this.msg.length == 4 && this.msg[1].equals("set") && this.isNumeric(2) && this.isNumeric(3);

        if(view)
        {
            StringBuilder out = new StringBuilder();

            for(int i = 0; i < this.playerData.getParty().size(); i++)
                out.append(i + 1).append(": ").append(RPGCharacter.build(this.playerData.getParty().get(i)).getListOverview()).append("\n");

            this.embed
                    .setDescription(this.playerData.getParty().isEmpty() ? "Empty" : out.toString())
                    .setTitle(this.player.getName() + "'s Party");
        }
        else if(!add && this.playerData.getParty().isEmpty()) this.response = "Your Party is empty! Add Characters using `r!party add`!";
        else if(add || remove)
        {
            List<String> sourceList = add ? this.playerData.getCharacterList() : this.playerData.getParty();
            List<Integer> nums = new ArrayList<>(Arrays.stream(Arrays.copyOfRange(this.msg, 2, this.msg.length)).map(Integer::parseInt).map(i -> i - 1).toList());
            if(nums.stream().anyMatch(i -> i < 0 || i > sourceList.size())) this.response = "Invalid index!";
            else
            {
                List<String> transfer = nums.stream().map(sourceList::get).distinct().collect(Collectors.toList());

                if(add)
                {
                    if(transfer.size() + sourceList.size() > PARTY_SIZE_LIMIT) this.response = "A Party can have a maximum of %s characters!".formatted(PARTY_SIZE_LIMIT);
                    else if(transfer.size() == 1)
                    {
                        this.playerData.addPartyCharacter(transfer.get(0));

                        this.response = "Added `" + RPGCharacter.build(transfer.get(0)).getName() + "` to your Party!";
                    }
                    else
                    {
                        List<String> newParty = new ArrayList<>(List.copyOf(this.playerData.getParty()));
                        newParty.addAll(transfer);

                        this.playerData.setParty(newParty);

                        this.response = "Added " + transfer.size() + " characters to your Party!";
                    }
                }
                else if(remove)
                {
                    if(transfer.size() > sourceList.size()) this.response = "Your Party does not have that many characters!";
                    else if(transfer.size() == 1)
                    {
                        this.playerData.removePartyCharacter(transfer.get(0));

                        this.response = "Removed `" + RPGCharacter.build(transfer.get(0)).getName() + "` from your Party!";
                    }
                    else
                    {
                        List<String> newParty = new ArrayList<>(List.copyOf(this.playerData.getParty()));
                        newParty.removeIf(transfer::contains);

                        this.playerData.setParty(newParty);

                        this.response = "Removed " + transfer.size() + " characters from your Party!";
                    }
                }
            }
        }
        else if(clear)
        {
            this.playerData.clearParty();

            this.response = "Your party was successfully cleared!";
        }
        else if(set)
        {
            //r!party set <partyIndex> <number>
            int partyIndex = this.getInt(2) - 1;
            int characterIndex = this.getInt(3) - 1;

            if(partyIndex < 0 || partyIndex > this.playerData.getParty().size()) this.response = "Invalid Party index!";
            else if(characterIndex < 0 || characterIndex > this.playerData.getCharacterList().size()) this.response = "Invalid Character index!";
            else
            {
                RPGCharacter character = RPGCharacter.build(this.playerData.getCharacterList().get(characterIndex));

                if(this.playerData.getParty().contains(character.getCharacterID())) this.response = "`" + character.getName() + "` is already in your Party!";
                else
                {
                    List<String> newParty = new ArrayList<>(List.copyOf(this.playerData.getParty()));
                    newParty.set(partyIndex, character.getCharacterID());

                    this.playerData.setParty(newParty);

                    this.response = "`" + character.getName() + "` is now in Position #" + (partyIndex + 1) + " of your Party!";
                }
            }
        }
        else this.response = INVALID;

        return this;
    }
}
