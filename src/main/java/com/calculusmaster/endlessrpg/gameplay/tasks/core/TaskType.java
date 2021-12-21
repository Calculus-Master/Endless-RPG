package com.calculusmaster.endlessrpg.gameplay.tasks.core;

import com.calculusmaster.endlessrpg.util.Global;
import org.bson.Document;

import java.util.function.Function;
import java.util.function.Supplier;

public enum TaskType
{
    DEFEAT_ENEMIES(DefeatEnemiesTask::new, DefeatEnemiesTask::new);

    private final Supplier<Task> creator;
    private final Function<Document, Task> builder;

    TaskType(Supplier<Task> creator, Function<Document, Task> builder)
    {
        this.creator = creator;
        this.builder = builder;
    }

    public Task get()
    {
        return this.creator.get();
    }

    public static Task get(Document serialized)
    {
        return TaskType.cast(serialized.getString("type")).builder.apply(serialized);
    }

    public static TaskType cast(String input)
    {
        return Global.castEnum(input, values());
    }
}
