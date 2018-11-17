package CharacterBox;

import CharacterBox.AttackBox.Weapon;
import CharacterBox.AttackBox.WeaponProficiencies;
import CharacterBox.BroadInfo.*;
import CoreBox.Die;
import DatabaseBox.DatabaseTable;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import ExceptionsBox.ContactEwaException;
import net.dv8tion.jda.core.entities.User;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;



/**
 * refactored 13/11/18
 */
public class UserCharacter implements Serializable {
    // When a character makes an attack the number that must be beaten or equaled for a hit
    private static final int defenderAC = 13;
    private static DatabaseTable charDatabaseTable = DatabaseTable.createDatabaseTable(
            "Characters", CharDatabaseFields.values());
    private static DatabaseTable charAbilitiesDatabaseTable = DatabaseTable.createDatabaseTable(
            "CharacterAbilities", CharAbilitiesDatabaseFields.values());
    private static DatabaseTable charProficienciesDatabaseTable = DatabaseTable.createDatabaseTable(
            "CharactersProficiencies", CharProficienciesDatabaseFields.values());
    private static DatabaseTable charLanguagesDatabaseTable = DatabaseTable.createDatabaseTable(
            "CharactersLanguages", CharLanguagesDatabaseFields.values());



    private enum CharDatabaseFields implements DatabaseTable.DatabaseFieldsEnum {
        USERID("user", DatabaseTable.SQLType.TEXT, true), NAME("name", DatabaseTable.SQLType.TEXT, true),
        AGE("age", DatabaseTable.SQLType.INT, true), ALIGNMENT("alignment", DatabaseTable.SQLType.TEXT, true),
        CLAZZ("clazz", DatabaseTable.SQLType.TEXT, true), RACE("race", DatabaseTable.SQLType.TEXT, true),
        SUBRACE("subrace", DatabaseTable.SQLType.TEXT, false), BACKGROUND(
                "background", DatabaseTable.SQLType.TEXT, true),
        BACKGROUND_SPECIFICS("backgroundSpecifics", DatabaseTable.SQLType.TEXT, true),
        TRINKET("trinket", DatabaseTable.SQLType.TEXT, true), WEAPON("weapon", DatabaseTable.SQLType.TEXT, true),
        FUNDS("funds", DatabaseTable.SQLType.INT, true);

        private String fieldName;
        private DatabaseTable.SQLType sqlType;
        private boolean required;


        CharDatabaseFields(String fieldName, DatabaseTable.SQLType sqlType, boolean required) {
            this.fieldName = fieldName;
            this.sqlType = sqlType;
            this.required = required;
        }


        @Override
        public String getFieldName() {
            return fieldName;
        }


        @Override
        public DatabaseTable.SQLType getSqlType() {
            return sqlType;
        }


        @Override
        public boolean isRequired() {
            return required;
        }
    }



    private enum CharAbilitiesDatabaseFields implements DatabaseTable.DatabaseFieldsEnum {
        CHAR_ID("charID", DatabaseTable.SQLType.INT, true), ABILITY("ability", DatabaseTable.SQLType.TEXT, true),
        VALUE("value", DatabaseTable.SQLType.INT, true), IS_SAVING_THROW(
                "isSavingThrow", DatabaseTable.SQLType.BIT, true);

        private String fieldName;
        private DatabaseTable.SQLType sqlType;
        private boolean required;


        CharAbilitiesDatabaseFields(String fieldName, DatabaseTable.SQLType sqlType, boolean required) {
            this.fieldName = fieldName;
            this.sqlType = sqlType;
            this.required = required;
        }


        @Override
        public String getFieldName() {
            return fieldName;
        }


        @Override
        public DatabaseTable.SQLType getSqlType() {
            return sqlType;
        }


        @Override
        public boolean isRequired() {
            return required;
        }
    }



    private enum CharProficienciesDatabaseFields implements DatabaseTable.DatabaseFieldsEnum {
        CHAR_ID("charID", DatabaseTable.SQLType.INT, true), PROF_TYPE("profType", DatabaseTable.SQLType.TEXT, true),
        PROFICIENCY("proficiency", DatabaseTable.SQLType.TEXT, true);

        private String fieldName;
        private DatabaseTable.SQLType sqlType;
        private boolean required;


        CharProficienciesDatabaseFields(String fieldName, DatabaseTable.SQLType sqlType, boolean required) {
            this.fieldName = fieldName;
            this.sqlType = sqlType;
            this.required = required;
        }


        @Override
        public String getFieldName() {
            return fieldName;
        }


        @Override
        public DatabaseTable.SQLType getSqlType() {
            return sqlType;
        }


        @Override
        public boolean isRequired() {
            return required;
        }
    }


    private enum CharLanguagesDatabaseFields implements DatabaseTable.DatabaseFieldsEnum {
        CHAR_ID("charID", DatabaseTable.SQLType.INT, true), LANGUAGE("language", DatabaseTable.SQLType.TEXT, true);

        private String fieldName;
        private DatabaseTable.SQLType sqlType;
        private boolean required;


        CharLanguagesDatabaseFields(String fieldName, DatabaseTable.SQLType sqlType, boolean required) {
            this.fieldName = fieldName;
            this.sqlType = sqlType;
            this.required = required;
        }


        @Override
        public String getFieldName() {
            return fieldName;
        }


        @Override
        public DatabaseTable.SQLType getSqlType() {
            return sqlType;
        }


        @Override
        public boolean isRequired() {
            return required;
        }
    }



    private String name;
    private int age;
    private String alignment;
    private Clazz.ClassEnum clazz;
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


    public UserCharacter(String name) {
        this.name = name;
    }


    private UserCharacter(String name, int age, String alignment, Clazz.ClassEnum clazz,
                          Race.RaceEnum race, SubRace.SubRaceEnum subRace, UserBackground background,
                          String trinket, int funds, Weapon.WeaponsEnum weapon) {
        this.name = name;
        this.age = age;
        this.alignment = alignment;
        this.clazz = clazz;
        this.race = race;
        this.subRace = subRace;
        this.background = background;
        this.trinket = trinket;
        this.funds = funds;
        this.weapon = weapon;
    }


    public void setClazz(Clazz.ClassEnum clazz) {
        this.clazz = clazz;
    }


    public void setRace(Race.RaceEnum race) {
        this.race = race;
    }


    public Race.RaceEnum getRace() {
        return race;
    }


    public void setSubRace(SubRace.SubRaceEnum subRace) {
        this.subRace = subRace;
    }


    /**
     * Sets the character background and alignment (required for background generation)
     */
    public void setBackground(Background.BackgroundEnum background) {
        final Alignment alignment = Race.getRaceInfo(race).getRandomAlignment();
        this.alignment = alignment.getAlignmentInitials();
        this.background = background.generateRandomBackground(alignment);
    }


    private void saveToDatabase(String userID) {
        charDatabaseTable.deleteTable();
        charAbilitiesDatabaseTable.deleteTable();
        charProficienciesDatabaseTable.deleteTable();

        final Map<String, Object> charArgs = new HashMap<>();
        charArgs.put(CharDatabaseFields.USERID.fieldName, userID);
        charArgs.put(CharDatabaseFields.NAME.fieldName, name);
        charArgs.put(CharDatabaseFields.AGE.fieldName, age);
        charArgs.put(CharDatabaseFields.ALIGNMENT.fieldName, alignment);
        charArgs.put(CharDatabaseFields.CLAZZ.fieldName, clazz.toString());
        charArgs.put(CharDatabaseFields.RACE.fieldName, race.toString());
        if (subRace != null) {
            charArgs.put(CharDatabaseFields.SUBRACE.fieldName, subRace.toString());
        }
        charArgs.put(CharDatabaseFields.BACKGROUND.fieldName, background.getBackgroundEnumVal().toString());
        charArgs.put(CharDatabaseFields.BACKGROUND_SPECIFICS.fieldName, background.getForSpecificsDatabase());
        charArgs.put(CharDatabaseFields.TRINKET.fieldName, trinket);
        charArgs.put(CharDatabaseFields.WEAPON.fieldName, weapon.toString());
        charArgs.put(CharDatabaseFields.FUNDS.fieldName, funds);
        charDatabaseTable.insert(charArgs);

        final int charID = getCharID(userID, name);

        for (CharacterConstants.AbilityEnum ability : CharacterConstants.AbilityEnum.values()) {
            final Map<String, Object> abilityArgs = new HashMap<>();
            abilityArgs.put(CharAbilitiesDatabaseFields.CHAR_ID.getFieldName(), charID);
            abilityArgs.put(CharAbilitiesDatabaseFields.ABILITY.getFieldName(), ability.toString());
            abilityArgs.put(CharAbilitiesDatabaseFields.VALUE.getFieldName(), abilities.getStat(ability));
            abilityArgs.put(CharAbilitiesDatabaseFields.IS_SAVING_THROW.getFieldName(), savingThrows.contains(ability));
            charAbilitiesDatabaseTable.insert(abilityArgs);
        }

        for (CharacterConstants.SkillEnum skill : skillProficiencies) {
            final Map<String, Object> proficiencyArgs = new HashMap<>();
            proficiencyArgs.put(CharProficienciesDatabaseFields.CHAR_ID.getFieldName(), charID);
            proficiencyArgs.put(CharProficienciesDatabaseFields.PROF_TYPE.getFieldName(), "SKILL");
            proficiencyArgs.put(CharProficienciesDatabaseFields.PROFICIENCY.getFieldName(), skill.toString());
            charProficienciesDatabaseTable.insert(proficiencyArgs);
        }
        for (Map<String, Object> weaponProficienciesArgs : weaponProficiencies.toDatabaseArgs(
                CharProficienciesDatabaseFields.PROFICIENCY,
                CharProficienciesDatabaseFields.PROF_TYPE)) {
            weaponProficienciesArgs.put(CharProficienciesDatabaseFields.CHAR_ID.getFieldName(), charID);
            charProficienciesDatabaseTable.insert(weaponProficienciesArgs);
        }

        for (CharacterConstants.Language language : languages) {
            final Map<String, Object> languageArgs = new HashMap<>();
            languageArgs.put(CharLanguagesDatabaseFields.CHAR_ID.getFieldName(), charID);
            languageArgs.put(CharLanguagesDatabaseFields.LANGUAGE.getFieldName(), language.toString());
            charLanguagesDatabaseTable.insert(languageArgs);
        }
    }


    private static UserCharacter loadFromDatabase(String userID, String name) {
        final int charID = getCharID(userID, name);

        /*
         * Main character info
         */
        final Map<String, Object> charArgs = new HashMap<>();
        charArgs.put(charDatabaseTable.getPrimaryKey(), charID);
        final UserCharacter character = (UserCharacter) charDatabaseTable.selectAND(charArgs, rs -> {
            if (rs.next()) {
                try {
                    SubRace.SubRaceEnum subrace;
                    try {
                        subrace = SubRace.SubRaceEnum.valueOf(rs.getString(CharDatabaseFields.SUBRACE.getFieldName()));
                    } catch (NullPointerException e) {
                        subrace = null;
                    }

                    return new UserCharacter(
                            rs.getString(CharDatabaseFields.NAME.getFieldName()),
                            rs.getInt(CharDatabaseFields.AGE.getFieldName()),
                            rs.getString(CharDatabaseFields.ALIGNMENT.getFieldName()),
                            Clazz.ClassEnum.valueOf(rs.getString(CharDatabaseFields.CLAZZ.getFieldName())),
                            Race.RaceEnum.valueOf(rs.getString(CharDatabaseFields.RACE.getFieldName())),
                            subrace,
                            new UserBackground(
                                    Background.BackgroundEnum.valueOf(
                                            rs.getString(CharDatabaseFields.BACKGROUND.getFieldName())),
                                    rs.getString(CharDatabaseFields.BACKGROUND_SPECIFICS.getFieldName())),
                            rs.getString(CharDatabaseFields.TRINKET.getFieldName()),
                            rs.getInt(CharDatabaseFields.FUNDS.getFieldName()),
                            Weapon.WeaponsEnum.valueOf(rs.getString(CharDatabaseFields.WEAPON.getFieldName()))
                    );
                } catch (IllegalArgumentException e) {
                    throw new ContactEwaException("Character load failed");
                }
            }
            // cannot get here else getCharID would have failed
            return null;
        });

        /*
         * Abilities and saving throws
         */
        final Map<String, Object> abilitiesArgs = new HashMap<>();
        charArgs.put(CharAbilitiesDatabaseFields.CHAR_ID.getFieldName(), charID);
        charAbilitiesDatabaseTable.selectAND(abilitiesArgs, rs -> {
            final Map<CharacterConstants.AbilityEnum, Integer> abilitiesMap = new HashMap<>();
            character.savingThrows = new HashSet<>();
            while (rs.next()) {
                final CharacterConstants.AbilityEnum ability = CharacterConstants.AbilityEnum.valueOf(
                        rs.getString(CharAbilitiesDatabaseFields.ABILITY.getFieldName()));
                abilitiesMap.put(ability, rs.getInt(CharAbilitiesDatabaseFields.VALUE.getFieldName()));
                if (rs.getBoolean(CharAbilitiesDatabaseFields.IS_SAVING_THROW.getFieldName())) {
                    character.savingThrows.add(ability);
                }
            }
            character.abilities = new Abilities(abilitiesMap);
            return null;
        });

        /*
         * Weapon and skill proficiencies
         */
        final Map<String, Object> proficienciesArgs = new HashMap<>();
        proficienciesArgs.put(CharProficienciesDatabaseFields.CHAR_ID.getFieldName(), charID);
        charProficienciesDatabaseTable.selectAND(proficienciesArgs, rs -> {
            character.weaponProficiencies = new WeaponProficiencies();
            character.skillProficiencies = new HashSet<>();
            while (rs.next()) {
                final String profType = rs.getString(CharProficienciesDatabaseFields.PROF_TYPE.getFieldName());
                final String proficiency = rs.getString(CharProficienciesDatabaseFields.PROFICIENCY.getFieldName());
                if (profType.equals(WeaponProficiencies.databaseTypeType)) {
                    character.weaponProficiencies.add(Weapon.WeaponProficiencyEnum.valueOf(proficiency));
                }
                else if (profType.equals(WeaponProficiencies.databaseTypeSpecific)) {
                    character.weaponProficiencies.add(Weapon.WeaponsEnum.valueOf(proficiency));
                }
                else {
                    character.skillProficiencies.add(CharacterConstants.SkillEnum.valueOf(proficiency));
                }
            }
            return null;
        });

        /*
         * Languages
         */
        final Map<String, Object> langugesArgs = new HashMap<>();
        langugesArgs.put(CharLanguagesDatabaseFields.CHAR_ID.getFieldName(), charID);
        charLanguagesDatabaseTable.selectAND(langugesArgs, rs -> {
            character.languages = new HashSet<>();
            while (rs.next()) {
                character.languages.add(CharacterConstants.Language.valueOf(rs.getString(CharLanguagesDatabaseFields.LANGUAGE.fieldName)));
            }
            return null;
        });
        return character;
    }


    /**
     * @return character ID
     * @throws BadUserInputException if the character name is invalid
     * @throws BadStateException if the user doesn't have any characters
     */
    private static Integer getCharID(String userID, String name) {
        final Map<String, Object> getIDArgs = new HashMap<>();
        getIDArgs.put(CharDatabaseFields.USERID.fieldName, userID);
        getIDArgs.put(CharDatabaseFields.NAME.fieldName, name);
        return (Integer) charDatabaseTable.selectAND(getIDArgs, rs -> {
            if (rs.next()) {
                return rs.getInt(charDatabaseTable.getPrimaryKey());
            }
            else {
                // Find out why the previous select failed by checking if the user has any characters
                getIDArgs.remove(CharDatabaseFields.NAME.getFieldName());
                return charDatabaseTable.selectAND(getIDArgs, rs2 -> {
                    if (rs2.next()) {
                        throw new BadUserInputException("You don't have a character with the name " + name);
                    }
                    else {
                        throw new BadStateException("You don't have any characters");
                    }
                });
            }
        });
    }


    /**
     * Finish off character creation adding age, speed, level, languages, abilities, and proficiencies, then save the
     * character to the database
     */
    public void completeCreation(String userID) {
        if (race == null || clazz == null || background == null) {
            throw new ContactEwaException("You've somehow messed up character creation...");
        }

        final Clazz classInfo = Clazz.getClassInfo(clazz);
        final Race raceInfo = Race.getRaceInfo(race);
        final SubRace subRaceInfo = getSubraceInfo(subRace);
        final Background backgroundInfo = Background.getBackgroundInfo(background.getBackgroundEnumVal());

        level = 1;

        age = raceInfo.generateRandomAge();
        speed = raceInfo.getSpeed();

        languages = new HashSet<>();
        languages.addAll(raceInfo.getLanguages());
        removeLanguageWildcards();
        languages.addAll(backgroundInfo.getLanguages());
        removeLanguageWildcards();

        abilities = new Abilities(classInfo.getAbilityOrder());
        abilities.addIncreases(raceInfo.getAbilityIncreases());
        abilities.addIncreases(subRaceInfo.getExtraAbilityIncreases());
        addSkillProficiencies(backgroundInfo, race, classInfo);

        savingThrows = classInfo.getSavingThrows();
        weaponProficiencies = classInfo.getWeaponProficiencies();
        weapon = classInfo.getStartWeapon();
        funds = classInfo.rollFunds();
        hp = classInfo.getStartHP(abilities.getModifier(CharacterConstants.AbilityEnum.CONSTITUTION));

        addSubraceSubtleties(subRace);
        trinket = Trinkets.getTrinketLowerCaseStart();

        saveToDatabase(userID);
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


    /**
     * Adds class proficiencies and, if needed, race proficiencies
     */
    private void addSkillProficiencies(Background backgroundInfo, Race.RaceEnum race, Clazz classInfo) {
        skillProficiencies = backgroundInfo.getProficiencies();
        skillProficiencies.addAll(classInfo.getRandomSkillProficiencies(skillProficiencies));

        switch (race) {
            case HALFELF:
                // 2 extra random proficiencies
                final CharacterConstants.SkillEnum[] skillEnums = CharacterConstants.SkillEnum.values();
                for (int i = 0; i < 2; i++) {
                    CharacterConstants.SkillEnum skill;
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


    /**
     * Deletes a specified characters related to a user
     *
     * @param userID the id of the user who's characters will be deleted
     */
    public static void deleteCharacter(String userID, String name) {
        int charID = getCharID(userID, name);
        final Map<String, Object> charArgs = new HashMap<>();
        charArgs.put(CharDatabaseFields.USERID.getFieldName(), userID);
        charArgs.put(CharDatabaseFields.NAME.getFieldName(), name);
        charDatabaseTable.deleteAND(charArgs);

        final Map<String, Object> abilArgs = new HashMap<>();
        abilArgs.put(CharAbilitiesDatabaseFields.CHAR_ID.getFieldName(), charID);
        charAbilitiesDatabaseTable.deleteAND(abilArgs);
        final Map<String, Object> profArgs = new HashMap<>();
        profArgs.put(CharProficienciesDatabaseFields.CHAR_ID.getFieldName(), charID);
        charProficienciesDatabaseTable.deleteAND(profArgs);
    }


    /**
     * @throws BadUserInputException if weapon is not recognised
     */
    public static void changeCharacterWeapon(String authorID, String name, String newWeapon) {
        final Weapon.WeaponsEnum weapon;
        try {
            weapon = Weapon.WeaponsEnum.valueOf(newWeapon.replace(" ", "").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadUserInputException(
                    "Weapon not recognised, you can see a list of weapons using !char weapons list");
        }

        final Map<String, Object> setArgs = new HashMap<>();
        setArgs.put(CharDatabaseFields.WEAPON.getFieldName(), weapon.toString());
        final Map<String, Object> whereArgs = new HashMap<>();
        whereArgs.put(charDatabaseTable.getPrimaryKey(), getCharID(authorID, name));
        charDatabaseTable.updateAND(setArgs, whereArgs);
    }


    /**
     * @return attack flavour text and how much damage was dealt
     */
    public static String attack(User author, String characterName, String victim) {
        final UserCharacter character = loadFromDatabase(author.getId(), characterName);
        victim = victim.trim().replace("@", "");

        final Weapon weapon = character.getWeaponInfo();
        String message = weapon.getAttackLine();

        final Die.RollResult attackRoll = new Die(1, 20, character.getAttackModifier()).roll();
        if (attackRoll.getTotal() >= defenderAC && !attackRoll.isCritFail()) {
            message += " " + weapon.getHitLine();

            int damage;
            damage = rollDamage(character, attackRoll.isNaddy20());
            message += String.format(" VIC took %d damage", damage);
        }
        else {
            message += " " + weapon.getMissLine();
        }

        message = message.replaceAll("PC", characterName);
        return message.replaceAll("VIC", victim);
    }


    private static int rollDamage(UserCharacter character, boolean isNat20) {
        if (isNat20) {
            return character.rollCriticalDamage();
        }
        else {
            return character.rollDamage();
        }
    }


    /**
     * @param name name of the character
     * @return description of the character
     */
    public static String getCharacterDescription(String authorID, String name) {
        return loadFromDatabase(authorID, name).getDescription();
    }


    public String getDescription() {
        String subraceString = "";
        if (subRace != null) {
            subraceString = subRace.toString().toLowerCase() + " ";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("```fix\n%s, %s, %s%s %s (%s), %d gp```", name, age, subraceString,
                                race.toString(), clazz.toString().toLowerCase(), alignment, funds));
        sb.append(String.format(
                "```yaml\nStrength %d, Dexterity %d, Constitution %d, Intelligence %d, Wisdom %d, Charisma %d\n```",
                abilities.getStat(CharacterConstants.AbilityEnum.STRENGTH),
                abilities.getStat(CharacterConstants.AbilityEnum.DEXTERITY),
                abilities.getStat(CharacterConstants.AbilityEnum.CONSTITUTION),
                abilities.getStat(CharacterConstants.AbilityEnum.INTELLIGENCE),
                abilities.getStat(CharacterConstants.AbilityEnum.WISDOM),
                abilities.getStat(CharacterConstants.AbilityEnum.CHARISMA)
        ));
        sb.append("```ini\n");
        sb.append(String.format("[Saving Throws]: %s\n", getAsPrintableString(savingThrows)));
        sb.append(String.format("[Skill Proficiencies]: %s\n", getAsPrintableString(skillProficiencies)));
        sb.append(String.format("[Languages]: %s\n", getAsPrintableString(languages)));
        sb.append(String.format("[Weapon Proficiencies]: %s\n", weaponProficiencies.toString()));
        sb.append(String.format("[Weapon]: %s```\n", weapon.toString().toLowerCase()));
        sb.append(background.getDescription());
        sb.append(String.format("\n\nFor as long as I can remember I've had %s", trinket));

        return sb.toString();
    }


    /**
     * @return each item in the list separated by ,
     */
    private <E extends Enum<E>> String getAsPrintableString(Set<E> objects) {
        final StringBuilder sb = new StringBuilder();
        for (E object : objects) {
            sb.append(object.toString());
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }


    /**
     * Die a specific stat, saving throw, or initiative
     * @return the roll string e.g. "name rolled a 7 and crit"
     */
    public static String roll(String authorID, String name, String args) {
        args = args.toUpperCase();
        final UserCharacter character = loadFromDatabase(authorID, name);
        final String rollString = name + " ";

        if (args.equals("INITIATIVE")) {
            final int modifier = character.abilities.getModifier(CharacterConstants.AbilityEnum.DEXTERITY);
            return rollString + new Die(1, 20, modifier).getStringForRoll();
        }

        try {
            final CharacterConstants.AbilityEnum ability = CharacterConstants.AbilityEnum.valueOf(args);
            int modifier = character.abilities.getModifier(ability);
            if (character.savingThrows.contains(ability)) {
                modifier += CharacterConstants.getProficiencyBonus(character.level);
            }
            return rollString + new Die(1, 20, modifier).getStringForRoll();
        } catch (IllegalArgumentException e) {
            // It may have been a skill check
        }

        try {
            final CharacterConstants.SkillEnum skill = CharacterConstants.SkillEnum.valueOf(args);
            int modifier = character.abilities.getModifier(skill.getMainAbility());
            if (character.skillProficiencies.contains(skill)) {
                modifier += CharacterConstants.getProficiencyBonus(character.level);
            }
            return rollString + new Die(1, 20, modifier).getStringForRoll();
        } catch (IllegalArgumentException e) {
            throw new BadUserInputException("You can only roll abilities, skills, or initiative");
        }
    }


    public String getName() {
        return name;
    }


    private Weapon getWeaponInfo() {
        return Weapon.getWeaponInfo(weapon);
    }


    /**
     * @return the attack modifier for the current weapon
     */
    private int getAttackModifier() {
        int modifier = getAbilityAttackModifier(Weapon.getWeaponInfo(weapon).getWeaponAttackTypeEnum());
        if (weaponProficiencies.contains(weapon)) {
            modifier = CharacterConstants.getProficiencyBonus(level);
        }
        return modifier;
    }


    /**
     * @return the modifier of the ability that the current weapon uses to modify attacks (str or dex)
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


    /**
     * @return a damage rolled for the current weapon
     */
    private int rollDamage() {
        int damage = getAttackModifier() + getWeaponInfo().rollDamage();
        if (race == Race.RaceEnum.HALFORC) {
            damage += getWeaponInfo().rollOneDamageDie();
        }
        return damage;
    }


    /**
     * @return critical damage rolled for the current weapon
     */
    private int rollCriticalDamage() {
        int damage = getAttackModifier() + getWeaponInfo().rollCriticalDamage();
        if (race == Race.RaceEnum.HALFORC) {
            damage += getWeaponInfo().rollOneDamageDie();
        }
        return damage;
    }
}
