package com.calculusmaster.endlessrpg.command.core;

import com.calculusmaster.endlessrpg.command.activity.CommandAdventure;
import com.calculusmaster.endlessrpg.command.activity.CommandAttack;
import com.calculusmaster.endlessrpg.command.activity.CommandBattle;
import com.calculusmaster.endlessrpg.command.activity.CommandDungeon;
import com.calculusmaster.endlessrpg.command.character.*;
import com.calculusmaster.endlessrpg.command.economy.CommandBalance;
import com.calculusmaster.endlessrpg.command.economy.CommandBuy;
import com.calculusmaster.endlessrpg.command.economy.CommandShop;
import com.calculusmaster.endlessrpg.command.loot.CommandDelete;
import com.calculusmaster.endlessrpg.command.loot.CommandInventory;
import com.calculusmaster.endlessrpg.command.loot.CommandLootInfo;
import com.calculusmaster.endlessrpg.command.misc.CommandDeveloper;
import com.calculusmaster.endlessrpg.command.spell.CommandSpellInfo;
import com.calculusmaster.endlessrpg.command.spell.CommandSpells;
import com.calculusmaster.endlessrpg.command.world.CommandGather;
import com.calculusmaster.endlessrpg.command.world.CommandLocation;
import com.calculusmaster.endlessrpg.command.world.CommandResources;
import com.calculusmaster.endlessrpg.command.world.CommandTravel;
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

        register("lootinfo", "li")
                .setCommand(CommandLootInfo::new);

        register("delete", "salvage")
                .setCommand(CommandDelete::new);

        register("spellinfo", "si")
                .setCommand(CommandSpellInfo::new);

        register("shop")
                .setCommand(CommandShop::new);

        register("buy")
                .setCommand(CommandBuy::new);

        register("spells")
                .setCommand(CommandSpells::new);

        register("location", "loc")
                .setCommand(CommandLocation::new);

        register("travel", "go")
                .setCommand(CommandTravel::new);

        register("resources")
                .setCommand(CommandResources::new);

        register("gather")
                .setCommand(CommandGather::new);

        register("dungeon")
                .setCommand(CommandDungeon::new);

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
