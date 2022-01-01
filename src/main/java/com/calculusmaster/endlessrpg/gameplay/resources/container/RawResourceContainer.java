package com.calculusmaster.endlessrpg.gameplay.resources.container;

import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.resources.enums.RawResource;
import com.calculusmaster.endlessrpg.gameplay.world.skills.GatheringSkill;
import org.bson.Document;

import java.util.Arrays;

public class RawResourceContainer extends GenericResourceContainer
{
    public RawResourceContainer()
    {
        super(RawResource.values());
    }

    public RawResourceContainer(Document resources)
    {
        super(resources, RawResource.values());
    }

    public RawResourceContainer set(RawResource r, int value)
    {
        return (RawResourceContainer)super.set(r, value);
    }

    public String getOverview(GatheringSkill s)
    {
        StringBuilder content = new StringBuilder();
        RawResource.getResources(s).stream().filter(this::has).forEach(resource -> content.append(this.resourceValues.get(resource)).append("x ").append(resource.getName()).append(" - ").append(this.resourceValues.get(resource)).append("\n"));
        return content.isEmpty() ? "None" : content.deleteCharAt(content.length() - 1).toString();
    }

    public boolean has(GatheringSkill s)
    {
        return Arrays.stream(RawResource.values()).filter(r -> r.getSkill().equals(s)).anyMatch(this::has);
    }

    public boolean canGather(RPGCharacter character)
    {
        return !this.isEmpty() && Arrays.stream(RawResource.values()).filter(this::has).anyMatch(r -> r.canGather(character));
    }
}
