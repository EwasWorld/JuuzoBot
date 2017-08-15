package CharacterBox;

import CharacterBox.AttackBox.Weapon;
import CharacterBox.ClassBox.Class_;
import CharacterBox.RaceBox.Race;
import CharacterBox.RaceBox.SubRace;
import Foo.IDs;
import Foo.LoadSaveConstants;
import Foo.Roll;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;



public class UsersCharacters implements Serializable {
    private static final String fileLocation = IDs.mainFilePath + "CharacterBox/UserCharactersSave.txt";
    private static Map<Long, Character> userCharacters = new HashMap<>();
    private static final int defenderAC = 13;


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
        final Race.RaceEnum race;
        final Class_.ClassEnum class_;

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
            race = Race.RaceEnum.ELF;
            subRace = SubRace.SubRaceEnum.DARK;
        }
        else {
            try {
                race = Race.RaceEnum.valueOf(creationParts[creationParts.length - 2].toUpperCase());
            } catch (IllegalArgumentException e) {
                channel.sendMessage("Invalid race").queue();
                return;
            }
        }

        try {
            class_ = Class_.ClassEnum.valueOf(creationParts[creationParts.length - 1].toUpperCase());
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


    public static void attack(User author, String victim, MessageChannel channel) {
        final String attacker = author.getName();
        victim = victim.trim().replace("@", "");

        if (userCharacters.containsKey(author.getIdLong())) {
            Character character = userCharacters.get(author.getIdLong());
            Weapon weapon = character.getWeaponInfo();
            String message = weapon.getAttackLine();

            Roll.RollResult attackRoll = character.attackRoll();
            if (attackRoll.getResult() >= defenderAC && !attackRoll.isCritFail()) {
                message += " " + weapon.getHitLine();

                int damage;
                if (attackRoll.isNaddy20()) {
                    damage = character.rollCriticalDamage();
                }
                else {
                    damage = character.rollDamage();
                }
                message += String.format(" VIC took %d damage", damage);
            }
            else {
                message += " " + weapon.getMissLine();
            }

            message = message.replaceAll("PC", character.getName());
            message = message.replaceAll("VIC", victim);
            channel.sendMessage(message).queue();
        }
        else {
            channel.sendMessage(attacker
                                        + ", I see you're eager to get to the violence but you'll need to make a character first using "
                                        + "!newChar").queue();
        }
    }


    public static void save() {
        try {
            LoadSaveConstants.save(fileLocation, userCharacters);
        }
        catch (IllegalStateException e) {
            System.out.println("Session times save failed");
        }
    }


    public static void load() {
        try {
            userCharacters = (Map<Long, Character>) LoadSaveConstants.loadFirstObject(fileLocation);
        }
        catch (IllegalStateException e) {
            System.out.println("Session times load failed");
        }
    }
}
