package com.calculusmaster.endlessrpg.gameplay.tasks.core;

import com.calculusmaster.endlessrpg.gameplay.tasks.Quest;
import org.bson.Document;

public abstract class Task
{
    private TaskType type;
    protected boolean isComplete;
    protected Quest quest;
    protected int level;

    public Task(TaskType type)
    {
        this.type = type;
        this.isComplete = false;
    }

    //Abstract Methods
    public abstract Document serialized();

    //Helper
    protected Document getBaseDocument()
    {
        return new Document()
                .append("type", this.type.toString())
                .append("completed", this.isComplete);
    }

    //Accessors
    public void setQuest(Quest quest)
    {
        this.quest = quest;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public boolean isComplete()
    {
        return this.isComplete;
    }

    public void complete()
    {
        this.isComplete = true;
    }

    public TaskType getType()
    {
        return this.type;
    }
}
