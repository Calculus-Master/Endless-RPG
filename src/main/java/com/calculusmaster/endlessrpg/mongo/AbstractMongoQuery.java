package com.calculusmaster.endlessrpg.mongo;

import com.calculusmaster.endlessrpg.util.helpers.LoggerHelper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class AbstractMongoQuery
{
    protected MongoCollection<Document> database;
    protected Bson query;
    protected Document document;

    public AbstractMongoQuery(String idKey, String idVal, MongoCollection<Document> database)
    {
        this.database = database;
        this.query = Filters.eq(idKey, idVal);
        this.document = database.find(this.query).first();
    }

    protected void update()
    {
        this.document = this.database.find(this.query).first();
    }

    protected void update(Bson update)
    {
        this.database.updateOne(this.query, update);

        LoggerHelper.warn(this.getClass(), "Database Update: " + this.database.getNamespace().getCollectionName() + ", Update Query: " + update);

        this.update();
    }

    protected void update(Bson... updates)
    {
        this.database.updateOne(this.query, Arrays.asList(updates));

        LoggerHelper.warn(this.getClass(), "Database Update: " + this.database.getNamespace().getCollectionName() + ", Update Query: " + Arrays.stream(updates).map(Object::toString).collect(Collectors.joining(", ")));

        this.update();
    }

    public boolean isNull()
    {
        return this.document == null;
    }
}
