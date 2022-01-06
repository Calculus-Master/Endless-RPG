package com.calculusmaster.endlessrpg.command.loot;

import com.calculusmaster.endlessrpg.command.core.Command;
import com.calculusmaster.endlessrpg.command.core.CommandInvalid;
import com.calculusmaster.endlessrpg.gameplay.character.RPGCharacter;
import com.calculusmaster.endlessrpg.gameplay.enums.LootType;
import com.calculusmaster.endlessrpg.gameplay.loot.LootBuilder;
import com.calculusmaster.endlessrpg.gameplay.loot.LootComponent;
import com.calculusmaster.endlessrpg.gameplay.loot.LootItem;
import com.calculusmaster.endlessrpg.util.Global;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CommandForge extends Command
{
    public CommandForge(MessageReceivedEvent event, String msg)
    {
        super(event, msg);
    }

    @Override
    public Command run()
    {
        List<String> rawInput = List.of(this.raw);
        List<String> input = List.of(this.msg);

        //r!forge --type <lootType> --components <components> --name <name>
        if(this.msg.length < 2) this.response = CommandInvalid.INVALID;
        else if(!input.contains("--type") || !input.contains("--components")) this.response = "You must include the loot type you want to forge and the components you want to use to forge it!";
        else
        {
            if(input.indexOf("--type") + 1 > input.size() || LootType.cast(input.get(input.indexOf("--type") + 1)) == null) return this.invalid("Invalid Loot Type!");

            LootType lootType = LootType.cast(input.get(input.indexOf("--type") + 1));

            List<Integer> componentsNumber = new ArrayList<>();
            for(int i = input.indexOf("--components") + 1; i < input.size() && !input.get(i).contains("--"); i++) if(this.isNumeric(i)) componentsNumber.add(this.getInt(i));
            componentsNumber = componentsNumber.stream().filter(i -> i > 0 && i <= this.playerData.getComponents().size()).map(i -> i - 1).distinct().toList();

            List<LootComponent> components = componentsNumber.stream().map(i -> this.playerData.getComponents().get(i)).map(LootComponent::build).toList();

            if(!lootType.getCraftingComponents().matches(components)) return this.invalid("Invalid Components! Check what components are required to craft a `" + Global.normalize(lootType.toString()) + "` using `r!craftinfo`");

            String name = null;
            if(input.contains("--name") && input.size() > input.indexOf("--name") + 1 && !input.get(input.indexOf("--name") + 1).contains("--"))
            {
                List<String> nameInput = new ArrayList<>();
                for(int i = input.indexOf("--name") + 1; i < input.size() && !input.get(i).contains("--"); i++) nameInput.add(rawInput.get(i));
                name = String.join(" ", nameInput);
            }

            RPGCharacter active = this.playerData.getActiveCharacter();

            LootItem crafted = LootBuilder.createCrafted(lootType, components, active.getLevel(), name);
            active.addLoot(crafted.getLootID());

            Executors.newSingleThreadExecutor().execute(() -> {
                crafted.upload();
                active.updateLoot();
            });

            for(LootComponent component : components) this.playerData.removeComponent(component.getComponentID());

            this.response = "You successfully crafted `" + crafted.getName() + "`!\n*Use* `r!lootinfo` *to check out its stats!*";
        }

        return this;
    }
}
