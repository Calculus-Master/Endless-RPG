package com.calculusmaster.endlessrpg.gameplay.character;

import com.calculusmaster.endlessrpg.gameplay.skills.GatheringSkill;
import com.calculusmaster.endlessrpg.gameplay.skills.RawResource;
import com.mongodb.BasicDBObject;
import org.bson.Document;

import java.util.LinkedHashMap;

public class RPGRawResourceContainer
{
    private LinkedHashMap<RawResource, Integer> resourceValues;

    public RPGRawResourceContainer()
    {
        this.resourceValues = new LinkedHashMap<>();
        for(RawResource r : RawResource.values()) this.resourceValues.put(r, 0);
    }

    public RPGRawResourceContainer(Document resources)
    {
        this.resourceValues = new LinkedHashMap<>();
        for(RawResource r : RawResource.values()) this.resourceValues.put(r, resources.getInteger(r.toString()));
    }

    public BasicDBObject serialized()
    {
        BasicDBObject resources = new BasicDBObject();
        for(RawResource r : RawResource.values()) resources.put(r.toString(), this.resourceValues.get(r));
        return resources;
    }

    public void set(RawResource r, int value)
    {
        this.resourceValues.put(r, value);
    }

    public void increase(RawResource r, int amount)
    {
        this.set(r, this.resourceValues.get(r) + amount);
    }

    public void decrease(RawResource r, int amount)
    {
        this.set(r, this.resourceValues.get(r) - amount);
    }

    public String getOverview(GatheringSkill s)
    {
        StringBuilder content = new StringBuilder();
        for(RawResource r : RawResource.getResources(s)) if(this.resourceValues.get(r) != 0) content.append(r.getName()).append(" - ").append(this.resourceValues.get(r));
        return content.isEmpty() ? "None" : content.deleteCharAt(content.length() - 1).toString();
    }

    public int get(RawResource r)
    {
        return this.resourceValues.get(r);
    }
}
