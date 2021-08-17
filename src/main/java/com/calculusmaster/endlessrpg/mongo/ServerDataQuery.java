package com.calculusmaster.endlessrpg.mongo;

import com.calculusmaster.endlessrpg.util.Mongo;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

public class ServerDataQuery extends MongoQuery
{
    public ServerDataQuery(String ID)
    {
        super("serverID", ID, Mongo.ServerData);
    }

    public static void register(String ID)
    {
        Document data = new Document()
                .append("serverID", ID)
                .append("prefix", "r!");

        Mongo.ServerData.insertOne(data);
    }

    public static void deregister(String ID)
    {
        Mongo.ServerData.deleteOne(Filters.eq("serverID", ID));
    }

    public static boolean isRegistered(String ID)
    {
        return Mongo.ServerData.find(Filters.eq("serverID", ID)).first() != null;
    }

    //key: "prefix"
    public String getPrefix()
    {
        return this.document.getString("prefix");
    }

    public void setPrefix(String prefix)
    {
        this.update(Updates.set("prefix", prefix));
    }
}
