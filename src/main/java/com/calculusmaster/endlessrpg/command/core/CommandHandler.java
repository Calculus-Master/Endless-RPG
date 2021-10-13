package com.calculusmaster.endlessrpg.command.core;

import com.calculusmaster.endlessrpg.command.activity.CommandAdventure;
import com.calculusmaster.endlessrpg.command.activity.CommandAttack;
import com.calculusmaster.endlessrpg.command.activity.CommandBattle;
import com.calculusmaster.endlessrpg.command.character.*;
import com.calculusmaster.endlessrpg.command.loot.CommandInventory;
import com.calculusmaster.endlessrpg.command.misc.CommandBalance;
import com.calculusmaster.endlessrpg.command.misc.CommandDeveloper;
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

        register("adventure")
                .setCommand(CommandAdventure::new);

        register("battle")
                .setCommand(CommandBattle::new);

        register("attack")
                .setCommand(CommandAttack::new);

        register("characters")
                .setCommand(CommandCharacters::new);

        register("select")
                .setCommand(CommandSelect::new);

        register("balance", "bal", "gold", "g")
                .setCommand(CommandBalance::new);

        register("remove")
                .setCommand(CommandRemove::new);

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
