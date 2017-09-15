package CoreBox;


import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import ExceptionsBox.IncorrectPermissionsException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.reflections.Reflections;

import javax.security.auth.login.LoginException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;



public class Bot {
    private static String localFilePath = "src/main/java/";
    private static String pathToJuuzoBot = IDs.pathToJuuzoBot;
    private static String mainFilePath = pathToJuuzoBot + localFilePath;
    private static Map<String, AbstractCommand> commands = new HashMap<>();
    private static JDA jda;
    private static boolean isLocked = false;
    private static boolean isSessionReminderThreadStarted = false;


    public static void main(String[] args) {
        if (args.length != 0) {
            pathToJuuzoBot = args[0];
        }


        final JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(IDs.botToken);
        builder.setAutoReconnect(true);
        builder.setStatus(OnlineStatus.DO_NOT_DISTURB);


        try {
            jda = builder.buildBlocking();
        } catch (LoginException | InterruptedException | RateLimitedException e) {
            System.err.println(e);
        }

        jda.addEventListener(new CommandListener());
        DataPersistence.loadData();
        new Thread(new DataPersistence()).start();
        loadCommands();
    }


    private static void loadCommands() {
        Reflections reflections = new Reflections("CommandsBox");
        Set<Class<? extends AbstractCommand>> classes = reflections.getSubTypesOf(AbstractCommand.class);
        for (Class<? extends AbstractCommand> s : classes) {
            try {
                if (Modifier.isAbstract(s.getModifiers())) {
                    continue;
                }
                AbstractCommand c = s.getConstructor().newInstance();
                if (!commands.containsKey(c.getCommand())) {
                    commands.put(c.getCommand().toUpperCase(), c);
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                    NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }


    public static String getMainFilePath() {
        return mainFilePath;
    }


    public static String getPathToJuuzoBot() {
        return pathToJuuzoBot;
    }


    static boolean isIsLocked() {
        return isLocked;
    }


    public static void setIsLocked(boolean isLocked) {
        Bot.isLocked = isLocked;
    }


    public static Set<AbstractCommand> getCommands() {
        return new HashSet<>(commands.values());
    }



    private static class CommandListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            super.onMessageReceived(event);

//            if (!isSessionReminderThreadStarted) {
//                new Thread(new SessionReminder(event.getGuild())).start();
//                isSessionReminderThreadStarted = true;
//            }

            String args = event.getMessage().getContent();
            Quotes.addMessage(event.getAuthor().getName(), args);

            if (!args.startsWith("!") || event.getAuthor().isBot()) {
                return;
            }
            String command = args.substring(1).split(" ")[0].toUpperCase();
            args = getRemainingMessage(command, args);


            try {
                if (commands.size() != 0) {
                    if (commands.containsKey(command)) {
                        commands.get(command).execute(args, event);
                        // TODO: Note to self command
                    }
                    else {
                        throw new BadUserInputException("I have no memory of this command");
                    }
                }
                else {
                    throw new BadStateException("I'm so broken right now I just can't even");
                }
            } catch (BadUserInputException | BadStateException | IncorrectPermissionsException e) {
                Logger.logEvent(event.getMessage().getContent(), e);
                event.getChannel().sendMessage(e.getMessage()).queue();
            }
        }


        /*
         * Removes the command string from the start of the message and returns the remainder
         */
        private static String getRemainingMessage(String command, String message) {
            message = message.substring(1);
            if (!message.equalsIgnoreCase(command)) {
                return message.substring(command.length() + 1);
            }
            else {
                return "";
            }
        }


        @Override
        public void onGuildMemberJoin(GuildMemberJoinEvent event) {
            super.onGuildMemberJoin(event);

            final List<TextChannel> channels = event.getGuild().getTextChannelsByName("general", false);
            if (channels.size() == 1) {
                final MessageChannel channel = channels.get(0);
                channel.sendMessage(String.format("Welcome @%s", event.getMember().getUser().getId()));
            }
        }
    }
}
