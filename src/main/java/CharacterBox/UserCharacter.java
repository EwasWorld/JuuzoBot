package CharacterBox;

import CharacterBox.AttackBox.Weapon;
import CharacterBox.AttackBox.WeaponProficiencies;
import CharacterBox.BroadInfo.*;
import CoreBox.Die;
import DatabaseBox.Args;
import DatabaseBox.DatabaseTable;
import DatabaseBox.DatabaseWrapper;
import DatabaseBox.SetArgs;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import ExceptionsBox.ContactEwaException;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;



/**
 * refactored 13/11/18
 * TODO prevent two characters with the same name from being created by a single user
 */
public class UserCharacter implements Serializable {
    // When a character makes an attack the number that must be beaten or equaled for a hit
    private static final int defenderAC = 13;
    private static final DatabaseTable charDatabaseTable = new DatabaseTable("Characters", CharDatabaseFields.values());
    private static final DatabaseTable abilitiesDatabaseTable = new DatabaseTable(
            "CharacterAbilities", CharAbilitiesDatabaseFields.values());
    private static final DatabaseTable proficienciesDatabaseTable = new DatabaseTable(
            "CharactersProficiencies", CharProficienciesDatabaseFields.values());
    private static final DatabaseTable languagesDatabaseTable = new DatabaseTable(
            "CharactersLanguages", CharLanguagesDatabaseFields.values());
    private static final DatabaseWrapper databaseWrapper = new DatabaseWrapper(new DatabaseTable[]{charDatabaseTable,
            abilitiesDatabaseTable, proficienciesDatabaseTable, languagesDatabaseTable});

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


    public UserCharacter(@NotNull String name) {
        this.name = name;
    }


    private UserCharacter(@NotNull String name, int age, @NotNull String alignment, @NotNull Clazz.ClassEnum clazz,
                          @NotNull Race.RaceEnum race, SubRace.SubRaceEnum subRace, @NotNull UserBackground background,
                          @NotNull String trinket, int funds, @NotNull Weapon.WeaponsEnum weapon) {
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


    /**
     * Helper method for testing
     */
    public static boolean checkRowCounts(int characters, int abilities, int proficiencies, int languages) {
        DatabaseWrapper.checkDatabaseInTestMode();
        return databaseWrapper.checkRowCounts(new int[]{characters, abilities, proficiencies, languages});
    }


    /**
     * Helper method for testing
     */
    public static DatabaseWrapper getDatabaseWrapper() {
        DatabaseWrapper.checkDatabaseInTestMode();
        return databaseWrapper;
    }


    /**
     * Deletes a specified characters related to a user
     *
     * @param userID the id of the user who's characters will be deleted
     */
    public static void deleteCharacter(@NotNull String userID, @NotNull String name) {
        int charID = getCharID(userID, name);
        charDatabaseTable.delete(new Args(charDatabaseTable, Map.of(
                CharDatabaseFields.USERID.fieldName, userID, CharDatabaseFields.NAME.fieldName, name)));
        abilitiesDatabaseTable.delete(new Args(abilitiesDatabaseTable,
                                               CharAbilitiesDatabaseFields.CHAR_ID.fieldName, charID));
        proficienciesDatabaseTable.delete(new Args(proficienciesDatabaseTable,
                                                   CharProficienciesDatabaseFields.CHAR_ID.fieldName, charID));
        languagesDatabaseTable.delete(new Args(languagesDatabaseTable,
                                               CharLanguagesDatabaseFields.CHAR_ID.fieldName, charID));
    }


    /**
     * @return character ID
     * @throws BadUserInputException if the character name is invalid
     * @throws BadStateException if the user doesn't have any characters
     */
    private static int getCharID(@NotNull String userID, @NotNull String name) {
        final Args args = new Args(charDatabaseTable, Map.of(CharDatabaseFields.USERID.fieldName, userID,
                                                             CharDatabaseFields.NAME.fieldName, name));
        return (int) charDatabaseTable.select(args, rs -> {
            if (rs.next()) {
                return rs.getInt(charDatabaseTable.getPrimaryKey());
            }
            else {
                // Find out why the previous select failed by checking if the user has any characters
                args.removeWhereField(CharDatabaseFields.NAME.fieldName);
                return charDatabaseTable.select(args, rs2 -> {
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
     * @throws BadUserInputException if weapon is not recognised
     */
    public static void changeCharacterWeapon(@NotNull String authorID, @NotNull String name,
                                             @NotNull String newWeapon) {
        final Weapon.WeaponsEnum weapon;
        try {
            weapon = Weapon.WeaponsEnum.valueOf(newWeapon.replace(" ", "").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadUserInputException(
                    "Weapon not recognised, you can see a list of weapons using !char weapons list");
        }
        charDatabaseTable.update(
                new SetArgs(charDatabaseTable, Map.of(CharDatabaseFields.WEAPON.fieldName, weapon.toString())),
                new Args(charDatabaseTable, charDatabaseTable.getPrimaryKey(), getCharID(authorID, name)));
    }


    /**
     * @return attack flavour text and how much damage was dealt
     */
    public static String attack(@NotNull String authorID, @NotNull String characterName, @NotNull String victim) {
        final UserCharacter character = loadFromDatabase(authorID, characterName);
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


    private static int rollDamage(@NotNull UserCharacter character, boolean isNat20) {
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
    public static String getCharacterDescription(@NotNull String authorID, @NotNull String name) {
        return loadFromDatabase(authorID, name).getDescription();
    }


    public String getDescription() {
        String subraceString = "";
        if (subRace != null) {
            subraceString = subRace.toString().toLowerCase() + " ";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("```fix\n%s, %s, %s%s %s (%s), %d gp```", name, age, subraceString,
                                race.toPrintableString(), clazz.toPrintableString(), alignment, funds));
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
        sb.append(String.format("[Saving Throws]: %s\n", DiscordPrintable.getAsPrintableString(savingThrows)));
        sb.append(String.format(
                "[Skill Proficiencies]: %s\n",
                DiscordPrintable.getAsPrintableString(skillProficiencies)));
        sb.append(String.format("[Languages]: %s\n", DiscordPrintable.getAsPrintableString(languages)));
        sb.append(String.format("[Weapon Proficiencies]: %s\n", weaponProficiencies.toString()));
        sb.append(String.format("[Weapon]: %s```\n", weapon.toString().toLowerCase()));
        sb.append(background.getDescription());
        sb.append(String.format("\n\nFor as long as I can remember I've had %s", trinket));

        return sb.toString();
    }


    private static UserCharacter loadFromDatabase(@NotNull String userID, @NotNull String name) {
        final int charID = getCharID(userID, name);

        /*
         * Main character info
         */
        final UserCharacter character = (UserCharacter) charDatabaseTable.select(
                new Args(charDatabaseTable, charDatabaseTable.getPrimaryKey(), charID),
                rs -> {
                    rs.next();
                    try {
                        SubRace.SubRaceEnum subrace;
                        try {
                            subrace = SubRace.SubRaceEnum.valueOf(
                                    rs.getString(CharDatabaseFields.SUBRACE.fieldName));
                        } catch (NullPointerException e) {
                            subrace = null;
                        }

                        return new UserCharacter(
                                rs.getString(CharDatabaseFields.NAME.fieldName),
                                rs.getInt(CharDatabaseFields.AGE.fieldName),
                                rs.getString(CharDatabaseFields.ALIGNMENT.fieldName),
                                Clazz.ClassEnum.valueOf(rs.getString(CharDatabaseFields.CLAZZ.fieldName)),
                                Race.RaceEnum.valueOf(rs.getString(CharDatabaseFields.RACE.fieldName)),
                                subrace,
                                new UserBackground(
                                        Background.BackgroundEnum.valueOf(
                                                rs.getString(CharDatabaseFields.BACKGROUND.fieldName)),
                                        rs.getString(CharDatabaseFields.BACKGROUND_SPECIFICS.fieldName)),
                                rs.getString(CharDatabaseFields.TRINKET.fieldName),
                                rs.getInt(CharDatabaseFields.FUNDS.fieldName),
                                Weapon.WeaponsEnum.valueOf(rs.getString(CharDatabaseFields.WEAPON.fieldName))
                        );
                    } catch (IllegalArgumentException e) {
                        throw new ContactEwaException("Character load failed");
                    }
                });

        /*
         * Abilities and saving throws
         */
        abilitiesDatabaseTable.select(
                new Args(abilitiesDatabaseTable, CharAbilitiesDatabaseFields.CHAR_ID.fieldName, charID),
                rs -> {
                    final Map<CharacterConstants.AbilityEnum, Integer> abilitiesMap = new HashMap<>();
                    character.savingThrows = new HashSet<>();
                    while (rs.next()) {
                        final CharacterConstants.AbilityEnum ability = CharacterConstants.AbilityEnum.valueOf(
                                rs.getString(CharAbilitiesDatabaseFields.ABILITY.fieldName));
                        abilitiesMap.put(ability, rs.getInt(CharAbilitiesDatabaseFields.VALUE.fieldName));
                        if (rs.getBoolean(CharAbilitiesDatabaseFields.IS_SAVING_THROW.fieldName)) {
                            character.savingThrows.add(ability);
                        }
                    }
                    character.abilities = new Abilities(abilitiesMap);
                    return null;
                });

        /*
         * Weapon and skill proficiencies
         */
        proficienciesDatabaseTable.select(
                new Args(proficienciesDatabaseTable, CharProficienciesDatabaseFields.CHAR_ID.fieldName, charID),
                rs -> {
                    character.weaponProficiencies = new WeaponProficiencies();
                    character.skillProficiencies = new HashSet<>();
                    while (rs.next()) {
                        final String profType = rs.getString(CharProficienciesDatabaseFields.PROF_TYPE.fieldName);
                        final String prof = rs.getString(CharProficienciesDatabaseFields.PROFICIENCY.fieldName);
                        if (profType.equals(WeaponProficiencies.databaseTypeType)) {
                            character.weaponProficiencies.add(Weapon.WeaponProficiencyEnum.valueOf(prof));
                        }
                        else if (profType.equals(WeaponProficiencies.databaseTypeSpecific)) {
                            character.weaponProficiencies.add(Weapon.WeaponsEnum.valueOf(prof));
                        }
                        else {
                            character.skillProficiencies.add(CharacterConstants.SkillEnum.valueOf(prof));
                        }
                    }
                    return null;
                });

        /*
         * Languages
         */
        languagesDatabaseTable.select(
                new Args(languagesDatabaseTable, CharLanguagesDatabaseFields.CHAR_ID.fieldName, charID),
                rs -> {
                    character.languages = new HashSet<>();
                    while (rs.next()) {
                        character.languages.add(CharacterConstants.Language.valueOf(
                                rs.getString(CharLanguagesDatabaseFields.LANGUAGE.fieldName)));
                    }
                    return null;
                });
        return character;
    }


    /**
     * Die a specific stat, saving throw, or initiative
     *
     * @return the roll string e.g. "name rolled a 7 and crit"
     */
    public static String roll(@NotNull String authorID, @NotNull String name, @NotNull String args) {
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


    public void setClazz(@NotNull Clazz.ClassEnum clazz) {
        this.clazz = clazz;
    }


    public Race.RaceEnum getRace() {
        return race;
    }


    public void setRace(@NotNull Race.RaceEnum race) {
        this.race = race;
    }


    public void setSubRace(@NotNull SubRace.SubRaceEnum subRace) {
        if (race == null) {
            throw new ContactEwaException("You need a race before you can have a subrace");
        }
        Race.RaceEnum mainRace = Race.getRaceInfo(subRace).getMainRace();
        if (race == mainRace) {
            this.subRace = subRace;
        }
        else {
            throw new BadUserInputException("This subrace can only be chosen if your race is " + mainRace.toString());
        }
    }


    /**
     * Sets the character background and alignment (required for background generation)
     */
    public void setBackground(@NotNull Background.BackgroundEnum background) {
        final Alignment alignment = Race.getRaceInfo(race).getRandomAlignment();
        this.alignment = alignment.getAlignmentInitials();
        this.background = background.generateRandomBackground(alignment);
    }


    /**
     * Finish off character creation adding age, speed, level, languages, abilities, and proficiencies, then save the
     * character to the database
     */
    public void completeCreation(@NotNull String userID) {
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
    private void addSkillProficiencies(@NotNull Background backgroundInfo, @NotNull Race.RaceEnum race,
                                       @NotNull Clazz classInfo) {
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


    private void saveToDatabase(@NotNull String userID) {
        final SetArgs charArgs = new SetArgs(charDatabaseTable, Map.of(
                CharDatabaseFields.USERID.fieldName, userID, CharDatabaseFields.CLAZZ.fieldName, clazz.toString(),
                CharDatabaseFields.NAME.fieldName, name, CharDatabaseFields.ALIGNMENT.fieldName, alignment,
                CharDatabaseFields.AGE.fieldName, age, CharDatabaseFields.WEAPON.fieldName, weapon.toString(),
                CharDatabaseFields.BACKGROUND.fieldName, background.getBackgroundEnumVal().toString(),
                CharDatabaseFields.BACKGROUND_SPECIFICS.fieldName, background.getForSpecificsDatabase(),
                CharDatabaseFields.TRINKET.fieldName, trinket, CharDatabaseFields.RACE.fieldName, race.toString()));
        charArgs.addSetArgument(CharDatabaseFields.FUNDS.fieldName, funds);
        if (subRace != null) {
            charArgs.addSetArgument(CharDatabaseFields.SUBRACE.fieldName, subRace.toString());
        }
        charDatabaseTable.insert(charArgs);

        final int charID = getCharID(userID, name);
        for (CharacterConstants.AbilityEnum ability : CharacterConstants.AbilityEnum.values()) {
            abilitiesDatabaseTable.insert(new SetArgs(abilitiesDatabaseTable, Map.of(
                    CharAbilitiesDatabaseFields.CHAR_ID.fieldName, charID,
                    CharAbilitiesDatabaseFields.ABILITY.fieldName, ability.toString(),
                    CharAbilitiesDatabaseFields.VALUE.fieldName, abilities.getStat(ability),
                    CharAbilitiesDatabaseFields.IS_SAVING_THROW.fieldName, savingThrows.contains(ability))));
        }
        for (CharacterConstants.SkillEnum skill : skillProficiencies) {
            proficienciesDatabaseTable.insert(new SetArgs(proficienciesDatabaseTable, Map.of(
                    CharProficienciesDatabaseFields.CHAR_ID.fieldName, charID,
                    CharProficienciesDatabaseFields.PROF_TYPE.fieldName, "SKILL",
                    CharProficienciesDatabaseFields.PROFICIENCY.fieldName, skill.toString())));
        }
        for (SetArgs weaponProficienciesArgs : weaponProficiencies.toDatabaseArgs(proficienciesDatabaseTable,
                                                                                  CharProficienciesDatabaseFields.PROFICIENCY,
                                                                                  CharProficienciesDatabaseFields.PROF_TYPE)) {
            weaponProficienciesArgs.addSetArgument(CharProficienciesDatabaseFields.CHAR_ID.fieldName, charID);
            proficienciesDatabaseTable.insert(weaponProficienciesArgs);
        }
        for (CharacterConstants.Language language : languages) {
            languagesDatabaseTable.insert(new SetArgs(languagesDatabaseTable, Map.of(
                    CharLanguagesDatabaseFields.CHAR_ID.fieldName, charID,
                    CharLanguagesDatabaseFields.LANGUAGE.fieldName, language.toString())));
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
    private int getAbilityAttackModifier(@NotNull Weapon.AttackTypeEnum attackType) {
        switch (attackType) {
            case MELEE:
                return abilities.getModifier(CharacterConstants.AbilityEnum.STRENGTH);
            case RANGE:
                return abilities.getModifier(CharacterConstants.AbilityEnum.DEXTERITY);
            case FINESSE:
            default:
                if (abilities.getStat(CharacterConstants.AbilityEnum.STRENGTH) > abilities
                        .getStat(CharacterConstants.AbilityEnum.DEXTERITY)) {
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


    private enum CharDatabaseFields implements DatabaseTable.DatabaseField {
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



    private enum CharAbilitiesDatabaseFields implements DatabaseTable.DatabaseField {
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



    private enum CharProficienciesDatabaseFields implements DatabaseTable.DatabaseField {
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



    private enum CharLanguagesDatabaseFields implements DatabaseTable.DatabaseField {
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
}
