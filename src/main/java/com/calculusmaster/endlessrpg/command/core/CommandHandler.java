package com.calculusmaster.endlessrpg.command.core;

import com.calculusmaster.endlessrpg.command.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandHandler
{
    public static final List<CommandRegistry> COMMANDS = new ArrayList<>();

    public static void init()
    {
        register("create")
                .setCommand(CommandCreate::new);

        register("info")
                .setCommand(CommandInfo::new);

        register("inventory", "inv")
                .setCommand(CommandInventory::new);

        register("equip")
                .setCommand(CommandEquip::new);

        register("dev")
                .setCommand(CommandDeveloper::new);
    }

    public static void parse(MessageReceivedEvent event, String msg)
    {
        Command c = null;
        for(CommandRegistry registry : COMMANDS) if(registry.matches(msg.toLowerCase().split("\\s+")[0])) c = registry.command.get(event, msg);

        if(c == null) c = new CommandInvalid(event, msg);

        c.run().send();
    }

    private static CommandRegistry register(String... aliases)
    {
        CommandRegistry registry = new CommandRegistry(aliases);

        COMMANDS.add(registry);
        return registry;
    }

    private static class CommandRegistry
    {
        private List<String> aliases;
        private CommandSupplier command;

        CommandRegistry(String... aliases)
        {
            this.aliases = new ArrayList<>(Arrays.asList(aliases));
        }

        CommandRegistry setCommand(CommandSupplier command)
        {
            this.command = command;
            return this;
        }

        boolean matches(String cmd)
        {
            return this.aliases.stream().anyMatch(cmd::equalsIgnoreCase);
        }
    }

    private interface CommandSupplier
    {
        Command get(MessageReceivedEvent event, String msg);
    }
}
