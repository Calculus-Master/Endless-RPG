package com.calculusmaster.endlessrpg.command.misc;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.command.world.CommandTravel;
import com.calculusmaster.endlessrpg.gameplay.battle.Battle;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.LootComponentType;
import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.enums.RPGClass;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.gameplay.loot.LootBuilder;
import com.calculusmaster.endlessrpg.gameplay.loot.LootComponent;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.gameplay.resources.enums.RawResource;
import com.calculusmaster.endlessrpg.gameplay.resources.enums.Resource;
import com.calculusmaster.endlessrpg.gameplay.world.LocationShop;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.gameplay.world.skills.GatheringSkill;
import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;
import com.calculusmaster.endlessrpg.util.Mongo;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Collections;

public class CommandDeveloper extends Command
{
    public static boolean DEV_MODE = false;
    public static final String DEVELOPER = "309135641453527040";

    public CommandDeveloper(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        if(this.msg.length == 1) this.response = INVALID;
        else if(!this.player.getId().equals(DEVELOPER)) this.response = "You cannot use this Command!";
        else
        {
            switch(this.msg[1])
            {
                case "setclass" -> {
                    PlayerDataQuery target = this.getMentions().size() > 0 ? new PlayerDataQuery(this.getMentions().get(0).getId()) : this.playerData;
                    RPGClass clazz = RPGClass.cast(this.msg[2]);
                    RPGCharacter c = target.getActiveCharacter();

                    c.setRPGClass(clazz);
                    c.updateRPGClass();
                }
                case "addloot" -> {
                    PlayerDataQuery target = this.getMentions().size() > 0 ? new PlayerDataQuery(this.getMentions().get(0).getId()) : this.playerData;
                    int activeLevel = target.getActiveCharacter().getLevel();
                    LootItem loot = LootBuilder.create(LootType.cast(this.msg[2]), activeLevel + 1);
                    loot.setRequirements();
                    loot.upload();
                    target.addLootItem(loot.getLootID());
                }
                case "clearbattles" -> Battle.BATTLES.clear();
                case "reset" -> {
                    Mongo.LootData.deleteMany(Filters.exists("lootID"));
                    Mongo.CharacterData.deleteMany(Filters.exists("characterID"));
                    Mongo.PlayerData.deleteMany(Filters.exists("playerID"));
                }
                case "setlevel" -> {
                    PlayerDataQuery player = this.getMentions().size() > 0 ? new PlayerDataQuery(this.getMentions().get(0).getId()) : this.playerData;
                    int target = this.getInt(2);

                    RPGCharacter active = player.getActiveCharacter();
                    while(active.getLevel() != target) active.addExp(active.getExpRequired(active.getLevel() + 1));
                    active.completeUpdate();
                }
                case "addgold" -> {
                    int amount = this.getInt(2);
                    if(this.getMentions().size() > 0) new PlayerDataQuery(this.getMentions().get(0).getId()).addGold(amount);
                    else this.playerData.addGold(amount);
                }
                case "deleteloot" -> {
                    Mongo.LootData.deleteMany(Filters.exists("lootID"));
                    Mongo.PlayerData.updateMany(Filters.exists("playerID"), Updates.set("loot", new ArrayList<>()));
                }
                case "addcorestat" -> {
                    PlayerDataQuery target = this.getMentions().size() > 0 ? new PlayerDataQuery(this.getMentions().get(0).getId()) : this.playerData;
                    Stat s = Stat.cast(this.msg[2]);
                    int amount = this.getInt(3);

                    RPGCharacter active = target.getActiveCharacter();
                    active.increaseCoreStat(s, amount);
                    active.completeUpdate();
                }
                case "newrealm" -> Realm.createNewRealm();
                case "realmmap" -> this.event.getChannel().sendMessage(Realm.CURRENT.getRealmLayout().toString()).queue();
                case "completerealm" -> Realm.CURRENT.getLocations().forEach(l -> { if(!this.playerData.getVisitedLocations().contains(l.getID())) this.playerData.addVisitedLocation(l.getID()); });
                case "removecooldown" -> {
                    String ID = this.getMentions().size() > 0 ? this.getMentions().get(0).getId() : this.player.getId();
                    Collections.synchronizedMap(CommandTravel.TRAVEL_COOLDOWNS).get(ID).cancel(true);
                    Collections.synchronizedMap(CommandTravel.TRAVEL_COOLDOWNS).remove(ID);
                }
                case "devmode" -> {
                    DEV_MODE = !DEV_MODE;
                    this.event.getChannel().sendMessage("Developer Mode is now " + DEV_MODE).queue();
                }
                case "addrawresource" -> {
                    PlayerDataQuery target = this.getMentions().size() > 0 ? new PlayerDataQuery(this.getMentions().get(0).getId()) : this.playerData;
                    GatheringSkill s = GatheringSkill.cast(this.msg[2]);
                    int tier = this.getInt(3);
                    int amount = this.msg.length == 5 ? this.getInt(4) : 1;

                    RPGCharacter active = target.getActiveCharacter();
                    active.getResources().increase(RawResource.getResource(s, tier), amount);
                    active.updateResources();
                }
                case "cycleweather" -> Realm.cycleWeather();
                case "resetshops" -> LocationShop.createShops();
                case "addcomponent" -> {
                    PlayerDataQuery target = this.getMentions().size() > 0 ? new PlayerDataQuery(this.getMentions().get(0).getId()) : this.playerData;
                    Resource r = Resource.cast(this.msg[2]);
                    LootComponentType type = LootComponentType.cast(this.msg[3]);

                    LootComponent component = LootComponent.create(type, r);
                    component.upload();
                    target.addComponent(component.getComponentID());
                }
                default -> throw new IllegalStateException("Invalid Developer Command. Input: " + this.msg[0]);
            }

            this.response = "Developer Command ran!";
        }

        return this;
    }

    public static boolean isDevMode(String ID)
    {
        return ID.equals(DEVELOPER) && DEV_MODE;
    }
}
