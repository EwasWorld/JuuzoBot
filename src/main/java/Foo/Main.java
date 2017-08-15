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

            User user = event.getAuthor();
            if (user.isBot()) {
                return;
            }

            String message = event.getMessage().getContent();
            if (!message.startsWith("!")) {
                return;
            }
            String command = message.substring(1).split(" ")[0];
            message = getRemainingMessage(command, message);


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
                event.getChannel().sendMessage("Functions are temporarily disabled for now :c Try again later").queue();
            }
        }
    }


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
        switch (command) {
            case "help":
                event.getChannel().sendMessage(Help.help).queue();
                return true;
            case "charHelp":
                event.getChannel().sendMessage(Help.charHelp).queue();
                return true;
            case "ping":
                event.getChannel().sendMessage("Pong").queue();
                return true;
            case "gameTime":
                SessionTimes.getSessionTime(event.getMember(), event.getChannel());
                return true;
            case "roll":
                Roll.rollDieFromChatEvent(message, event.getAuthor().getName(), event.getChannel());
                return true;
            case "potion":
                GrogList.drinkGrog(event.getAuthor().getName(), event.getChannel());
                return true;
            case "newChar":
                UsersCharacters.createUserCharacter(event.getChannel(), event.getAuthor().getIdLong(), message);
                return true;
            case "description":
                UsersCharacters.printDescription(event.getAuthor().getIdLong(), event.getChannel());
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
                UsersCharacters.attack(event.getAuthor(), message, event.getChannel());
                return true;
            case "changeWeapon":
                UsersCharacters.changeCharacterWeapon(event.getChannel(), event.getAuthor(), message);
                return true;
            case "deleteChar":
                UsersCharacters.deleteCharacter(event.getChannel(), event.getAuthor().getIdLong());
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
            default:
                return false;
        }
    }


    /*
     * Returns true if a command was completed
     */
    private static boolean dmCommandsHandler(MessageReceivedEvent event, String command, String message) {
        switch (command) {
            case "dmHelp":
                event.getChannel().sendMessage(Help.help + "\n" + Help.dmHelp).queue();
                return true;
            case "dateFormat":
                event.getChannel().sendMessage(Help.dateFormatHelp).queue();
                return true;
            case "addSessionTime":
                SessionTimes.addSessionTime(event.getMember(), event.getChannel(), message);
                return true;
            default:
                return false;
        }
    }


    /*
     * Returns true if a command was completed
     */
    private static boolean adminCommandsHandler(MessageReceivedEvent event, String command, String message) {
        switch (command) {
            case "adminHelp":
                event.getChannel().sendMessage(Help.help + "\n" + Help.dmHelp + "\n" + Help.adminHelp).queue();
                return true;
            case "addGame":
                SessionTimes.addGame(event.getChannel(), message);
                return true;
            case "removeGame":
                SessionTimes.removeGame(event.getChannel(), message);
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
    }


    private static void save() {
        UsersCharacters.save();
        SessionTimes.save();

        System.out.println("Saves complete");
    }


    private static void load() {
        UsersCharacters.load();
        SessionTimes.load();

        System.out.println("Load complete");
    }
}
