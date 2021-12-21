package com.calculusmaster.endlessrpg.gameplay.tasks.core;

import org.bson.Document;

import java.util.SplittableRandom;

public class DefeatEnemiesTask extends Task
{
    private int targetEnemies;
    private int progressEnemies;

    public DefeatEnemiesTask()
    {
        super(TaskType.DEFEAT_ENEMIES);

        this.targetEnemies = new SplittableRandom().nextInt(40);
        this.progressEnemies = 0;
    }

    public DefeatEnemiesTask(Document data)
    {
        this();

        this.targetEnemies = data.getInteger("target");
        this.progressEnemies = data.getInteger("progress");
    }

    @Override
    public Document serialized()
    {
        return this.getBaseDocument()
                .append("target", this.targetEnemies)
                .append("progress", this.progressEnemies);
    }
}
