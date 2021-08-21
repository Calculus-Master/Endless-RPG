package com.calculusmaster.endlessrpg.command;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.RPGClass;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.util.Mongo;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Random;

public class CommandDeveloper extends Command
{
    public CommandDeveloper(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        if(this.msg.length == 1) this.response = INVALID;
        else if(!this.player.getId().equals("309135641453527040")) this.response = "You cannot use this Command!";
        else
        {
            switch(this.msg[1])
            {
                case "resetself" -> {
                    this.playerData.getCharacterList().stream().map(RPGCharacter::build).forEach(RPGCharacter::delete);
                    Mongo.PlayerData.deleteOne(Filters.eq(this.player.getId()));
                }
                case "forceclasschange" -> {
                    RPGClass clazz = RPGClass.cast(this.msg[2]);
                    RPGCharacter c = this.playerData.getActiveCharacter();

                    c.setRPGClass(clazz);
                    c.updateRPGClass();
                }
                case "clearlootdb" -> Mongo.LootData.deleteMany(Filters.exists("lootID"));
                case "addloot" -> {
                    LootItem loot = switch(this.msg[2]) {
                        case "sword" -> LootItem.createSword(new Random().nextInt(15) + 1);
                        default -> throw new IllegalStateException("Invalid Loot Argument");
                    };

                    loot.upload();
                    this.playerData.addLootItem(loot.getLootID());
                }
                default -> throw new IllegalStateException("Invalid Developer Command. Input: " + this.msg[0]);
            }

            this.response = "Developer Command ran!";
        }

        return this;
    }
}
