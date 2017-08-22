package CharacterBox;


import CharacterBox.AttackBox.Weapon;
import CharacterBox.AttackBox.WeaponProficiencies;
import CharacterBox.BroardInfo.Class_;
import CharacterBox.BroardInfo.Race;
import CharacterBox.BroardInfo.SubRace;
import ExceptionsBox.BadUserInputException;
import Foo.Roll;

import java.io.Serializable;
import java.util.*;



public class Character implements Serializable {
    private String name;
    private int age;
    private Class_.ClassEnum class_;
    private Race.RaceEnum race;
    private SubRace.SubRaceEnum subRace;
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


    public Character(String name, Race.RaceEnum race, SubRace.SubRaceEnum subRace, Class_.ClassEnum class_) {
        this.name = name;
        this.class_ = class_;
        this.race = race;
        this.subRace = subRace;
        level = 1;

        final Class_ classInfo = Class_.getClassInfo(class_);
        final Race raceInfo = Race.getRaceInfo(race);
        final SubRace subRaceInfo = getSubraceInfo(subRace);

        final Map<CharacterConstants.AbilityEnum, Integer> abilitiesMap = new HashMap<>();
        for (int i = 0; i < classInfo.getAbilityOrder().length; i++) {
            CharacterConstants.AbilityEnum ability = classInfo.getAbilityOrder()[i];
            int score = CharacterConstants.startingAbilityScores[i]
                    + raceInfo.getAbilityIncreases(ability)
                    + subRaceInfo.getExtraAbilityIncreases(ability);
            abilitiesMap.put(ability, score);
        }
        abilities = new Abilities(abilitiesMap);

        savingThrows = classInfo.getSavingThrows();
        addSkillProficiencies(race, classInfo.getSkillProficiencies(), classInfo.getSkillQuantity());
        weaponProficiencies = classInfo.getWeaponProficiencies();
        weapon = classInfo.getStartWeapon();
        funds = classInfo.rollFunds();
        hp = classInfo.getHitDie() + abilities.getModifier(CharacterConstants.AbilityEnum.CONSTITUTION);

        age = raceInfo.generateRandomAge();
        speed = raceInfo.getSpeed();

        languages = raceInfo.getLanguages();
        if (languages.contains(CharacterConstants.Language.WILDCARD)) {
            languages.remove(CharacterConstants.Language.WILDCARD);
            languages.add(CharacterConstants.getRandomLanguage(languages));
        }

        addSubraceSubtleties(subRace);
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


    /*
     * Half elves get 2 extra random skill proficiencies
     * Half orcs get proficiency in intimidation
     */
    private void addSkillProficiencies(Race.RaceEnum race, Set<CharacterConstants.SkillEnum> possibleProficiencies,
                                       int quantity)
    {
        addSkillProficiencies(possibleProficiencies, quantity);
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
     * Choose a specified number of proficiencies from the given set
     */
    private void addSkillProficiencies(Set<CharacterConstants.SkillEnum> possibleProficiencies, int quantity) {
        skillProficiencies = new HashSet<>();
        for (int i = 0; i < quantity; i++) {
            int size = possibleProficiencies.size();
            final CharacterConstants.SkillEnum chosenSkill = possibleProficiencies
                    .toArray(new CharacterConstants.SkillEnum[size])[new Random().nextInt(size)];
            skillProficiencies.add(chosenSkill);
            possibleProficiencies.remove(chosenSkill);
        }
    }


    public String getDescription() {
        String subraceString = "";
        if (subRace != null) {
            subraceString = subRace.toString() + " ";
        }

        String string = "";
        string += String.format("Name: %s\n", name);
        string += String.format("Race/Class: %s%s %s\n", subraceString, race.toString(), class_.toString());
        string += String.format("Age: %s\n", age);
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
        string += String.format("FundsSetUp: %d\n", funds);
        string += String.format("Weapon Proficiencies: %s\n", weaponProficiencies.toString());
        string += String.format("Weapon: %s\n", weapon.toString());
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


    public String getName() {
        return name;
    }


    public Weapon getWeaponInfo() {
        return Weapon.getWeaponInfo(weapon);
    }


    /*
     * Returns an attack roll using the current weapon
     */
    public Roll.RollResult attackRoll() {
        return new Roll(1, 20, getAttackModifier()).roll();
    }


    /*
     * Returns the attack modifier for the current weapon
     */
    public int getAttackModifier() {
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
    public int rollDamage() {
        int damage = getAttackModifier() + getWeaponInfo().rollDamage();

        if (race == Race.RaceEnum.HALFORC) {
            damage += getWeaponInfo().rollOneDamageDie();
        }
        return damage;
    }


    /*
     * Returns a critical damage roll for the current weapon
     */
    public int rollCriticalDamage() {
        int damage = getAttackModifier() + getWeaponInfo().rollCriticalDamage();

        if (race == Race.RaceEnum.HALFORC) {
            damage += getWeaponInfo().rollOneDamageDie();
        }
        return damage;
    }


    public void changeWeapons(String newWeapon) {
        try {
            weapon = Weapon.WeaponsEnum.valueOf(newWeapon.replace(" ", "").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadUserInputException("Weapon not recognised, you can see a list of weapons using !weapons");
        }
    }


    public String rollInitiative() {
        final int modifier = abilities.getModifier(CharacterConstants.AbilityEnum.DEXTERITY);
        return new Roll(1, 20, modifier).getStringForRoll();
    }


    public String rollSavingThrow(CharacterConstants.AbilityEnum ability) {
        int modifier = abilities.getModifier(ability);

        if (savingThrows.contains(ability)) {
            modifier += CharacterConstants.getProficiencyBonus(level);
        }

        return new Roll(1, 20, modifier).getStringForRoll();
    }


    public String rollSkillCheck(CharacterConstants.SkillEnum skill) {
        int modifier = abilities.getModifier(skill.getMainAbility());

        if (skillProficiencies.contains(skill)) {
            modifier += CharacterConstants.getProficiencyBonus(level);
        }

        return new Roll(1, 20, modifier).getStringForRoll();
    }
}
