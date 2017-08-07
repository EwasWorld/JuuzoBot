package main.java.CharacterBox;

import main.java.CharacterBox.ClassBox.Classes;
import main.java.CharacterBox.RaceBox.Races;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.util.HashMap;
import java.util.Map;



public class UsersCharacters {
    private static Map<Long, Character> userCharacters;


    public static void createUserCharacter(MessageChannel channel, long id, String creationString) {
        // TODO Temp for testing
        if (creationString.endsWith(" human fighter")) {
            if (userCharacters == null) {
                userCharacters = new HashMap<>();
            }

            final String[] creationParts = creationString.split(" ");
            if (creationParts.length != 3) {
                sendInvalidFormatMessage(channel);
            }

            final Races.RaceEnum race;
            final Classes.ClassEnum class_;
            try {
                race = Races.RaceEnum.valueOf(creationParts[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                channel.sendMessage("Invalid race").queue();
                return;
            }
            try {
                class_ = Classes.ClassEnum.valueOf(creationParts[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                channel.sendMessage("Invalid class").queue();
                return;
            }

            userCharacters.put(id, new Character(creationParts[0], race, class_));
            channel.sendMessage("Character successfully created").queue();
        }
    }


    private static void sendInvalidFormatMessage(MessageChannel channel) {
        channel.sendMessage("Invalid input. Use '!newChar {name} {race} {class}").queue();
    }
}
