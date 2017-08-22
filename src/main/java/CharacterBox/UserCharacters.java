package CharacterBox;

import CharacterBox.AttackBox.Weapon;
import CharacterBox.BroardInfo.Class_;
import CharacterBox.BroardInfo.Race;
import CharacterBox.BroardInfo.SubRace;
import DataPersistenceBox.DataPersistence;
import ExceptionsBox.BadUserInputException;
import Foo.Roll;
import net.dv8tion.jda.core.entities.User;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;



public class UserCharacters implements Serializable {
    private static final String fileName = "UserCharactersSave.txt";
    // When a character makes an attack the number that must be beaten or equaled for a hit
    private static final int defenderAC = 13;
    private static Map<Long, Character> userCharacters = new HashMap<>();


    /*
     * id is the id of the user who the created character will be bound to
     */
    public static void createUserCharacter(long id, String creationString) {
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
                throw new BadUserInputException("Invalid race");
            }
        }

        try {
            class_ = Class_.ClassEnum.valueOf(creationParts[creationParts.length - 1].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadUserInputException("Invalid class");
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
    }


    /*
     * id is the id of the user who's character will be deleted
     */
    public static void deleteCharacter(long id) {
        if (userCharacters.containsKey(id)) {
            userCharacters.remove(id);
        }
        else {
            throw new IllegalStateException("You don't have a character to delete");
        }
    }


    /*
     * id is the id of the user who's character who's weapon will be changed
     */
    public static void changeCharacterWeapon(long id, String newWeapon) {
        if (getCharacter(id).isPresent()) {
            userCharacters.get(id).changeWeapons(newWeapon);
        }
        else {
            throw new IllegalStateException(
                    "If you don't have a character yet you can't change their weapon. "
                            + "Use !NewCommand to make a new character (!charHelp if you get stuck)");
        }
    }


    public static Optional<Character> getCharacter(long id) {
        if (userCharacters.keySet().contains(id)) {
            return Optional.of(userCharacters.get(id));
        }
        else {
            return Optional.empty();
        }
    }


    /*
     * The attacker will be the character of the author
     */
    public static String attack(User author, String victim) {
        final String attacker = author.getName();
        victim = victim.trim().replace("@", "");

        if (userCharacters.containsKey(author.getIdLong())) {
            final Character character = userCharacters.get(author.getIdLong());
            final Weapon weapon = character.getWeaponInfo();
            String message = weapon.getAttackLine();

            final Roll.RollResult attackRoll = character.attackRoll();
            if (attackRoll.getResult() >= defenderAC && !attackRoll.isCritFail()) {
                message += " " + weapon.getHitLine();

                int damage;
                damage = getDamage(character, attackRoll.isNaddy20());
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
                                                    + ", I see you're eager to get to the violence but you'll need to"
                                                    + " make a "
                                                    + "character first using !NewCommand");
        }
    }


    private static int getDamage(Character character, boolean isNaddy20) {
        if (isNaddy20) {
            return character.rollCriticalDamage();
        }
        else {
            return character.rollDamage();
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
            throw new IllegalStateException("You don't seem to have a character yet. Make one using !NewCommand");
        }
    }


    /*
     * Roll a specific stat, saving throw, or initiative
     */
    public static String roll(long id, String message) {
        message = message.toUpperCase();

        if (userCharacters.containsKey(id)) {
            final Character character = userCharacters.get(id);
            final String characterName = character.getName();

            if (message.equals("INITIATIVE")) {
                return characterName + " " + character.rollInitiative();
            }

            try {
                final CharacterConstants.AbilityEnum ability = CharacterConstants.AbilityEnum.valueOf(message);
                return characterName + " " + character.rollSavingThrow(ability);
            } catch (IllegalArgumentException e) {
                // It may have been a skill check
            }

            try {
                final CharacterConstants.SkillEnum skill = CharacterConstants.SkillEnum.valueOf(message);
                return characterName + " " + character.rollSkillCheck(skill);
            } catch (IllegalArgumentException e) {
                throw new BadUserInputException("You can only roll abilities, skills, or initiative");
            }
        }
        else {
            throw new IllegalStateException("You don't seem to have a character yet. Make one using !NewCommand");
        }
    }


    public static void save() {
        try {
            DataPersistence.save(fileName, userCharacters);
        } catch (IllegalStateException e) {
            System.out.println("Character save failed");
        }
    }


    public static void load() {
        try {
            userCharacters = (Map<Long, Character>) DataPersistence.loadFirstObject(fileName);
        } catch (IllegalStateException e) {
            System.out.println("Character load failed");
        }
    }
}
