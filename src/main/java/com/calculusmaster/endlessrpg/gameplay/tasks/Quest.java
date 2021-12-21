package com.calculusmaster.endlessrpg.gameplay.tasks;

import com.calculusmaster.endlessrpg.gameplay.tasks.core.Task;
import com.calculusmaster.endlessrpg.gameplay.tasks.core.TaskType;
import com.calculusmaster.endlessrpg.util.Mongo;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;

public class Quest
{
    private String questID;
    private String name;
    private QuestType type;
    private int level;
    private List<Task> tasks;

    private Quest()
    {
        this.tasks = new ArrayList<>();
    }

    public static Quest createDefault(QuestType type, int level)
    {
        Quest q = new Quest();

        q.setQuestID();
        q.setName("UNNAMED_QUEST"); //TODO: Quest Names
        q.setType(type);
        q.setLevel(level);
        q.createTasks();
        q.completeTaskSetup();

        return q;
    }

    public static Quest createBounty(Task task, int level)
    {
        Quest q = new Quest();

        q.setQuestID();
        q.setName("Bounty – "); //TODO: Bounty Quest Names
        q.setType(QuestType.RANDOM);
        q.setLevel(level);
        q.createTasks(task);
        q.completeTaskSetup();

        return q;
    }

    public static Quest build(String questID)
    {
        Document data = Objects.requireNonNull(Mongo.QuestData.find(Filters.eq("questID", questID)).first());

        Quest q = new Quest();

        q.setQuestID(questID);
        q.setName(data.getString("name"));
        q.setType(QuestType.cast(data.getString("type")));
        q.setLevel(data.getInteger("level"));
        q.setTasks(data.getList("tasks", Document.class));

        return q;
    }

    public void upload()
    {
        Document data = new Document()
                .append("questID", this.questID)
                .append("name", this.name)
                .append("type", this.type.toString())
                .append("level", this.level)
                .append("tasks", this.tasks.stream().map(Task::serialized).toList());

        Mongo.QuestData.insertOne(data);
    }

    public void delete()
    {
        Mongo.QuestData.deleteOne(Filters.eq("questID", this.questID));
    }

    //Accessors
    public boolean isComplete()
    {
        return this.tasks.stream().allMatch(Task::isComplete);
    }

    private void completeTaskSetup()
    {
        this.tasks.forEach(t -> t.setQuest(this));
        this.tasks.forEach(t -> t.setLevel(this.level)); //TODO: Variety of levels, etc? (Improved system?)
    }

    public void createTasks()
    {
        this.tasks.add(TaskType.DEFEAT_ENEMIES.get());
        this.tasks.add(TaskType.DEFEAT_ENEMIES.get());
    }

    public void createTasks(Task... tasks)
    {
        this.tasks = new ArrayList<>(List.of(tasks));
    }

    public void setTasks(List<Document> tasks)
    {
        this.tasks = new ArrayList<>(tasks.stream().map(TaskType::get).toList());
    }

    public List<Task> getTasks()
    {
        return this.tasks;
    }

    public int getLevel()
    {
        return this.level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public QuestType getType()
    {
        return this.type;
    }

    public void setType(QuestType type)
    {
        this.type = type;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getQuestID()
    {
        return this.questID;
    }

    public void setQuestID(String questID)
    {
        this.questID = questID;
    }

    public void setQuestID()
    {
        final StringBuilder s = new StringBuilder();
        final SplittableRandom r = new SplittableRandom();
        for(int i = 0; i < 6; i++) s.append(r.nextInt(10));
        this.questID = s.toString();
    }
}
