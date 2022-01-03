package com.calculusmaster.endlessrpg.gameplay.character;

import com.calculusmaster.endlessrpg.gameplay.battle.player.AIPlayer;
import com.calculusmaster.endlessrpg.gameplay.battle.player.AbstractPlayer;
import com.calculusmaster.endlessrpg.gameplay.enums.ElementType;
import com.calculusmaster.endlessrpg.gameplay.enums.EquipmentType;
import com.calculusmaster.endlessrpg.gameplay.enums.RPGClass;
import com.calculusmaster.endlessrpg.gameplay.enums.Stat;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.gameplay.resources.enums.RawResource;
import com.calculusmaster.endlessrpg.gameplay.spell.Spell;
import com.calculusmaster.endlessrpg.gameplay.spell.SpellData;
import com.calculusmaster.endlessrpg.gameplay.world.skills.GatheringSkill;
import com.calculusmaster.endlessrpg.mongo.PlayerDataQuery;
import com.calculusmaster.endlessrpg.util.Global;
import com.calculusmaster.endlessrpg.util.Mongo;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class RPGCharacter
{
    //Core Fields
    private String characterID;
    private Bson query;

    //Database Fields

    private String name;
    private RPGClass classRPG;
    private int level;
    private int experience;
    private final LinkedHashMap<Stat, Integer> stats = new LinkedHashMap<>();
    private LinkedHashMap<GatheringSkill, Integer> skillLevels = new LinkedHashMap<>();
    private LinkedHashMap<GatheringSkill, Integer> skillExperience = new LinkedHashMap<>();
    private RPGEquipment equipment;
    private List<String> spells;
    private RPGElementalContainer coreElementalDamage;
    private RPGElementalContainer coreElementalDefense;
    private RPGResourceContainer resources;
    private int gold;
    private List<String> loot;

    //Battle Only Fields

    private Optional<Integer> health = Optional.empty();
    private Optional<AbstractPlayer> owner = Optional.empty();
    private Optional<LinkedHashMap<Stat, Integer>> changes = Optional.empty();

    //Use Factory methods!
    private RPGCharacter() {}

    //Factory
    public static RPGCharacter create(String name)
    {
        RPGCharacter c = new RPGCharacter();

        c.setCharacterID();
        c.setName(name);
        c.setRPGClass(RPGClass.RECRUIT);
        c.setLevel(1);
        c.setExp(0);
        c.setCoreStats();
        c.setSkillLevel();
        c.setSkillExperience();
        c.setEquipment();
        c.setSpells();
        c.setCoreElementalDamage();
        c.setCoreElementalDefense();
        c.setResources();
        c.setGold();
        c.setLoot();

        return c;
    }

    public static RPGCharacter build(String characterID)
    {
        Document data = Objects.requireNonNull(Mongo.CharacterData.find(Filters.eq("characterID", characterID)).first(), "Could not build Character from ID: " + characterID);

        RPGCharacter c = new RPGCharacter();

        c.setCharacterID(characterID);
        c.setName(data.getString("name"));
        c.setRPGClass(RPGClass.cast(data.getString("class")));
        c.setLevel(data.getInteger("level"));
        c.setExp(data.getInteger("exp"));
        c.setCoreStats(data.get("stats", Document.class));
        c.setSkillLevel(data.get("skillLevel", Document.class));
        c.setSkillExperience(data.get("skillExp", Document.class));
        c.setEquipment(data.get("equipment", Document.class));
        c.setSpells(data.getList("spells", String.class));
        c.setCoreElementalDamage(data.get("coreElementalDamage", Document.class));
        c.setCoreElementalDefense(data.get("coreElementalDefense", Document.class));
        c.setResources(data.get("resources", Document.class));
        c.setGold(data.getInteger("gold"));
        c.setLoot(data.getList("loot", String.class));

        return c;
    }

    //Creates the object with necessary Battle fields
    public void forBattle(AbstractPlayer owner)
    {
        this.forBattle(owner, true);
    }

    public void forBattle(AbstractPlayer owner, boolean health)
    {
        this.setOwner(owner);

        if(health) this.setMaxHealth();

        this.setChanges();
    }

    //Updates

    public void upload()
    {
        Document d = new Document()
                .append("characterID", this.characterID)
                .append("name", this.name)
                .append("class", this.classRPG.toString())
                .append("level", this.level)
                .append("exp", this.experience)
                .append("stats", Global.serializedMap(this.stats, Stat.values()))
                .append("skillLevel", Global.serializedMap(this.skillLevels, GatheringSkill.values()))
                .append("skillExp", Global.serializedMap(this.skillExperience, GatheringSkill.values()))
                .append("equipment", this.equipment.serialized())
                .append("spells", this.spells)
                .append("coreElementalDamage", this.coreElementalDamage.serialized())
                .append("coreElementalDefense", this.coreElementalDefense.serialized())
                .append("resources", this.resources.serialized())
                .append("gold", this.gold)
                .append("loot", this.loot);

        Mongo.CharacterData.insertOne(d);
    }

    public void delete()
    {
        Mongo.CharacterData.deleteOne(this.query);
    }

    public void completeUpdate()
    {
        this.delete();
        this.upload();
    }

    private void update(Bson... updates)
    {
        Mongo.CharacterData.updateOne(this.query, Arrays.asList(updates));
    }

    public void updateName()
    {
        this.update(Updates.set("name", this.name));
    }

    public void updateRPGClass()
    {
        this.update(Updates.set("class", this.classRPG.toString()));
    }

    public void updateExperience()
    {
        this.update(Updates.set("level", this.level), Updates.set("experience", this.experience));
    }

    public void updateEquipment()
    {
        this.update(Updates.set("equipment", this.equipment.serialized()));
    }

    public void updateSpells()
    {
        this.update(Updates.set("spells", this.spells));
    }

    public void updateCoreElementalDamage()
    {
        this.update(Updates.set("coreElementalDamage", this.coreElementalDamage.serialized()));
    }

    public void updateCoreElementalDefense()
    {
        this.update(Updates.set("coreElementalDefense", this.coreElementalDefense.serialized()));
    }

    public void updateResources()
    {
        this.update(Updates.set("resources", this.resources.serialized()));
    }

    public void updateGold()
    {
        this.update(Updates.set("gold", this.gold));
    }

    public void updateLoot()
    {
        this.update(Updates.set("loot", this.loot));
    }

    public void updateSkillExperience()
    {
        this.update(Updates.set("skillLevel", Global.serializedMap(this.skillLevels, GatheringSkill.values())), Updates.set("skillExp", Global.serializedMap(this.skillExperience, GatheringSkill.values())));
    }

    //Misc
    public String getListOverview()
    {
        String name = "\"" + this.getName() + "\"";
        String level = "Level: " + this.getLevel();
        String clazz = "Class: " + Global.normalize(this.getRPGClass().toString());
        String statTotal = "Power: " + Arrays.stream(Stat.values()).mapToInt(this::getStat).sum();

        return name + " | " + level + " | " + clazz + " | " + statTotal;
    }

    //Stat Changes
    public LinkedHashMap<Stat, Integer> getChanges()
    {
        return this.changes.orElse(new LinkedHashMap<>());
    }

    public void setChanges()
    {
        this.changes = Optional.of(new LinkedHashMap<>());
    }

    public void addChange(Stat s, int change)
    {
        LinkedHashMap<Stat, Integer> changes = this.getChanges();
        changes.put(s, changes.getOrDefault(s, 0) + change);
        this.changes = Optional.of(changes);
    }

    //Owner
    public AbstractPlayer getOwner()
    {
        return this.owner.orElse(null);
    }

    public String getOwnerID()
    {
        return this.getOwner().ID;
    }

    public void setOwner(AbstractPlayer owner)
    {
        this.owner = owner == null ? Optional.empty() : Optional.of(owner);
    }

    public boolean isAI()
    {
        return this.getOwner() instanceof AIPlayer;
    }

    public boolean isOwnedBy(String owner)
    {
        return this.getOwnerID().equals(owner);
    }

    //Health
    public int getHealth()
    {
        return this.health.orElse(-1);
    }

    public void setMaxHealth()
    {
        this.setHealth(this.getStat(Stat.HEALTH));
    }

    public void setHealth(int health)
    {
        this.health = Optional.of(health);
    }

    public void damage(int amount)
    {
        this.setHealth(this.getHealth() - amount);

        if(this.isDefeated() && this.gold > 0)
        {
            Executors.newSingleThreadExecutor().execute(() -> {

                int lostGold = new SplittableRandom().nextInt((int)(this.gold * 0.05), (int)(this.gold * 0.3));

                this.gold -= lostGold;

                if(lostGold > 0) this.updateGold();

                List<String> lostLoot = new ArrayList<>();

                for(String l : this.getLoot())
                {
                    if(!this.equipment.asList().contains(l) && new SplittableRandom().nextInt(100) < 20) lostLoot.add(l);
                    else if(new SplittableRandom().nextInt(100) < 5) lostLoot.add(l);
                }

                for(String l : lostLoot)
                {
                    this.removeLoot(l);
                    if(this.equipment.asList().contains(l)) this.equipment.remove(l);
                }

                if(lostLoot.isEmpty())
                {
                    this.updateLoot();
                    this.updateEquipment();
                }

                int lostResourceCount = 0;

                for(RawResource r : RawResource.values())
                {
                    if(this.resources.has(r))
                    {
                        int lostAmount = new SplittableRandom().nextInt((int)(this.resources.get(r) * 0.1), (int)(this.resources.get(r) * 0.5));
                        lostResourceCount += lostAmount;
                        this.resources.decrease(r, lostAmount);
                    }
                }

                if(lostResourceCount > 0) this.updateResources();

                final String lostSummary = this.getName() + " was defeated and lost %s Gold, %s Loot Items, and %s Resources!".formatted(lostGold, lostLoot.size(), lostResourceCount);
                Mongo.PlayerData.find().forEach(d -> {
                    PlayerDataQuery p = new PlayerDataQuery(d.getString("playerID"));
                    if(p.getCharacterList().contains(this.getCharacterID())) p.DM(lostSummary);
                });
            });
        }
    }

    public void heal(int amount)
    {
        this.setHealth(this.getHealth() + amount);
    }

    public boolean isDefeated()
    {
        return this.getHealth() <= 0;
    }

    //Loot
    public void setLoot(List<String> loot)
    {
        this.loot = loot;
    }

    public void setLoot()
    {
        this.loot = new ArrayList<>();
    }

    public void addLoot(String loot)
    {
        if(this.getLoot().size() >= this.getMaxLootAmount())
        {
            Executors.newSingleThreadExecutor().execute(() -> {
                LootItem.delete(loot);

                Mongo.PlayerData.find().forEach(d -> {
                    PlayerDataQuery p = new PlayerDataQuery(d.getString("playerID"));
                    if(p.getCharacterList().contains(this.getCharacterID())) p.DM(this.getName() + " is out of inventory space! Any newly acquired Loot will be permanently deleted!");
                });
            });
        }
        else this.loot.add(loot);
    }

    public void removeLoot(String... loot)
    {
        this.loot.removeAll(List.of(loot));
    }

    public List<String> getLoot()
    {
        return this.loot;
    }

    public int getMaxLootAmount()
    {
        return this.level * 30;
    }

    //Gold
    public void setGold(int gold)
    {
        this.gold = gold;
    }

    public void setGold()
    {
        this.gold = 0;
    }

    public void addGold(int amount)
    {
        this.gold += amount;
    }

    public void removeGold(int amount)
    {
        this.gold = Math.max(0, this.gold - amount);
    }

    public int getGold()
    {
        return this.gold;
    }

    //Raw Resources
    public RPGResourceContainer getResources()
    {
        return this.resources;
    }

    public void setResources()
    {
        this.resources = new RPGResourceContainer();
    }

    public void setResources(Document resources)
    {
        this.resources = new RPGResourceContainer(resources);
    }

    //Core Elemental Defense
    public void setCoreElementalDefense(Document defense)
    {
        this.coreElementalDefense = new RPGElementalContainer(defense);
    }

    public void setCoreElementalDefense()
    {
        this.coreElementalDefense = new RPGElementalContainer();
    }

    public RPGElementalContainer getCoreElementalDefense()
    {
        return this.coreElementalDefense;
    }

    //Core Elemental Damage
    public void setCoreElementalDamage(Document damage)
    {
        this.coreElementalDamage = new RPGElementalContainer(damage);
    }

    public void setCoreElementalDamage()
    {
        this.coreElementalDamage = new RPGElementalContainer();
    }

    public RPGElementalContainer getCoreElementalDamage()
    {
        return this.coreElementalDamage;
    }

    //Spells
    public List<Spell> getSpells()
    {
        return this.spells.stream().map(SpellData::fromID).collect(Collectors.toList());
    }

    public Spell getSpell(int index)
    {
        return SpellData.fromID(this.spells.get(index));
    }

    public void addSpell(String spellID)
    {
        this.spells.add(spellID);
    }

    public void removeSpell(String spellID)
    {
        this.spells.remove(spellID);
    }

    public boolean knowsSpell(String spellID)
    {
        return this.spells.contains(spellID);
    }

    public List<SpellData> availableSpells()
    {
        //TODO: Should include owned spells only
        List<SpellData> out = new ArrayList<>();
        for(SpellData spell : SpellData.values()) if(spell.getRequirements().check(this)) out.add(spell);
        return out;
    }

    public void setSpells(List<String> spells)
    {
        this.spells = spells;
    }

    public void setSpells()
    {
        this.spells = new ArrayList<>();
        this.spells.add(SpellData.STRIKE.getID());
    }

    //Equipment
    public RPGEquipment getEquipment()
    {
        return this.equipment;
    }

    public void setEquipment()
    {
        this.equipment = new RPGEquipment();
    }

    public void setEquipment(Document equipment)
    {
        this.equipment = new RPGEquipment(equipment);
    }

    public void equipLoot(EquipmentType type, String lootID)
    {
        this.equipment.setEquipmentID(type, lootID);
    }

    public LinkedHashMap<Stat, Integer> getEquipmentBoosts()
    {
        LinkedHashMap<Stat, Integer> boosts = new LinkedHashMap<>();

        for(EquipmentType type : EquipmentType.values())
        {
            LootItem loot = this.equipment.getEquipmentLoot(type);
            if(!loot.isEmpty()) for(Stat s : Stat.values()) boosts.put(s, boosts.getOrDefault(s, 0) + loot.getBoost(s));
        }

        return boosts;
    }

    //Skill Level & Exp
    public LinkedHashMap<GatheringSkill, Integer> getSkillExperience()
    {
        return this.skillExperience;
    }

    public LinkedHashMap<GatheringSkill, Integer> getSkillLevels()
    {
        return this.skillLevels;
    }

    public int getSkillExp(GatheringSkill skill)
    {
        return this.skillExperience.get(skill);
    }

    public int getSkillLevel(GatheringSkill skill)
    {
        return this.skillLevels.get(skill);
    }

    public int getRequiredSkillExp(int targetLevel)
    {
        return (int)(Math.pow(targetLevel, 2.1) + 100);
    }

    public void addSkillExp(GatheringSkill skill, int amount)
    {
        this.skillExperience.put(skill, this.skillExperience.get(skill) + amount);

        int required = this.getRequiredSkillExp(this.getSkillLevel(skill) + 1);
        while(this.skillExperience.get(skill) >= required)
        {
            this.skillExperience.put(skill, this.skillExperience.get(skill) - required);
            this.increaseSkillLevel(skill);

            required = this.getRequiredSkillExp(this.getSkillLevel(skill) + 1);
        }
    }

    public void increaseSkillLevel(GatheringSkill skill)
    {
        this.skillLevels.put(skill, this.getSkillLevel(skill) + 1);
    }

    public void setSkillLevel()
    {
        this.skillLevels = new LinkedHashMap<>();
        for(GatheringSkill s : GatheringSkill.values()) this.skillLevels.put(s, 1);
    }

    public void setSkillExperience()
    {
        this.skillExperience = new LinkedHashMap<>();
        for(GatheringSkill s : GatheringSkill.values()) this.skillExperience.put(s, 0);
    }

    public void setSkillLevel(Document data)
    {
        this.skillLevels = new LinkedHashMap<>();
        for(GatheringSkill s : GatheringSkill.values()) this.skillLevels.put(s, data.getInteger(s.toString()));
    }

    public void setSkillExperience(Document data)
    {
        this.skillExperience = new LinkedHashMap<>();
        for(GatheringSkill s : GatheringSkill.values()) this.skillExperience.put(s, data.getInteger(s.toString()));
    }

    //Stats - Effective

    public int getStat(Stat s)
    {
        int core = this.getCoreStat(s);
        int loot = this.getEquipmentBoosts().getOrDefault(s, 0);

        int stat;

        //Stat Calculation from Loot and Core
        if(s.equals(Stat.HEALTH)) stat = 10 + (core * 5) + loot;
        else stat = core + loot;

        //RPG Class Stat Modifier
        stat *= this.classRPG.getModifiers().getOrDefault(s, 1.0f);

        //Battle: Any stat changes from Spells
        stat += this.getChanges().getOrDefault(s, 0);

        return stat;
    }

    //Stats - Core
    public int getCoreStat(Stat s)
    {
        return this.stats.get(s);
    }

    private void setCoreStats(Document data)
    {
        for(Stat s : Stat.values()) this.stats.put(s, data.getInteger(s.toString()));
    }

    private void setCoreStats()
    {
        for(Stat s : Stat.values()) this.stats.put(s, 0);
    }

    public void increaseCoreStat(Stat s, int amount)
    {
        this.stats.put(s, this.stats.get(s) + amount);
    }

    //Experience
    public int getExp()
    {
        return this.experience;
    }

    public void setExp(int exp)
    {
        this.experience = exp;
    }

    public void addExp(int amount)
    {
        this.experience += amount;

        while(this.experience >= this.getExpRequired(this.level + 1))
        {
            this.experience -= this.getExpRequired(this.level + 1);
            this.level++;

            this.onLevelUp();
        }
    }

    private void onLevelUp()
    {
        final Random r = new Random();
        final StringBuilder message = new StringBuilder(this.getName() + " is now Level " + this.getLevel()).append("\nRewards Earned:\n");

        //Grant Core Stat
        int total = this.getLevel();
        List<Stat> coreStatPool = new ArrayList<>(Arrays.asList(Stat.values()));

        //TODO: More weight based on value
        if(!this.getRPGClass().equals(RPGClass.RECRUIT)) coreStatPool.addAll(this.getRPGClass().getModifiers().keySet());

        coreStatPool.removeIf(s -> s.equals(Stat.HEALTH));

        Map<Stat, Integer> coreImprovements = new HashMap<>();
        while(total > 0)
        {
            Stat s = coreStatPool.get(r.nextInt(coreStatPool.size()));
            this.stats.put(s, this.stats.get(s) + 1);

            coreImprovements.put(s, coreImprovements.getOrDefault(s, 0) + 1);
            total--;
        }

        message.append("Core Stats Improved!\n");
        for(Map.Entry<Stat, Integer> e : coreImprovements.entrySet()) message.append("- ").append(Global.normalize(e.getKey().toString())).append(": ").append(e.getValue()).append("\n");

        //Health

        this.stats.put(Stat.HEALTH, this.stats.get(Stat.HEALTH) + 2);

        message.append("Health improved to %s!\n".formatted(this.getStat(Stat.HEALTH)));

        //Grant Elemental Core Stat
        if(this.getLevel() % 20 == 0)
        {
            ElementType damage = ElementType.values()[r.nextInt(ElementType.values().length)];
            ElementType defense = ElementType.values()[r.nextInt(ElementType.values().length)];

            this.getCoreElementalDamage().increase(damage, 1 + this.getLevel() / 20);
            this.getCoreElementalDefense().increase(defense, 1 + this.getLevel() / 20);

            message
                    .append("Core ").append(Global.normalize(damage.toString())).append(" Damage & Core ")
                    .append(Global.normalize(defense.toString())).append(" Defense Improved by ")
                    .append(1 + this.getLevel() / 20).append("!\n");
        }

        Executors.newSingleThreadExecutor().execute(() -> Mongo.PlayerData.find().forEach(d -> {
            if(d.getList("characters", String.class).contains(this.getCharacterID())) new PlayerDataQuery(d.getString("playerID")).DM(message.toString());
        }));

        //LoggerHelper.info(this.getClass(), "Level Up Event (" + this.getCharacterID() + ")\n" + message);
    }

    public int getExpRequired(int targetLevel)
    {
        //base * rate^(targetLevel - 1)
        int base = 175;
        double rate = 1.08;

        double required = base * Math.pow(rate, targetLevel - 1);
        return (int)required;
    }

    //Level
    public int getLevel()
    {
        return this.level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    //Class
    public RPGClass getRPGClass()
    {
        return this.classRPG;
    }

    public void setRPGClass(RPGClass classRPG)
    {
        this.classRPG = classRPG;
    }

    //Name
    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    //Character ID
    public String getCharacterID()
    {
        return this.characterID;
    }

    public void setCharacterID(String characterID)
    {
        this.characterID = characterID;
        this.query = Filters.eq("characterID", characterID);
    }

    private void setCharacterID()
    {
        final StringBuilder s = new StringBuilder();
        final String pool = "abcdefghiklmnopqrstuvwxyz0123456789";
        final Random r = new Random();
        for(int i = 0; i < 32; i++) s.append(pool.charAt(r.nextInt(pool.length())));
        this.setCharacterID(s.toString());
    }

    //toString
    @Override
    public String toString()
    {
        return "Character {characterID: %s, name: %s}: \n".formatted(this.characterID, this.name) +
                "RPG Class – %s\n".formatted(this.classRPG.toString()) +
                "Core Stats – %s\n".formatted(this.stats.toString());
    }

    public String getSummary()
    {
        List<String> summary = new ArrayList<>();

        summary.add("Character Summary: **\"" + this.getName() + "\"**(" + this.getCharacterID() + ")");

        summary.add("Class: " + this.getRPGClass().getName() + " - " + this.getRPGClass().getDescription());

        summary.add("Level: " + this.getLevel());

        summary.add("Experience: " + this.getExp() + " / " + this.getExpRequired(this.getLevel() + 1) + " XP");

            StringBuilder spells = new StringBuilder();
            for(int i = 0; i < this.getSpells().size(); i++) spells.append(this.getSpell(i).getName()).append(", ");
            spells.deleteCharAt(spells.length() - 1).deleteCharAt(spells.length() - 1);

        summary.add("Current Spells: " + spells);

            StringBuilder equipment = new StringBuilder();

            for(EquipmentType e : EquipmentType.values())
            {
                LootItem loot = this.getEquipment().getEquipmentLoot(e);

                equipment.append("`").append(e.getStyledName()).append("`: ");

                if(loot.isEmpty()) equipment.append("None\n");
                else
                {
                    equipment.append(loot.getName());
                    if(!loot.getLootType().isArmor()) equipment.append(" (").append(Global.normalize(loot.getLootType().toString())).append(")");
                    equipment.append(" | Boosts: ").append(loot.getBoostsOverview()).append("\n");
                }
            }

            equipment.deleteCharAt(equipment.length() - 1);

        summary.add("Equipment:\n" + equipment);

        summary.add("Elemental Damage:\n" + this.getEquipment().combinedElementalDamage().getOverview());
        summary.add("Elemental Defense:\n" + this.getEquipment().combinedElementalDefense().getOverview());

            StringBuilder stats = new StringBuilder();

            for(Stat s : Stat.values()) stats.append(Global.normalize(s.toString())).append(": ").append(this.getStat(s)).append("\n");

            stats.deleteCharAt(stats.length() - 1);

            summary.add("Stats:\n" + stats);

        StringBuilder out = new StringBuilder();
        for(String s : summary) out.append(s).append("\n");
        return out.deleteCharAt(out.length() - 1).toString();
    }
}