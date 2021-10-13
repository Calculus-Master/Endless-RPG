package com.calculusmaster.endlessrpg.gameplay.character;

import com.calculusmaster.endlessrpg.gameplay.enums.ElementType;
import com.mongodb.BasicDBObject;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;

public class RPGElementalContainer
{
    //The Integer actually correlates to a %
    private Map<ElementType, Integer> elementValues;

    public RPGElementalContainer()
    {
        this.elementValues = new HashMap<>();
        for(ElementType element : ElementType.values()) this.elementValues.put(element, 0);
    }

    public RPGElementalContainer(Document elements)
    {
        this();
        for(ElementType element : ElementType.values()) this.elementValues.put(element, elements.getInteger(element.toString()));
    }

    public BasicDBObject serialized()
    {
        BasicDBObject elements = new BasicDBObject();
        for(ElementType element : ElementType.values()) elements.put(element.toString(), this.elementValues.get(element));
        return elements;
    }

    public void set(ElementType element, int value)
    {
        this.elementValues.put(element, value);
    }

    public void increase(ElementType element, int amount)
    {
        this.set(element, this.elementValues.get(element) + amount);
    }

    public void decrease(ElementType element, int amount)
    {
        this.set(element, this.elementValues.get(element) - amount);
    }

    public void combine(RPGElementalContainer other)
    {
        for(ElementType element : ElementType.values()) this.increase(element, other.getRaw(element));
    }

    public double get(ElementType element)
    {
        return this.getRaw(element) / 100.;
    }

    public int getRaw(ElementType element)
    {
        return this.elementValues.get(element);
    }
}
