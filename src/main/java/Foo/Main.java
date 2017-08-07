package main.java.Foo;


import main.java.CharacterBox.ClassBox.Classes;
import main.java.CharacterBox.RaceBox.Races;
import main.java.CharacterBox.UsersCharacters;
import main.java.Grog.GrogList;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;



public class Main {
    public static JDA jda;


    public static void main(String[] args) {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken("MzM0Nzc2NjUxMzQ0NzczMTIw.DEgKHQ.UdhBjR1-KSzuZB5yPydIklito94");
        builder.setAutoReconnect(true);
        builder.setStatus(OnlineStatus.DO_NOT_DISTURB);

        try {
            jda = builder.buildBlocking();
            jda.addEventListener(new CommandListener());
        } catch (LoginException | InterruptedException | RateLimitedException e) {
            System.err.println(e);
        }
    }


    private static class CommandListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            String message = event.getMessage().getContent();
            if (!event.getAuthor().isBot() && message.startsWith("!")) {
                message = message.substring(1);
                if (message.equals("help")) {
                    event.getChannel().sendMessage("Working commands: \n" +
                                                           "!ping - test bot is working\n" +
                                                           "!potion - drink a potion").queue();
                }
                else if (message.equals("ping")) {
                    event.getChannel().sendMessage("Pong").queue();
                }
                else if (message.startsWith("roll")) {
                    DiceRoller.getStringForRoll(event);
                }
                else if (message.startsWith("newChar")) {
                    UsersCharacters.createUserCharacter(event.getChannel(), event.getAuthor().getIdLong(),
                                                        message.substring(8)
                    );
                }
                else if (message.equals("races")) {
                    event.getChannel().sendMessage(Races.getRacesList()).queue();
                }
                else if (message.equals("classes")) {
                    event.getChannel().sendMessage(Classes.getClassesList()).queue();
                }
                else if (message.startsWith("potion")) {
                    GrogList.drinkGrog(event);
                }
                else if (message.startsWith("attack")) {

                }
            }
        }
    }
}
