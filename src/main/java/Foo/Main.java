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
            message = message.substring(1);

            if (!isLocked || user.getId().equals(IDs.eywaID)) {
                generalCommandsHandler(event, message);

                for (Role role : event.getMember().getRoles()) {
                    if (role.getName().equals("dm")) {
                        dmCommandsHandler(event, message);
                    }
                }

                if (user.getId().equals(IDs.eywaID)) {
                    adminCommandsHandler(event, message);
                }
            }
            else {
                event.getChannel().sendMessage("Functions are temporarily disabled for now :c Try again later").queue();
            }
        }
    }


    /*
     * Returns true if a command was completed
     */
    private static boolean generalCommandsHandler(MessageReceivedEvent event, String message) {
        if (message.equalsIgnoreCase("help")) {
            String help = "Working commands {required} [optional]: \n"
                    + " - !ping - test bot is working\n"
                    + " - !gameTime - prints time of your next game session\n"
                    + " - !roll [quantity] d {die size} [modifier] - roll a die\n"
                    + " - !potion - drink a potion\n"
                    + " - !charHelp - lists working commands related to characters";
            event.getChannel().sendMessage(help).queue();
        }
        else if (message.equalsIgnoreCase("charHelp")) {
            String help = "Working character related commands {required} [optional]: \n"
                    + " - !newChar {name} [subrace] {race} {class} - create a character\n"
                    + " - !races - list of possible races\n"
                    + " - !classes - list of possible classes\n"
                    + " - !weapons - list of possible weapons\n"
                    + " - !changeWeapons {weapon} - change your character's weapon\n"
                    + " - !attack {victim} - have your character (must be created) attack your chosen victim"
                    + " >:]\n"
                    + " - !deleteChar - deletes your character";
            event.getChannel().sendMessage(help).queue();
        }
        else if (message.equals("ping")) {
            event.getChannel().sendMessage("Pong").queue();
        }
        else if (message.equals("gameTime")) {
            SessionTimes.getSessionTime(event.getMember(), event.getChannel());
        }
        else if (message.startsWith("roll")) {
            Roll.rollDieFromChatEvent(message.substring(4), event.getAuthor().getName(), event.getChannel());
        }
        else if (message.startsWith("potion")) {
            GrogList.drinkGrog(event.getAuthor().getName(), event.getChannel());
        }
        else if (message.startsWith("newChar")) {
            UsersCharacters.createUserCharacter(event.getChannel(), event.getAuthor().getIdLong(),
                                                message.substring(8)
            );
        }
        else if (message.equals("races")) {
            event.getChannel().sendMessage(Race.getRacesList()).queue();
        }
        else if (message.equals("classes")) {
            event.getChannel().sendMessage(Class_.getClassesList()).queue();
        }
        else if (message.equals("weapons")) {
            event.getChannel().sendMessage(Weapon.getWeaponsList()).queue();
        }
        else if (message.startsWith("attack")) {
            UsersCharacters.attack(event.getAuthor(), message.substring(7), event.getChannel());
        }
        else if (message.startsWith("changeWeapon")) {
            UsersCharacters.changeCharacterWeapon(event.getChannel(), event.getAuthor(), message.substring(14));
        }
        else if (message.equals("deleteChar")) {
            UsersCharacters.deleteCharacter(event.getChannel(), event.getAuthor().getIdLong());
        }
        else {
            return false;
        }

        return true;
    }


    /*
     * Returns true if a command was completed
     */
    private static boolean dmCommandsHandler(MessageReceivedEvent event, String message) {
        if (message.equalsIgnoreCase("dmHelp")) {
            event.getChannel().sendMessage(
                    " - !addSessionTime {HH:mm dd/M/yy z} - updates the next session time (see !dateFormat for help)\n"
                    + " - !dateFormat - shows what the above moon runes for date/time format mean"
            ).queue();
        }
        if (message.equalsIgnoreCase("dateFormat")) {
            event.getChannel().sendMessage(
                    "Dates should be in the form 'HH:mm dd/M/yy z'\n"
                            + "e.g. '16:00 21/8/17 BST'\n"
                            + "  or '16:00 21/8/17 GMT + 1' (spaces around '+' are important)"
            ).queue();
        }
        else if (message.startsWith("addSessionTime")) {
            SessionTimes.addSessionTime(event.getMember(), event.getChannel(), message.substring(15));
        }
        else {
            return false;
        }
        return true;
    }


    /*
     * Returns true if a command was completed
     */
    private static boolean adminCommandsHandler(MessageReceivedEvent event, String message) {
        if (message.equals("adminHelp")) {
            event.getChannel().sendMessage(
                    " - !addGame {game} - allows time sessions for a game to be added\n"
                            + " - !removeGame {game} - prevents time sessions for a game from being added\n"
                            + " - !lock - blocks commands from people other than Eywa (not in Junk Yard)\n"
                            + " - !unlock - Lets anyone use commands freely\n"
                            + " - !save - saves character info\n"
                            + " - !exit - runs !save then puts Juuzo to bed"
            ).queue();
        }
        else if (message.startsWith("addGame")) {
            SessionTimes.addGame(event.getChannel(), message.substring(8));
        }
        else if (message.startsWith("removeGame")) {
            SessionTimes.removeGame(event.getChannel(), message.substring(8));
        }
        else if (message.equals("lock")) {
            isLocked = true;
        }
        else if (message.equals("unlock")) {
            isLocked = false;
        }
        else if (message.equals("save")) {
            save();
        }
        else if (message.equals("exit")) {
            save();
            System.exit(0);
        }
        else {
            return false;
        }
        return true;
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
