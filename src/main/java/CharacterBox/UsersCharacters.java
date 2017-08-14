package main.java.CharacterBox;

import main.java.CharacterBox.ClassBox.Classes;
import main.java.CharacterBox.RaceBox.Races;
import main.java.CharacterBox.RaceBox.SubRace;
import main.java.Foo.IDs;
import main.java.Foo.LoadSaveConstants;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;



public class UsersCharacters implements Serializable {
    private static final String fileLocation = IDs.mainFilePath + "CharacterBox/UserCharactersSave.txt";
    private static Map<Long, Character> userCharacters = new HashMap<>();


    public static Optional<Character> getCharacter(long id) {
        if (userCharacters.keySet().contains(id)) {
            return Optional.of(userCharacters.get(id));
        }
        else {
            return Optional.empty();
        }
    }


    public static void createUserCharacter(MessageChannel channel, long id, String creationString) {
        if (userCharacters == null) {
            userCharacters = new HashMap<>();
        }

        final String[] creationParts = creationString.split(" ");
        SubRace.SubRaceEnum subRace;
        final Races.RaceEnum race;
        final Classes.ClassEnum class_;

        if (creationParts[creationParts.length - 3].equalsIgnoreCase("drow")) {
            subRace = SubRace.SubRaceEnum.DARK;
        }
        else {
            try {
                subRace = SubRace.SubRaceEnum.valueOf(creationParts[creationParts.length - 3].toUpperCase());
            } catch (IllegalArgumentException e) {
                // Not everyone wants a subrace
                subRace = null;
            }
        }

        if (creationParts[creationParts.length - 2].equalsIgnoreCase("drow")) {
            race = Races.RaceEnum.ELF;
            subRace = SubRace.SubRaceEnum.DARK;
        }
        else {
            try {
                race = Races.RaceEnum.valueOf(creationParts[creationParts.length - 2].toUpperCase());
            } catch (IllegalArgumentException e) {
                channel.sendMessage("Invalid race").queue();
                return;
            }
        }

        try {
            class_ = Classes.ClassEnum.valueOf(creationParts[creationParts.length - 1].toUpperCase());
        } catch (IllegalArgumentException e) {
            channel.sendMessage("Invalid class").queue();
            return;
        }

        String name = "";
        for (int i = 0; i < creationParts.length - 2; i++) {
            if (i < creationParts.length - 3 || subRace == null || creationParts[creationParts.length - 2]
                    .equalsIgnoreCase("drow"))
            {
                name += creationParts[i] + " ";
            }
        }

        Character character = new Character(name.trim(), race, subRace, class_);
        userCharacters.put(id, character);
        channel.sendMessage("Character successfully created.\n" + character.getDescription()).queue();
    }


    public static void deleteCharacter(MessageChannel channel, long id) {
        if (userCharacters.containsKey(id)) {
            userCharacters.remove(id);
            channel.sendMessage("Character removed").queue();
        }
        else {
            channel.sendMessage("You don't have a character to delete").queue();
        }
    }


    public static void changeCharacterWeapon(MessageChannel channel, User author, String newWeapon) {
        long authorID = author.getIdLong();
        if (getCharacter(authorID).isPresent()) {
            if (userCharacters.get(authorID).changeWeapons(newWeapon)) {
                channel.sendMessage("Weapon change successful, enjoy your new toy.").queue();
            }
            else {
                channel.sendMessage("Weapon not recognised, you can see a list of weapons using !weapons").queue();
            }
        }
        else {
            channel.sendMessage(
                    "If you don't have a character yet you can't change their weapon. Use !newChar to make a new "
                            + "character (!charHelp if you get stuck)")
                    .queue();
        }
    }


    public static void save(MessageChannel channel) {
        try {
            LoadSaveConstants.save(fileLocation, userCharacters);
        }
        catch (IllegalStateException e) {
            channel.sendMessage("Session times save failed").queue();
        }
    }


    public static void load(MessageChannel channel) {
        try {
            userCharacters = (Map<Long, Character>) LoadSaveConstants.loadFirstObject(fileLocation);
        }
        catch (IllegalStateException e) {
            channel.sendMessage("Session times load failed").queue();
        }
    }
}
