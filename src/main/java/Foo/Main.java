package Foo;


import CharacterBox.AttackBox.Weapon;
import CharacterBox.ClassBox.Class_;
import CharacterBox.RaceBox.Race;
import CharacterBox.UsersCharacters;
import Grog.GrogList;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.List;



public class Main {
    public static JDA jda;
    private static boolean isLocked = false;


    public static void main(String[] args) {
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
        load();
    }


    private static class CommandListener extends ListenerAdapter {
        @Override
        public void onGuildMemberJoin(GuildMemberJoinEvent event) {
            super.onGuildMemberJoin(event);

            final List<TextChannel> channels = event.getGuild().getTextChannelsByName("general", false);
            if (channels.size() == 1) {
                final MessageChannel channel = channels.get(0);
                channel.sendMessage(String.format("Welcome @%s", event.getMember().getUser().getId()));
            }
        }


        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            super.onMessageReceived(event);
            // TODO: Note to self

            String message = event.getMessage().getContent();
            if (!message.startsWith("!")) {
                Quotes.addMessage(event.getAuthor().getName(), message);
                return;
            }
            String command = message.substring(1).split(" ")[0];
            message = getRemainingMessage(command, message);


            User user = event.getAuthor();
            if (user.isBot()) {
                return;
            }


            if (!isLocked || user.getId().equals(IDs.eywaID)) {
                generalCommandsHandler(event, command, message);

                for (Role role : event.getMember().getRoles()) {
                    if (role.getName().equals("dm")) {
                        dmCommandsHandler(event, command, message);
                    }
                }

                if (user.getId().equals(IDs.eywaID)) {
                    adminCommandsHandler(event, command, message);
                }
            }
            else {
                event.getChannel().sendMessage(
                        "Functions are temporarily disabled for now :c Try again later"
                ).queue();
            }
        }
    }


    /*
     * Removes the command string from the start of the message and returns the remainder
     */
    private static String getRemainingMessage(String command, String message) {
        message = message.substring(1);
        if (!message.equals(command)) {
            return message.substring(command.length() + 1);
        }
        else {
            return "";
        }
    }


    /*
     * Returns true if a command was completed
     */
    private static boolean generalCommandsHandler(MessageReceivedEvent event, String command, String message) {
        try {
            switch (command) {
                case "help":
                    event.getChannel().sendMessage(Help.getHelp()).queue();
                    return true;
                case "charHelp":
                    event.getChannel().sendMessage(Help.charHelp).queue();
                    return true;
                case "ping":
                    event.getChannel().sendMessage("Pong").queue();
                    return true;
                case "gameTime":
                    event.getChannel().sendMessage(
                            SessionTimes.getNextSessionAsString(event.getMember())
                    ).queue();
                    return true;
                case "roll":
                    event.getChannel().sendMessage(
                            Roll.rollDieFromChatEvent(message, event.getAuthor().getName())
                    ).queue();
                    return true;
                case "potion":
                    event.getChannel().sendMessage(
                            GrogList.drinkGrog(event.getAuthor().getName())
                    ).queue();
                    return true;
                case "newChar":
                    event.getChannel().sendMessage(
                            UsersCharacters.createUserCharacter(event.getAuthor().getIdLong(), message)
                    ).queue();
                    return true;
                case "description":
                    event.getChannel().sendMessage(
                            UsersCharacters.getCharacterDescription(event.getAuthor().getIdLong())
                    ).queue();
                    return true;
                case "races":
                    event.getChannel().sendMessage(Race.getRacesList()).queue();
                    return true;
                case "classes":
                    event.getChannel().sendMessage(Class_.getClassesList()).queue();
                    return true;
                case "weapons":
                    event.getChannel().sendMessage(Weapon.getWeaponsList()).queue();
                    return true;
                case "attack":
                    event.getChannel().sendMessage(
                            UsersCharacters.attack(event.getAuthor(), message)
                    ).queue();
                    return true;
                case "changeWeapon":
                    event.getChannel().sendMessage(
                            UsersCharacters.changeCharacterWeapon(event.getAuthor().getIdLong(), message)
                    ).queue();
                    return true;
                case "deleteChar":
                    event.getChannel().sendMessage(
                            UsersCharacters.deleteCharacter(event.getAuthor().getIdLong())
                    ).queue();
                    return true;
                case "addQuote":
                    event.getChannel().sendMessage(
                            Quotes.addQuote(message)
                    ).queue();
                    return true;
                case "getQuote":
                    if (message.equals("")) {
                        event.getChannel().sendMessage(
                                Quotes.getQuote()
                        ).queue();
                    }
                    else {
                        try {
                            event.getChannel().sendMessage(
                                    Quotes.getQuote(Integer.parseInt(message))
                            ).queue();
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException(
                                    "Incorrect quote format, either give no argument or an integer");
                        }
                    }
                    return true;
                case "confetti":
                    event.getChannel().sendMessage(
                            " :tada:  :tada:  :tada:  :tada:  :tada:  :tada:  :tada:  :tada:  :tada:  :tada: "
                                    + " :tada:  :tada:  :tada:  :tada:  :tada:  :tada:  :tada:  :tada:  :tada:  :tada: "
                    ).queue();
                    return true;
                case "fancify":
                    event.getChannel().sendMessage("But... but... I'm already fancy af").queue();
                    return true;
                case "complaints":
                    event.getChannel().sendMessage(
                            "You may kindly take your complaints and insert them into your anal cavity "
                                    + "making sure to use plenty of lube."
                    ).queue();
                    return true;
                default:
                    return false;
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            event.getChannel().sendMessage(e.getMessage()).queue();
            return true;
        }
    }


    /*
     * Returns true if a command was completed
     */
    private static boolean dmCommandsHandler(MessageReceivedEvent event, String command, String message) {
        try {
            switch (command) {
                case "dmHelp":
                    event.getChannel().sendMessage(Help.getdmHelp()).queue();
                    return true;
                case "dateFormat":
                    event.getChannel().sendMessage(Help.dateFormatHelp).queue();
                    return true;
                case "addSessionTime":
                    event.getChannel().sendMessage(
                            SessionTimes.addSessionTime(event.getMember(), message)
                    ).queue();
                    return true;
                case "gameReminder":
                    event.getChannel().sendMessage(
                            SessionTimes.getSessionReminder(event.getMember())
                    ).queue();
                    return true;
                default:
                    return false;
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            event.getChannel().sendMessage(e.getMessage()).queue();
            return true;
        }
    }


    /*
     * Returns true if a command was completed
     */
    private static boolean adminCommandsHandler(MessageReceivedEvent event, String command, String message) {
        try {
            switch (command) {
                case "adminHelp":
                    event.getChannel().sendMessage(Help.getadminHelp()).queue();
                    return true;
                case "addGame":
                    event.getChannel().sendMessage(
                            SessionTimes.addGame(message)
                    ).queue();
                    return true;
                case "removeGame":
                    event.getChannel().sendMessage(
                            SessionTimes.removeGame(message)
                    ).queue();
                    return true;
                case "removeQuote":
                    try {
                        event.getChannel().sendMessage(
                                Quotes.removeQuote(Integer.parseInt(message))
                        ).queue();
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Incorrect quote number - it needs to be an integer");
                    }
                    return true;
                case "lock":
                    isLocked = true;
                    return true;
                case "unlock":
                    isLocked = false;
                    return true;
                case "save":
                    save();
                    return true;
                case "exit":
                    save();
                    System.exit(0);
                    return true;
                default:
                    return false;
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            event.getChannel().sendMessage(e.getMessage()).queue();
            return true;
        }
    }


    private static void save() {
        UsersCharacters.save();
        SessionTimes.save();
        Quotes.save();

        System.out.println("Saves complete");
    }


    private static void load() {
        UsersCharacters.load();
        SessionTimes.load();
        Quotes.load();

        System.out.println("Load complete");
    }
}
