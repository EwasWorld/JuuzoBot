package main.java.Foo;


import main.java.CharacterBox.AttackBox.Attack;
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
    private static boolean isLocked = false;


    public static void main(String[] args) {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(IDs.botToken);
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

            User user = event.getAuthor();
            String message = event.getMessage().getContent();
            if (!user.isBot() && message.startsWith("!")) {
                message = message.substring(1);

                if (!isLocked || user.getId().equals(IDs.eywaID) || event.getGuild().getId().equals(IDs.junkYardID)) {
                    if (message.equalsIgnoreCase("help")) {
                        String help = "Working commands {required} [optional]: \n"
                                + "  !ping - test bot is working\n"
                                + "  !roll [quantity] d {die size} [modifier] - roll a die\n"
                                + "  !potion - drink a potion\n"
                                + "  !charHelp - lists working commands related to characters";
                        event.getChannel().sendMessage(help).queue();
                    }
                    else if (message.equalsIgnoreCase("charHelp")) {
                        String help = "Working character related commands {required} [optional]: \n"
                                + "  !newChar {name} [subrace] {race} {class} - create a character\n"
                                + "  !races - list of possible races\n"
                                + "  !classes - list of possible classes\n"
                                + "  !attack {victim} - have your character (must be created) attack your chosen victim"
                                + " >:]";
                        event.getChannel().sendMessage(help).queue();
                    }
                    else if (message.equals("ping")) {
                        event.getChannel().sendMessage("Pong").queue();
                    }
                    else if (message.startsWith("roll")) {
                        Roll.rollDieFromChatEvent(message.substring(4), event.getAuthor().getName(), event.getChannel());
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
                        GrogList.drinkGrog(event.getAuthor().getName(), event.getChannel());
                    }
                    else if (message.startsWith("attack")) {
                        Attack.attack(event.getAuthor(), message.substring(7), event.getChannel());
                    }

                    if (user.getId().equals(IDs.eywaID)) {
                        if (message.equals("lock")) {
                            isLocked = true;
                        }
                        if (message.equals("unlock")) {
                            isLocked = false;
                        }
                    }
                }
                else if (isLocked) {
                    event.getChannel().sendMessage("Functions are temporarily disabled for now :c Try again later").queue();
                }
            }
        }
    }
}
