package com.calculusmaster.endlessrpg.gameplay.resources.container;

import com.calculusmaster.endlessrpg.gameplay.resources.enums.Resource;
import org.bson.Document;

import java.util.Arrays;
import java.util.LinkedHashMap;

public class GenericResourceContainer
{
    protected LinkedHashMap<Resource, Integer> resourceValues;
    private final Resource[] values;

    public GenericResourceContainer(Resource[] values)
    {
        this.resourceValues = new LinkedHashMap<>();
        this.values = values;

        Arrays.stream(values).forEach(resource -> this.resourceValues.put(resource, 0));
    }

    protected GenericResourceContainer(Document serialized, Resource[] values)
    {
        this(values);
        Arrays.stream(values).forEach(resource -> this.resourceValues.put(resource, serialized.getInteger(resource.toString(), 0)));
    }

    public Document serialized()
    {
        Document data = new Document();
        Arrays.stream(this.values).filter(this::has).forEach(resource -> data.append(resource.toString(), this.get(resource)));
        return data;
    }

    public GenericResourceContainer set(Resource r, int value)
    {
        this.resourceValues.put(r, value);
        return this;
    }

    public void increase(Resource r, int amount)
    {
        this.set(r, this.resourceValues.get(r) + amount);
    }

    public void decrease(Resource r, int amount)
    {
        this.set(r, this.resourceValues.get(r) - amount);
    }

    public boolean has(Resource r)
    {
        return this.get(r) != 0;
    }

    public String getFullOverview()
    {
        StringBuilder content = new StringBuilder();
        Arrays.stream(this.values).filter(this::has).forEach(resource -> content.append(resource.getName()).append(" - ").append(this.resourceValues.get(resource)).append("\n"));
        return content.isEmpty() ? "None" : content.deleteCharAt(content.length() - 1).toString();
    }

    public boolean isEmpty()
    {
        return this.resourceValues.values().stream().allMatch(i -> i == 0);
    }

    public int get(Resource r)
    {
        return this.resourceValues.get(r);
    }
}
