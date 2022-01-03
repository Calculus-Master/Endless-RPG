package com.calculusmaster.endlessrpg.gameplay.loot;

import com.calculusmaster.endlessrpg.gameplay.enums.LootComponentType;
import com.calculusmaster.endlessrpg.gameplay.resources.enums.Resource;
import com.calculusmaster.endlessrpg.util.Mongo;
import com.calculusmaster.endlessrpg.util.helpers.IDHelper;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.Objects;

public class LootComponent
{
    private String componentID;
    private LootComponentType type;
    private Resource resource;

    private LootComponent() {}

    public static LootComponent create(LootComponentType type, Resource resource)
    {
        LootComponent component = new LootComponent();

        component.setComponentID();
        component.setType(type);
        component.setResource(resource);

        return component;
    }

    public static LootComponent build(String componentID)
    {
        Document data = Objects.requireNonNull(Mongo.ComponentData.find(Filters.eq("componentID", componentID)).first());

        LootComponent component = new LootComponent();

        component.setComponentID(componentID);
        component.setType(LootComponentType.cast(data.getString("type")));
        component.setResource(Resource.cast(data.getString("resource")));

        return component;
    }

    public void upload()
    {
        Document data = new Document()
                .append("componentID", this.componentID)
                .append("type", this.type.toString())
                .append("resource", this.resource.toString());

        Mongo.ComponentData.insertOne(data);
    }

    public void delete()
    {
        Mongo.ComponentData.deleteOne(Filters.eq("componentID", this.componentID));
    }

    //Accessors

    public String getName()
    {
        return this.resource.getName() + " " + this.type.getName();
    }

    private void setResource(Resource resource)
    {
        this.resource = resource;
    }

    public Resource getResource()
    {
        return this.resource;
    }

    private void setType(LootComponentType type)
    {
        this.type = type;
    }

    public LootComponentType getType()
    {
        return this.type;
    }

    public void setComponentID(String componentID)
    {
        this.componentID = componentID;
    }

    public void setComponentID()
    {
        this.setComponentID(IDHelper.create(6));
    }

    public String getComponentID()
    {
        return this.componentID;
    }
}
