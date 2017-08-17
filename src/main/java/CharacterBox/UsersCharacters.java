package CharacterBox;

import CharacterBox.AttackBox.Weapon;
import CharacterBox.ClassBox.Class_;
import CharacterBox.RaceBox.Race;
import CharacterBox.RaceBox.SubRace;
import Foo.IDs;
import Foo.LoadSaveConstants;
import Foo.Roll;
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


    /*
     * id is the id of the user who the created character will be bound to
     */
    public static String createUserCharacter(long id, String creationString) {
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
                throw new IllegalArgumentException("Invalid race");
            }
        }

        try {
            class_ = Class_.ClassEnum.valueOf(creationParts[creationParts.length - 1].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid class");
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
        return "Character successfully created.\n" + character.getDescription();
    }


    /*
     * id is the id of the user who's character will be deleted
     */
    public static String deleteCharacter(long id) {
        if (userCharacters.containsKey(id)) {
            userCharacters.remove(id);
            return "Character removed";
        }
        else {
            throw new IllegalStateException("You don't have a character to delete");
        }
    }


    /*
     * id is the id of the user who's character who's weapon will be changed
     */
    public static String changeCharacterWeapon(long id, String newWeapon) {
        if (getCharacter(id).isPresent()) {
            if (userCharacters.get(id).changeWeapons(newWeapon)) {
                return "Weapon change successful, enjoy your new toy.";
            }
            else {
                throw new IllegalArgumentException(
                        "Weapon not recognised, you can see a list of weapons using !weapons");
            }
        }
        else {
            throw new IllegalStateException(
                    "If you don't have a character yet you can't change their weapon. Use !newChar to make a new "
                            + "character (!charHelp if you get stuck)");
        }
    }


    /*
     * The attacker will be the character of the author
     */
    public static String attack(User author, String victim) {
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
            return message.replaceAll("VIC", victim);
        }
        else {
            throw new IllegalStateException(attacker
                                        + ", I see you're eager to get to the violence but you'll need to make a "
                                        + "character first using !newChar");
        }
    }


    /*
     * Returns the description of the character bound to the given id
     */
    public static String getCharacterDescription(long id) {
        if (userCharacters.containsKey(id)) {
            return userCharacters.get(id).getDescription();
        }
        else {
            throw new IllegalStateException("You don't seem to have a character yet. Make one using !newChar");
        }
    }


    public static void save() {
        try {
            LoadSaveConstants.save(fileLocation, userCharacters);
        } catch (IllegalStateException e) {
            System.out.println("Session times save failed");
        }
    }


    public static void load() {
        try {
            userCharacters = (Map<Long, Character>) LoadSaveConstants.loadFirstObject(fileLocation);
        } catch (IllegalStateException e) {
            System.out.println("Session times load failed");
        }
    }
}
