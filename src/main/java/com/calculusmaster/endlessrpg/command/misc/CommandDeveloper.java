package com.calculusmaster.endlessrpg.command.misc;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.enums.RPGClass;
import com.calculusmaster.endlessrpg.gameplay.loot.LootBuilder;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.util.Mongo;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
                    int activeLevel = this.playerData.getActiveCharacter().getLevel();
                    LootItem loot = LootBuilder.reward(LootType.cast(this.msg[2]), activeLevel + 1);
                    loot.upload();
                    this.playerData.addLootItem(loot.getLootID());
                }
                case "clearbattles" -> Battle.BATTLES.clear();
                case "reset" -> {
                    Mongo.LootData.deleteMany(Filters.exists("lootID"));
                    Mongo.CharacterData.deleteMany(Filters.exists("characterID"));
                    Mongo.PlayerData.deleteOne(Filters.exists("playerID"));
                }
                case "forcelevel" -> {
                    int target = this.getInt(2);
                    RPGCharacter active = this.playerData.getActiveCharacter();
                    while(active.getLevel() != target) active.addExp(active.getExpRequired(active.getLevel() + 1));
                    active.completeUpdate();
                }
                default -> throw new IllegalStateException("Invalid Developer Command. Input: " + this.msg[0]);
            }

            this.response = "Developer Command ran!";
        }

        return this;
    }
}
