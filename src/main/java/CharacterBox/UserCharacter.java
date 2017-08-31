package CharacterBox;


import CharacterBox.AttackBox.Weapon;
import CharacterBox.AttackBox.WeaponProficiencies;
import CharacterBox.BroadInfo.*;
import CoreBox.Roll;
import DataPersistenceBox.DataPersistence;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.entities.User;

import java.io.Serializable;
import java.util.*;



public class UserCharacter implements Serializable {
    private static final String fileName = "UserCharactersSave.txt";
    // When a character makes an attack the number that must be beaten or equaled for a hit
    private static final int defenderAC = 13;
    private static Map<Long, UserCharacter> userCharacters = new HashMap<>();

    private String name;
    private int age;
    private String alignment;
    private Class_.ClassEnum class_;
    private Race.RaceEnum race;
    private SubRace.SubRaceEnum subRace;
    private UserBackground background;
    private String trinket;
    private int level;
    private int hp;
    private int speed;
    private Abilities abilities;
    private Set<CharacterConstants.AbilityEnum> savingThrows;
    private Set<CharacterConstants.SkillEnum> skillProficiencies;
    private Set<CharacterConstants.Language> languages;
    private int funds;
    private WeaponProficiencies weaponProficiencies;
    private Weapon.WeaponsEnum weapon;


    public UserCharacter(String name, Race.RaceEnum race, SubRace.SubRaceEnum subRace, Class_.ClassEnum class_,
                         String background)
    {
        this.name = name;
        this.class_ = class_;
        this.race = race;
        this.subRace = subRace;
        level = 1;

        final Class_ classInfo = Class_.getClassInfo(class_);
        final Race raceInfo = Race.getRaceInfo(race);
        final SubRace subRaceInfo = getSubraceInfo(subRace);
        final Background backgroundInfo = Background.getBackgroundInfo(background);

        age = raceInfo.generateRandomAge();
        speed = raceInfo.getSpeed();
        Alignment alignment = raceInfo.getRandomAlignment();
        this.alignment = alignment.getAlignmentInitials();
        this.background = new UserBackground(background, alignment);

        languages = new HashSet<>();
        languages.addAll(raceInfo.getLanguages());
        removeLanguageWildcards();
        languages.addAll(backgroundInfo.getLanguages());
        removeLanguageWildcards();

        abilities = new Abilities(classInfo.getAbilityOrder());
        abilities.addIncreases(raceInfo.getAbilityIncreases());
        abilities.addIncreases(subRaceInfo.getAbilityIncreases());
        addSkillProficiencies(backgroundInfo, race, classInfo);

        savingThrows = classInfo.getSavingThrows();
        weaponProficiencies = classInfo.getWeaponProficiencies();
        weapon = classInfo.getStartWeapon();
        funds = classInfo.rollFunds();
        hp = classInfo.getStartHP(abilities.getModifier(CharacterConstants.AbilityEnum.CONSTITUTION));

        addSubraceSubtleties(subRace);
        trinket = Trinkets.getTrinketLowerCaseStart();
    }


    private SubRace getSubraceInfo(SubRace.SubRaceEnum subRaceEnum) {
        if (subRaceEnum != null) {
            final SubRace subRace = Race.getRaceInfo(subRaceEnum);
            if (race != subRace.getMainRace()) {
                throw new BadUserInputException("Invalid subrace");
            }
            else {
                return subRace;
            }
        }
        else {
            return new SubRace();
        }
    }


    private void removeLanguageWildcards() {
        if (languages.contains(CharacterConstants.Language.WILDCARD)) {
            languages.remove(CharacterConstants.Language.WILDCARD);
            languages.add(CharacterConstants.getRandomLanguage(languages));
        }
        if (languages.contains(CharacterConstants.Language.WILDCARD2)) {
            languages.remove(CharacterConstants.Language.WILDCARD2);
            languages.add(CharacterConstants.getRandomLanguage(languages));
        }
    }


    /*
     * Half elves get 2 extra random skill proficiencies
     * Half orcs get proficiency in intimidation
     */
    private void addSkillProficiencies(Background backgroundInfo, Race.RaceEnum race, Class_ classInfo) {
        skillProficiencies = backgroundInfo.getProficiencies();
        // TODO: Given passing by reference do I need to return?
        skillProficiencies = classInfo.getAddSkillProficiencies(skillProficiencies);

        switch (race) {
            case HALFELF:
                final CharacterConstants.SkillEnum[] skillEnums = CharacterConstants.SkillEnum.values();
                for (int i = 0; i < 2; i++) {
                    CharacterConstants.SkillEnum skill;
                    // Find a skill that the character doesn't yet have proficiency in
                    do {
                        skill = skillEnums[new Random().nextInt(skillEnums.length)];
                    } while (skillProficiencies.contains(skill));
                    skillProficiencies.add(skill);
                }
                break;
            case HALFORC:
                skillProficiencies.add(CharacterConstants.SkillEnum.INTIMIDATION);
                break;
        }
    }


    private void addSubraceSubtleties(SubRace.SubRaceEnum subRace) {
        if (subRace != null) {
            switch (subRace) {
                case HIGH:
                    languages.add(CharacterConstants.getRandomLanguage(languages));
                    break;
                case DARK:
                    weaponProficiencies.add(Weapon.WeaponsEnum.RAPIER);
                    weaponProficiencies.add(Weapon.WeaponsEnum.SHORTSWORD);
                    weaponProficiencies.add(Weapon.WeaponsEnum.CROSSBOW);
                    break;
                case HILL:
                    hp++;
                    break;
                case WOOD:
                    speed = 35;
                    break;
            }

            if (subRace == SubRace.SubRaceEnum.HIGH || subRace == SubRace.SubRaceEnum.WOOD) {
                weaponProficiencies.add(Weapon.WeaponsEnum.SHORTSWORD);
                weaponProficiencies.add(Weapon.WeaponsEnum.LONGSWORD);
                weaponProficiencies.add(Weapon.WeaponsEnum.SHORTBOW);
                weaponProficiencies.add(Weapon.WeaponsEnum.LONGBOW);
            }
        }
    }


    /*
     * id is the id of the user who the created character will be bound to
     */
    public static void createUserCharacterAndAddToMap(long id, String creationString) {
        userCharacters.put(id, createUserCharacter(creationString));
    }


    public static UserCharacter createUserCharacter(String creationString) {
        if (userCharacters == null) {
            userCharacters = new HashMap<>();
        }

        final String[] creationParts = creationString.split(" ");

        if (creationParts.length < 4) {
            throw new BadUserInputException("Not enough arguments, try checking out !help char");
        }

        SubRace.SubRaceEnum subRace;
        final Race.RaceEnum race;
        final Class_.ClassEnum class_;
        final String background = creationParts[creationParts.length - 1];

        if (!Background.contains(background)) {
            throw new BadUserInputException("Invalid background");
        }

        try {
            class_ = Class_.ClassEnum.valueOf(creationParts[creationParts.length - 2].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadUserInputException("Invalid class");
        }

        try {
            subRace = SubRace.SubRaceEnum.valueOf(creationParts[creationParts.length - 4].toUpperCase());
        } catch (IllegalArgumentException e) {
            // Not everyone wants a subrace
            subRace = null;
        }

        if (creationParts[creationParts.length - 3].equalsIgnoreCase("drow")) {
            race = Race.RaceEnum.ELF;
            subRace = SubRace.SubRaceEnum.DARK;
        }
        else {
            try {
                race = Race.RaceEnum.valueOf(creationParts[creationParts.length - 3].toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadUserInputException("Invalid race");
            }
        }

        String name = "";
        for (int i = 0; i < creationParts.length - 3; i++) {
            if (i < creationParts.length - 4 || subRace == null
                    || creationParts[creationParts.length - 3].equalsIgnoreCase("drow"))
            {
                name += creationParts[i] + " ";
            }
        }

        return new UserCharacter(name.trim(), race, subRace, class_, background);
    }


    /*
     * id is the id of the user who's character will be deleted
     */
    public static void deleteCharacter(long id) {
        if (userCharacters.containsKey(id)) {
            userCharacters.remove(id);
        }
        else {
            throw new BadStateException("You don't have a character to delete");
        }
    }


    /*
     * id is the id of the user who's character who's weapon will be changed
     */
    public static void changeCharacterWeapon(long id, String newWeapon) {
        if (userCharacters.containsKey(id)) {
            userCharacters.get(id).changeWeapons(newWeapon);
        }
        else {
            throw new BadStateException(
                    "If you don't have a character yet you can't change their weapon. "
                            + "Use !NewCommand to make a new character (!charHelp if you get stuck)");
        }
    }


    private void changeWeapons(String newWeapon) {
        try {
            weapon = Weapon.WeaponsEnum.valueOf(newWeapon.replace(" ", "").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadUserInputException("Weapon not recognised, you can see a list of weapons using !weapons");
        }
    }


    /*
     * The attacker will be the character of the author
     */
    public static String attack(User author, String victim) {
        final String attacker = author.getName();
        victim = victim.trim().replace("@", "");

        if (userCharacters.containsKey(author.getIdLong())) {
            final UserCharacter character = userCharacters.get(author.getIdLong());
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
            throw new BadStateException(
                    attacker + ", I see you're eager to get to the violence but you'll need to"
                            + " make a character first using !newChar");
        }
    }


    private static int getDamage(UserCharacter character, boolean isNaddy20) {
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
            throw new BadStateException("You don't seem to have a character yet. Make one using !NewCommand");
        }
    }


    public String getDescription() {
        String subraceString = "";
        if (subRace != null) {
            subraceString = subRace.toString() + " ";
        }

        String string = "";
        string += String.format("%s, %s (%s)\n", name, age, alignment);
        string += String.format("Race/Class: %s%s %s\n", subraceString, race.toString(), class_.toString());
        string += String.format("Funds: %d\n", funds);
        string += String.format(
                "Stats: Str %d, Dex %d, Con %d, Int %d, Wis %d, Cha %d\n",
                abilities.getStat(CharacterConstants.AbilityEnum.STRENGTH),
                abilities.getStat(CharacterConstants.AbilityEnum.DEXTERITY),
                abilities.getStat(CharacterConstants.AbilityEnum.CONSTITUTION),
                abilities.getStat(CharacterConstants.AbilityEnum.INTELLIGENCE),
                abilities.getStat(CharacterConstants.AbilityEnum.WISDOM),
                abilities.getStat(CharacterConstants.AbilityEnum.CHARISMA)
        );
        string += String.format("Saving Throws: %s\n", getSavingThrowsAsString());
        string += String.format("Skill Proficiencies: %s\n", getSkillProficienciesAsString());
        string += String.format("Languages: %s\n", getLanguagesAsString());
        string += String.format("Weapon Proficiencies: %s\n", weaponProficiencies.toString());
        string += String.format("Weapon: %s\n\n", weapon.toString());
        string += String.format("Background: %s\n%s\n\n", background.getName().toUpperCase(),
                                background.getDescription()
        );
        string += String.format("For as long as I can remember I've had %s", trinket);

        return string;
    }


    private String getSavingThrowsAsString() {
        String string = "";
        for (CharacterConstants.AbilityEnum ability : savingThrows) {
            string += ability.toString() + ", ";
        }
        string = string.trim();
        string = string.substring(0, string.length() - 1);

        return string;
    }


    private String getSkillProficienciesAsString() {
        String string = "";
        for (CharacterConstants.SkillEnum skill : skillProficiencies) {
            string += skill.toString() + ", ";
        }
        string = string.trim();
        string = string.substring(0, string.length() - 1);

        return string;
    }


    private String getLanguagesAsString() {
        String string = "";
        for (CharacterConstants.Language language : languages) {
            string += language.toString() + ", ";
        }
        string = string.trim();
        string = string.substring(0, string.length() - 1);

        return string;
    }


    /*
     * Roll a specific stat, saving throw, or initiative
     */
    public static String roll(long id, String message) {
        message = message.toUpperCase();

        if (userCharacters.containsKey(id)) {
            final UserCharacter character = userCharacters.get(id);
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
            throw new BadStateException("You don't seem to have a character yet. Make one using !NewCommand");
        }
    }


    public String getName() {
        return name;
    }


    private String rollInitiative() {
        final int modifier = abilities.getModifier(CharacterConstants.AbilityEnum.DEXTERITY);
        return new Roll(1, 20, modifier).getStringForRoll();
    }


    private String rollSavingThrow(CharacterConstants.AbilityEnum ability) {
        int modifier = abilities.getModifier(ability);

        if (savingThrows.contains(ability)) {
            modifier += CharacterConstants.getProficiencyBonus(level);
        }

        return new Roll(1, 20, modifier).getStringForRoll();
    }


    private String rollSkillCheck(CharacterConstants.SkillEnum skill) {
        int modifier = abilities.getModifier(skill.getMainAbility());

        if (skillProficiencies.contains(skill)) {
            modifier += CharacterConstants.getProficiencyBonus(level);
        }

        return new Roll(1, 20, modifier).getStringForRoll();
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
            userCharacters = (Map<Long, UserCharacter>) DataPersistence.loadFirstObject(fileName);
        } catch (IllegalStateException e) {
            System.out.println("Character load failed");
        }
    }


    private Weapon getWeaponInfo() {
        return Weapon.getWeaponInfo(weapon);
    }


    /*
     * Returns an attack roll using the current weapon
     */
    private Roll.RollResult attackRoll() {
        return new Roll(1, 20, getAttackModifier()).roll();
    }


    /*
     * Returns the attack modifier for the current weapon
     */
    private int getAttackModifier() {
        int modifier = getAbilityAttackModifier(Weapon.getWeaponInfo(weapon).getWeaponAttackTypeEnum());

        if (weaponProficiencies.contains(weapon)) {
            modifier = CharacterConstants.getProficiencyBonus(level);
        }

        return modifier;
    }


    /*
     * Returns the modifier of the ability that the current weapon uses to modify attacks (str or dex)
     */
    private int getAbilityAttackModifier(Weapon.AttackTypeEnum attackType) {
        switch (attackType) {
            case MELEE:
                return abilities.getModifier(CharacterConstants.AbilityEnum.STRENGTH);
            case RANGE:
                return abilities.getModifier(CharacterConstants.AbilityEnum.DEXTERITY);
            case FINESSE:
            default:
                if (abilities.getStat(CharacterConstants.AbilityEnum.STRENGTH) > abilities
                        .getStat(CharacterConstants.AbilityEnum.DEXTERITY))
                {
                    return getAbilityAttackModifier(Weapon.AttackTypeEnum.MELEE);
                }
                else {
                    return getAbilityAttackModifier(Weapon.AttackTypeEnum.RANGE);
                }
        }
    }


    /*
     * Returns a damage roll for the current weapon
     */
    private int rollDamage() {
        int damage = getAttackModifier() + getWeaponInfo().rollDamage();

        if (race == Race.RaceEnum.HALFORC) {
            damage += getWeaponInfo().rollOneDamageDie();
        }
        return damage;
    }


    /*
     * Returns a critical damage roll for the current weapon
     */
    private int rollCriticalDamage() {
        int damage = getAttackModifier() + getWeaponInfo().rollCriticalDamage();

        if (race == Race.RaceEnum.HALFORC) {
            damage += getWeaponInfo().rollOneDamageDie();
        }
        return damage;
    }
}
