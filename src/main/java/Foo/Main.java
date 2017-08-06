package main.java.Foo;



import main.java.Grog.GrogList;
import main.java.RaceBox.*;
import com.google.gson.Gson;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static JDA jda;
    private static InformationWrapper informationWrapper = null;

    public static InformationWrapper getInformationWrapper() {
        return informationWrapper;
    }

    private static void setInformationWrapper(InformationWrapper informationWrapper) {
        if (Main.informationWrapper == null) {
            Main.informationWrapper = informationWrapper;
        }
    }

    public static void main(String[] args) {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken("MzM0Nzc2NjUxMzQ0NzczMTIw.DEgKHQ.UdhBjR1-KSzuZB5yPydIklito94");
        builder.setAutoReconnect(true);
        builder.setStatus(OnlineStatus.DO_NOT_DISTURB);

        try {
            jda = builder.buildBlocking();
            jda.addEventListener(new CommandListener());
        }
        catch (LoginException | InterruptedException | RateLimitedException e) {
            System.err.println(e);
        }
    }

    private static class CommandListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            String message = event.getMessage().getContent();
            if (!event.getAuthor().isBot() && message.startsWith("!")) {
                if (message.equals("!help")) {
                    event.getChannel().sendMessage("Working commands: \n" +
                            "!potion - drink a potion").queue();
                }
                else if (message.startsWith("roll", 1)) {
                    DiceRoller.roll(event);
                }
                else if (message.startsWith("newChar", 1)) {

                }
                else if (message.startsWith("potion", 1)) {
                    GrogList.drinkGrog(event);
                }
                else if (message.startsWith("attack", 1)) {

                }
            }
        }
    }
}
