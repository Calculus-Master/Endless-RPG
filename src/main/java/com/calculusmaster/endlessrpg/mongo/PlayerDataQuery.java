package com.calculusmaster.endlessrpg.mongo;

import com.calculusmaster.endlessrpg.EndlessRPG;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.util.Mongo;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.json.JSONArray;

import java.util.List;

public class PlayerDataQuery extends AbstractMongoQuery
{
    public PlayerDataQuery(String ID)
    {
        super("playerID", ID, Mongo.PlayerData);
    }

    public static void register(String ID, String username)
    {
        Document data = new Document()
                .append("playerID", ID)
                .append("username", username)
                .append("characters", new JSONArray())
                .append("selected", 0)
                .append("gold", 100)
                .append("location", Realm.CURRENT.getLocations().get(0).getID())
                .append("visited", List.of(Realm.CURRENT.getLocations().get(0).getID()))
                .append("loot", new JSONArray());

        Mongo.PlayerData.insertOne(data);
    }

    public static boolean isRegistered(String ID)
    {
        return Mongo.PlayerData.find(Filters.eq("playerID", ID)).first() != null;
    }

    public String getID()
    {
        return this.document.getString("playerID");
    }

    public String getMention()
    {
        return "<@" + this.getID() + ">";
    }

    public void DM(String content)
    {
        EndlessRPG.BOT_JDA.openPrivateChannelById(this.getID()).flatMap(channel -> channel.sendMessage(content)).queue();
    }

    //key: "username"
    public String getUsername()
    {
        return this.document.getString("username");
    }

    //key: "characters"
    public List<String> getCharacterList()
    {
        return this.document.getList("characters", String.class);
    }

    public void addCharacter(String ID)
    {
        this.update(Updates.push("characters", ID));
    }

    public void removeCharacter(String ID)
    {
        this.update(Updates.pull("characters", ID));
    }

    public int getLevel()
    {
        int max = -1;
        for(String s : this.getCharacterList()) max = Math.max(max, RPGCharacter.build(s).getLevel());
        return max;
    }

    //key: "selected"
    public int getSelected()
    {
        int selected = this.document.getInteger("selected");

        if(selected < 0 || selected >= this.getCharacterList().size())
        {
            this.setSelected(0);
            return 0;
        }
        else return selected;
    }

    public void setSelected(int selected)
    {
        this.update(Updates.set("selected", selected));
    }

    public RPGCharacter getActiveCharacter()
    {
        return RPGCharacter.build(this.getCharacterList().get(this.getSelected()));
    }

    //key: "gold"
    public int getGold()
    {
        return this.document.getInteger("gold");
    }

    private void changeGold(int amount)
    {
        this.update(Updates.set("gold", this.getGold() + amount));
    }

    public void addGold(int amount)
    {
        this.changeGold(amount);
    }

    public void removeGold(int amount)
    {
        this.changeGold(-1 * amount);
    }

    //key: "location"
    public String getLocationID()
    {
        return this.document.getString("location");
    }

    public void setLocation(String locationID)
    {
        this.update(Updates.set("location", locationID));
    }

    public void resetLocation()
    {
        this.setLocation("");
    }

    //key: "visited"
    public List<String> getVisitedLocations()
    {
        return this.document.getList("visited", String.class);
    }

    public void addVisitedLocation(String locationID)
    {
        this.update(Updates.push("visited", locationID));
    }

    public void clearVisitedLocations()
    {
        this.update(Updates.set("visited", new JSONArray()));
    }

    //key: "loot"
    public List<String> getLoot()
    {
        return this.document.getList("loot", String.class);
    }

    public void addLootItem(String ID)
    {
        if(this.getLoot().size() == this.getMaxLootAmount())
        {
            this.DM("You have reached your maximum Loot amount! Any newly acquired loot will be lost forever!");
            LootItem.delete(ID);
        }
        else this.update(Updates.push("loot", ID));
    }

    public int getMaxLootAmount()
    {
        return 50; //TODO: Player Level, and adjusting the inventory size for that
    }

    public void removeLootItem(String ID)
    {
        this.update(Updates.pull("loot", ID));
    }
}
