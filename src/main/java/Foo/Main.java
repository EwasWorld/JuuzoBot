package main.java.Foo;


import main.java.CharacterBox.AttackBox.Attack;
import main.java.CharacterBox.AttackBox.Weapons;
import main.java.CharacterBox.Character;
import main.java.CharacterBox.ClassBox.Classes;
import main.java.CharacterBox.RaceBox.Races;
import main.java.CharacterBox.UsersCharacters;
import main.java.Grog.GrogList;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.List;



public class Main {
    public static JDA jda;
    private static boolean isLocked = true;


    public static void main(String[] args) {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(IDs.botToken);
        builder.setAutoReconnect(true);
        builder.setStatus(OnlineStatus.DO_NOT_DISTURB);


        try {
            jda = builder.buildBlocking();
        } catch (LoginException | InterruptedException | RateLimitedException e) {
            System.err.println(e);
        }

        jda.addEventListener(new CommandListener());

        // Load saved characters
        MessageChannel junkYardGeneral = jda.getGuildById(IDs.junkYardID).getTextChannelsByName("general", false)
                .get(0);
        load(junkYardGeneral);
    }


    private static class CommandListener extends ListenerAdapter {
        @Override
        public void onGuildMemberJoin(GuildMemberJoinEvent event) {
            super.onGuildMemberJoin(event);

            List<TextChannel> channels = event.getGuild().getTextChannelsByName("general", false);
            if (channels.size() == 1) {
                MessageChannel channel = channels.get(0);
                String user = event.getMember().getUser().getName();

                channel.sendMessage(String.format("Welcome @%s", user));
            }
        }


        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            super.onMessageReceived(event);

            // TODO: Next session time, note to self

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
                dmCommandsHandler(event, message);

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
                    + " - !changeWeapons - change your character's weapon\n"
                    + " - !attack {victim} - have your character (must be created) attack your chosen victim"
                    + " >:]";
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
            event.getChannel().sendMessage(Races.getRacesList()).queue();
        }
        else if (message.equals("classes")) {
            event.getChannel().sendMessage(Classes.getClassesList()).queue();
        }
        else if (message.equals("weapons")) {
            event.getChannel().sendMessage(Weapons.getWeaponsList()).queue();
        }
        else if (message.startsWith("attack")) {
            Attack.attack(event.getAuthor(), message.substring(7), event.getChannel());
        }
        else if (message.startsWith("changeWeapon")) {
            UsersCharacters.changeCharacterWeapon(event.getChannel(), event.getAuthor(), message.substring(14));
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
                    " - !addSessionTime {time/date} - updates the next session time (see !dateFormat for help)\n"
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
                    " - !lock - blocks commands from people other than Eywa (not in Junk Yard)\n"
                            + " - !unlock - Lets anyone use commands freely\n"
                            + " - !save - saves character info\n"
                            + " - !exit - runs !save then puts Juuzo to bed"
            ).queue();
        }
        else if (message.equals("lock")) {
            isLocked = true;
        }
        else if (message.equals("unlock")) {
            isLocked = false;
        }
        else if (message.equals("save")) {
            save(event.getChannel());
        }
        else if (message.equals("exit")) {
            save(event.getChannel());
            System.exit(0);
        }
        else if (message.startsWith("addGame")) {
            SessionTimes.addGame(event.getChannel(), message.substring(8));
        }
        else {
            return false;
        }
        return true;
    }


    private static void save(MessageChannel channel) {
        UsersCharacters.save(channel);
        SessionTimes.save(channel);

        channel.sendMessage("Saves Complete").queue();
    }


    private static void load(MessageChannel channel) {
        UsersCharacters.load(channel);
        SessionTimes.load(channel);

        channel.sendMessage("Load Complete").queue();
    }
}
