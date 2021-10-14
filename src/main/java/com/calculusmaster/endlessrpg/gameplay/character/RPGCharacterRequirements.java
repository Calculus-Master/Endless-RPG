package com.calculusmaster.endlessrpg.gameplay.character;

import com.calculusmaster.endlessrpg.gameplay.enums.ElementType;
import com.calculusmaster.endlessrpg.gameplay.enums.RPGClass;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.util.Global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RPGCharacterRequirements
{
    private int level;
    private List<RPGClass> clazz;
    private Map<Stat, Integer> stat;
    private Map<Stat, Integer> coreStat;
    private Map<ElementType, Integer> elementalDamage;
    private Map<ElementType, Integer> elementalDefense;

    {
        this.level = 0;
        this.clazz = new ArrayList<>();
        this.stat = new HashMap<>();
        this.coreStat = new HashMap<>();
        this.elementalDamage = new HashMap<>();
        this.elementalDefense = new HashMap<>();
    }

    public RPGCharacterRequirements addLevel(int level)
    {
        this.level = level;
        return this;
    }

    public RPGCharacterRequirements addClass(RPGClass clazz)
    {
        this.clazz.add(clazz);
        return this;
    }

    public RPGCharacterRequirements addStat(Stat s, int value)
    {
        this.stat.put(s, value);
        return this;
    }

    public RPGCharacterRequirements addCoreStat(Stat s, int value)
    {
        this.coreStat.put(s, value);
        return this;
    }

    public RPGCharacterRequirements addElementalDamage(ElementType e, int value)
    {
        this.elementalDamage.put(e, value);
        return this;
    }

    public RPGCharacterRequirements addElementalDefense(ElementType e, int value)
    {
        this.elementalDefense.put(e, value);
        return this;
    }

    public String getOverview()
    {
        List<String> overview = new ArrayList<>();

        if(this.level != 0) overview.add("Level: " + this.level);

        if(!this.clazz.isEmpty())
        {
            StringBuilder s = new StringBuilder("Class: ");
            for(RPGClass clazz : this.clazz) s.append(Global.normalize(clazz.toString())).append(", ");
            s.deleteCharAt(s.length() - 1).deleteCharAt(s.length() - 1);
            overview.add(s.toString());
        }

        if(!this.stat.isEmpty())
        {
            StringBuilder s = new StringBuilder("Stats: ");
            for(Map.Entry<Stat, Integer> e : this.stat.entrySet()) s.append(Global.normalize(e.getKey().toString())).append("(").append(e.getValue()).append(")").append(", ");
            s.deleteCharAt(s.length() - 1).deleteCharAt(s.length() - 1);
            overview.add(s.toString());
        }

        if(!this.coreStat.isEmpty())
        {
            StringBuilder s = new StringBuilder("Core Stats: ");
            for(Map.Entry<Stat, Integer> e : this.coreStat.entrySet()) s.append(Global.normalize(e.getKey().toString())).append("(").append(e.getValue()).append(")").append(", ");
            s.deleteCharAt(s.length() - 1).deleteCharAt(s.length() - 1);
            overview.add(s.toString());
        }

        if(!this.elementalDamage.isEmpty())
        {
            StringBuilder s = new StringBuilder("Elemental Damage: ");
            for(Map.Entry<ElementType, Integer> e : this.elementalDamage.entrySet()) s.append(e.getValue()).append(" ").append(e.getKey().getIcon().getAsMention()).append(", ");
            s.deleteCharAt(s.length() - 1).deleteCharAt(s.length() - 1);
            overview.add(s.toString());
        }

        if(!this.elementalDefense.isEmpty())
        {
            StringBuilder s = new StringBuilder("Elemental Defense: ");
            for(Map.Entry<ElementType, Integer> e : this.elementalDefense.entrySet()) s.append(e.getValue()).append(" ").append(e.getKey().getIcon().getAsMention()).append(", ");
            s.deleteCharAt(s.length() - 1).deleteCharAt(s.length() - 1);
            overview.add(s.toString());
        }

        if(overview.isEmpty()) overview.add("None");

        StringBuilder out = new StringBuilder();
        overview.forEach(s -> out.append(s).append("\n"));
        return out.deleteCharAt(out.length() - 1).toString();
    }

    public boolean check(RPGCharacter c)
    {
        //Level
        if(c.getLevel() != 0 && c.getLevel() < this.level) return false;

        //Class
        if(!this.clazz.isEmpty() && !this.clazz.contains(c.getRPGClass())) return false;

        //Stat
        if(!this.stat.isEmpty() && !this.stat.entrySet().stream().allMatch(e -> c.getStat(e.getKey()) >= e.getValue())) return false;

        //Core Stat
        if(!this.coreStat.isEmpty() && !this.coreStat.entrySet().stream().allMatch(e -> c.getCoreStat(e.getKey()) >= e.getValue())) return false;

        //Elemental Damage
        if(!this.elementalDamage.isEmpty() && !this.elementalDamage.entrySet().stream().allMatch(e -> c.getEquipment().combinedElementalDamage().get(e.getKey()) >= e.getValue())) return false;

        //Elemental Defense
        if(!this.elementalDefense.isEmpty() && !this.elementalDefense.entrySet().stream().allMatch(e -> c.getEquipment().combinedElementalDefense().get(e.getKey()) >= e.getValue())) return false;

        return true;
    }
}
