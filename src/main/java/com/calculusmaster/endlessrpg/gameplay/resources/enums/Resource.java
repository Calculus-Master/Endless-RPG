package com.calculusmaster.endlessrpg.gameplay.resources.enums;

import java.util.ArrayList;
import java.util.List;

public interface Resource
{
    String getName();

    int getTier();

    static List<Resource> all()
    {
        List<Resource> combined = new ArrayList<>();

        combined.addAll(List.of(RawResource.values()));
        combined.addAll(List.of(RefinedResource.values()));

        return combined;
    }

    static Resource cast(String input)
    {
        RawResource raw = RawResource.cast(input);
        RefinedResource refined = RefinedResource.cast(input);

        if(raw != null) return raw;
        else if(refined != null) return refined;
        else return null;
    }
}
