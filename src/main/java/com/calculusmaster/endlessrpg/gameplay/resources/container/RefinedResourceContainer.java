package com.calculusmaster.endlessrpg.gameplay.resources.container;

import com.calculusmaster.endlessrpg.gameplay.resources.enums.RefinedResource;
import org.bson.Document;

public class RefinedResourceContainer extends GenericResourceContainer
{
    public RefinedResourceContainer()
    {
        super(RefinedResource.values());
    }

    public RefinedResourceContainer(Document resources)
    {
        super(resources, RefinedResource.values());
    }

    public RefinedResourceContainer set(RefinedResource r, int value)
    {
        return (RefinedResourceContainer)super.set(r, value);
    }
}
