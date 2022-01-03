package com.calculusmaster.endlessrpg.gameplay.character;

import com.calculusmaster.endlessrpg.gameplay.resources.container.RawResourceContainer;
import com.calculusmaster.endlessrpg.gameplay.resources.container.RefinedResourceContainer;
import com.calculusmaster.endlessrpg.gameplay.resources.enums.RawResource;
import com.calculusmaster.endlessrpg.gameplay.resources.enums.Resource;
import org.bson.Document;

public class RPGResourceContainer
{
    private RawResourceContainer raw;
    private RefinedResourceContainer refined;

    public RPGResourceContainer()
    {
        this.raw = new RawResourceContainer();
        this.refined = new RefinedResourceContainer();
    }

    public RPGResourceContainer(Document serialized)
    {
        this.raw = new RawResourceContainer(serialized.get("raw", Document.class));
        this.refined = new RefinedResourceContainer(serialized.get("refined", Document.class));
    }

    public Document serialized()
    {
        return new Document()
                .append("raw", this.raw.serialized())
                .append("refined", this.refined.serialized());
    }

    //Getters

    public RawResourceContainer raw()
    {
        return this.raw;
    }

    public RefinedResourceContainer refined()
    {
        return this.refined;
    }

    public int get(Resource r)
    {
        return r instanceof RawResource ? this.raw().get(r) : this.refined().get(r);
    }

    //Mutators

    public <R extends Resource> void increase(R resource, int amount)
    {
        (resource instanceof RawResource ? this.raw : this.refined).increase(resource, amount);
    }

    public <R extends Resource> void decrease(R resource, int amount)
    {
        (resource instanceof RawResource ? this.raw : this.refined).decrease(resource, amount);
    }

    //Misc

    public boolean has(Resource r)
    {
        return this.raw().has(r) || this.refined.has(r);
    }

    public boolean isEmpty()
    {
        return this.raw.isEmpty() && this.refined.isEmpty();
    }

    //Copy

    public static RPGResourceContainer copyOf(RPGResourceContainer other)
    {
        return new RPGResourceContainer(other.serialized());
    }
}
