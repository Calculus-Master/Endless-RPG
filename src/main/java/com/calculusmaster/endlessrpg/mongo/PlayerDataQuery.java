package com.calculusmaster.endlessrpg.mongo;

import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
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

    public static void register(String ID)
    {
        Document data = new Document()
                .append("playerID", ID)
                .append("characters", new JSONArray())
                .append("selected", 0)
                .append("gold", 100);

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
}
