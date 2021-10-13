package com.calculusmaster.endlessrpg;

import com.calculusmaster.endlessrpg.command.core.CommandHandler;
import com.calculusmaster.endlessrpg.util.Listener;
import com.calculusmaster.endlessrpg.util.PrivateInfo;
import com.calculusmaster.endlessrpg.util.helpers.LoggerHelper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

import javax.security.auth.login.LoginException;

public class EndlessRPG
{
    public static JDA BOT_JDA;
    public static Guild TESTING_SERVER;

    public static void main(String[] args) throws LoginException, InterruptedException
    {
        LoggerHelper.disableMongoLoggers();

        LoggerHelper.init("Command", CommandHandler::init);

        JDABuilder bot = JDABuilder
                .createDefault(PrivateInfo.TOKEN)
                .setActivity(Activity.playing("Endless RPG"))
                .addEventListeners(new Listener());

        BOT_JDA = bot.build().awaitReady();
        TESTING_SERVER = BOT_JDA.getGuildById("873993084155887617");
    }
}
