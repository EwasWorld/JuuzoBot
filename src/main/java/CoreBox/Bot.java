package CoreBox;


import BlackJackBox.GameInstance;
import CommandsBox.BlackJackCommand;
import CommandsBox.CharacterCommand;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import ExceptionsBox.IncorrectPermissionsException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.reflections.Reflections;

import javax.security.auth.login.LoginException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;


public class Bot {
    // Database and other information is stored in this location
    private static String pathToJuuzoBot = IDs.pathToJuuzoBot;
    // Temporary file path while I figure out how to get the ideal one (commented out underneath) working
    private static String resourceFilePath = "resources/";
    //    private static String resourceFilePath = pathToJuuzoBot + "src/main/resources/";
    private static Map<String, AbstractCommand> commands = new HashMap<>();
    // Prevents anyone other than me from using the bot
    private static boolean isLocked = false;


    public static void main(String[] args) {
        // Change the path to the specified one rather than using the default one
        if (args.length != 0) {
            pathToJuuzoBot = args[0];
            resourceFilePath = pathToJuuzoBot + resourceFilePath;
        }
        startJDA();
        DataPersistence.loadData();
        new Thread(new DataPersistence()).start();
        loadCommands();
    }


    /*
     * Turns the bot online in discord
     */
    private static void startJDA() {
        final JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(IDs.botToken);
        builder.setAutoReconnect(true);
        builder.setStatus(OnlineStatus.DO_NOT_DISTURB);
        try {
            final JDA jda = builder.build();
            jda.addEventListener(new CommandListener());
        } catch (LoginException e) {
            System.err.println(e);
        }
    }


    /*
     * Instantiate each command from the CommandsBox and add it to the commands map
     */
    private static void loadCommands() {
        final Reflections reflections = new Reflections("CommandsBox");
        final Set<Class<? extends AbstractCommand>> classes = reflections.getSubTypesOf(AbstractCommand.class);
        for (Class<? extends AbstractCommand> s : classes) {
            try {
                if (Modifier.isAbstract(s.getModifiers())) {
                    continue;
                }
                final AbstractCommand c = s.getConstructor().newInstance();
                if (!commands.containsKey(c.getCommand())) {
                    commands.put(c.getCommand().toUpperCase(), c);
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                    NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }


    public static String getPathToJuuzoBot() {
        return pathToJuuzoBot;
    }


    public static String getResourceFilePath() {
        return resourceFilePath;
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
        public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
            super.onGuildMessageReactionAdd(event);
            if (!event.getMember().getUser().isBot()) {
                if (BlackJackCommand.executeFromAddReaction(event.getMessageIdLong(), event.getReaction(), event.getMember())) {
                    return;
                }
                else if (CharacterCommand.executeFromAddReaction(event.getMessageIdLong(), event.getReaction(), event.getMember())) {
                    return;
                }
            }
        }


        @Override
        public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
            super.onGuildMessageReactionRemove(event);
            if (!event.getMember().getUser().isBot()) {
                if (BlackJackCommand.executeFromRemoveReaction(event.getMessageIdLong(), event.getReactionEmote().getName(), event.getMember())) {
                    return;
                }
            }
        }


        /*
         * Logs messages for use with Quotes
         *      then if the message begins with '!' and a known command it executes the command
         */
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            super.onMessageReceived(event);
            if (event.isFromType(ChannelType.PRIVATE)) {
                // TODO: here are PMs
            }

            String args = event.getMessage().getContentRaw();
            // Logs the message in case it will be quoted later
            Quotes.addMessage(event.getAuthor().getName(), args);
            if (event.getAuthor().isBot()) {
                if (args.startsWith(GameInstance.blackjackGameMessageTitleString)) {
                    BlackJackCommand.setCurrentGamesChannelMessage(event.getMessage());
                }
                else {
                    CharacterCommand.addMessage(event.getMessage());
                }
                return;
            }
            else if (!args.startsWith("!")) {
                return;
            }
            final String command = args.substring(1).split(" ")[0].toUpperCase();
            args = getRemainingMessage(command, args);

            try {
                if (commands == null || commands.size() == 0) {
                    throw new BadStateException("I'm so broken right now I just can't even");
                }
                if (!commands.containsKey(command)) {
                    throw new BadUserInputException("I have no memory of this command");
                }
                commands.get(command).execute(args, event);
            } catch (BadUserInputException | BadStateException | IncorrectPermissionsException e) {
                event.getChannel().sendMessage(e.getMessage()).queue();
            } catch (Exception e) {
                // Log unexpected errors
                e.printStackTrace();
                Logger.logEvent(event.getMessage().getContentRaw(), e);
            }
        }


        /*
         * Removes the command string and the '!' from the start of the message and returns the remainder
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


        /*
         * Displays a welcome message when a new member joins the server
         * TODO Implement this doesn't activate when a new member joins
         */
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
