package com.calculusmaster.endlessrpg.command.character;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.EquipmentType;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.gameplay.loot.LootBuilder;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.gameplay.tasks.Achievement;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandCreate extends Command
{
    public CommandCreate(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        boolean registered = PlayerDataQuery.isRegistered(this.player.getId());

        if(this.msg.length == 1) this.response = "You need to include the character name!";
        else
        {
            if(!registered)
            {
                PlayerDataQuery.register(this.player.getId(), this.player.getName());
                this.playerData = new PlayerDataQuery(this.player.getId());
            }

            String gold = "";
            if(!this.playerData.getCharacterList().isEmpty())
            {
                int cost = (int)Math.pow(100 * this.playerData.getCharacterList().size(), 1.31);
                cost -= cost % 100;

                if(!Realm.CURRENT.getLocation(this.playerData.getLocationID()).getType().isTown())
                {
                    this.response = "You can only create Characters in Towns or the Hub!";
                    return this;
                }
                if(this.playerData.getGold() < cost)
                {
                    this.response = "You don't have enough Gold in your Bank to create a new Character! You need `" + cost + "` Gold!";
                    return this;
                }
                else
                {
                    this.playerData.removeGold(cost);
                    gold = " (Cost: " + cost + " Gold)";
                }
            }

            RPGCharacter c = RPGCharacter.create(this.rawMultiWordContent(1));

            //Starting Inventory
            if(this.playerData.getCharacterList().isEmpty())
            {
                LootItem starterSword = LootBuilder.RandomSword(1).setBoost(Stat.ATTACK, 3);
                starterSword.upload();

                c.equipLoot(EquipmentType.RIGHT_HAND, starterSword.getLootID());
                c.addLoot(starterSword.getLootID());
                c.addGold(100);

                //Auto-Add to Party
                this.playerData.addPartyCharacter(c.getCharacterID());

                //Achievement
                this.playerData.addAchievement(Achievement.CREATE_A_CHARACTER);
            }

            c.upload();
            this.playerData.addCharacter(c.getCharacterID());

            this.response = "`" + c.getName() + "` has joined the world!" + gold;
        }
        return this;
    }
}
