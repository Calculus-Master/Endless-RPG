package com.calculusmaster.endlessrpg;

import com.calculusmaster.endlessrpg.command.core.CommandHandler;
import com.calculusmaster.endlessrpg.command.economy.CommandShop;
import com.calculusmaster.endlessrpg.gameplay.world.Realm;
import com.calculusmaster.endlessrpg.gameplay.world.UniqueLocations;
import com.calculusmaster.endlessrpg.util.Global;
import com.calculusmaster.endlessrpg.util.Listener;
import com.calculusmaster.endlessrpg.util.PrivateInfo;
import com.calculusmaster.endlessrpg.util.helpers.LoggerHelper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

import javax.security.auth.login.LoginException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EndlessRPG
{
    public static JDA BOT_JDA;
    public static Guild TESTING_SERVER;

    public static void main(String[] args) throws LoginException, InterruptedException
    {
        LoggerHelper.disableMongoLoggers();

        LoggerHelper.init("Command", CommandHandler::init);
        LoggerHelper.init("Shop", CommandShop::init);
        LoggerHelper.init("Unique Locations", UniqueLocations::init);
        LoggerHelper.init("Realm", Realm::init, true);

        JDABuilder bot = JDABuilder
                .createDefault(PrivateInfo.TOKEN)
                .setActivity(Activity.playing("Endless RPG"))
                .addEventListeners(new Listener());

        BOT_JDA = bot.build().awaitReady();
        TESTING_SERVER = BOT_JDA.getGuildById("873993084155887617");

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(Global::optimizeLootDatabase, 0, 30, TimeUnit.MINUTES);
    }
}
