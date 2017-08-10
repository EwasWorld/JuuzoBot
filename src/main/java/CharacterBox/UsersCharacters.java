package main.java.CharacterBox;

import main.java.CharacterBox.ClassBox.Classes;
import main.java.CharacterBox.RaceBox.Races;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;



public class UsersCharacters {
    private static Map<Long, Character> userCharacters;


    private static Optional<Character> getCharacter(long id) {
        if (userCharacters == null) {
            userCharacters = new HashMap<>();
        }
        return Optional.of(userCharacters.get(id));
    }


    public static void createUserCharacter(MessageChannel channel, long id, String creationString) {
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

        Character character = new Character(creationParts[0], race, class_);
        userCharacters.put(id, character);
        channel.sendMessage("Character successfully created").queue();
        channel.sendMessage(character.getDescription()).queue();
    }


    private static void sendInvalidFormatMessage(MessageChannel channel) {
        channel.sendMessage("Invalid input. Use '!newChar {name} {race} {class}").queue();
    }
}
