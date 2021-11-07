package com.calculusmaster.endlessrpg.gameplay.character;

import com.calculusmaster.endlessrpg.gameplay.world.skills.GatheringSkill;
import com.calculusmaster.endlessrpg.gameplay.world.skills.RawResource;
import com.mongodb.BasicDBObject;
import org.bson.Document;

import java.util.Arrays;
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
        for(RawResource r : RawResource.values()) this.resourceValues.put(r, resources.getInteger(r.toString(), 0));
    }

    public BasicDBObject serialized()
    {
        BasicDBObject resources = new BasicDBObject();
        for(RawResource r : RawResource.values()) if(this.resourceValues.get(r) > 0) resources.put(r.toString(), this.resourceValues.get(r));
        return resources;
    }

    public RPGRawResourceContainer set(RawResource r, int value)
    {
        this.resourceValues.put(r, value);
        return this;
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
        for(RawResource r : RawResource.getResources(s)) if(this.resourceValues.get(r) != 0) content.append(this.resourceValues.get(r)).append("x ").append(r.getName()).append(" - ").append(this.resourceValues.get(r)).append("\n");
        return content.isEmpty() ? "None" : content.deleteCharAt(content.length() - 1).toString();
    }

    public String getFullOverview()
    {
        StringBuilder content = new StringBuilder();
        for(RawResource r : RawResource.values()) if(this.resourceValues.get(r) != 0) content.append(r.getName()).append(" - ").append(this.resourceValues.get(r)).append("\n");
        return content.isEmpty() ? "None" : content.deleteCharAt(content.length() - 1).toString();
    }

    public boolean has(RawResource r)
    {
        return this.get(r) != 0;
    }

    public boolean has(GatheringSkill s)
    {
        return Arrays.stream(RawResource.values()).filter(r -> r.getSkill().equals(s)).anyMatch(this::has);
    }

    public boolean isEmpty()
    {
        return this.resourceValues.values().stream().allMatch(i -> i == 0);
    }

    public boolean canGather(RPGCharacter character)
    {
        return !this.isEmpty() && Arrays.stream(RawResource.values()).filter(this::has).anyMatch(r -> r.canGather(character));
    }

    public int get(RawResource r)
    {
        return this.resourceValues.get(r);
    }
}
