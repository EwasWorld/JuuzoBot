package main.java.CharacterBox;


import main.java.CharacterBox.AttackBox.Weapon;
import main.java.CharacterBox.AttackBox.Weapons;
import main.java.CharacterBox.ClassBox.Class_;
import main.java.CharacterBox.ClassBox.Classes;
import main.java.CharacterBox.RaceBox.Race;
import main.java.CharacterBox.RaceBox.Races;
import main.java.CharacterBox.RaceBox.SubRace;
import main.java.Foo.Roll;

import java.util.*;



public class Character {
    private String name;
    private int age;
    private Classes.ClassEnum class_;
    private Races.RaceEnum race;
    private SubRace.SubRaceEnum subRace;
    private int level;
    private int hp;
    private int speed;
    private Abilities abilities;
    private Set<AbilitySkillConstants.AbilityEnum> savingThrows;
    private Set<AbilitySkillConstants.SkillEnum> skillProficiencies;
    private Set<CharacterConstants.Language> languages;
    private int funds;
    private Weapons.WeaponsEnum weapon;


    public Character(String name, Races.RaceEnum race, SubRace.SubRaceEnum subRace, Classes.ClassEnum class_) {
        this.name = name;
        this.class_ = class_;
        this.race = race;
        this.subRace = subRace;
        level = 1;

        final Class_ classInfo = Classes.getClassInfo(class_);
        final Race raceInfo = Races.getRaceInfo(race);
        final SubRace subRaceInfo = getSubraceInfo(subRace);

        Map<AbilitySkillConstants.AbilityEnum, Integer> abilitiesMap = new HashMap<>();
        for (int i = 0; i < classInfo.getAbilityOrder().length; i++) {
            AbilitySkillConstants.AbilityEnum ability = classInfo.getAbilityOrder()[i];
            int score = AbilitySkillConstants.startingAbilityScores[i]
                    + raceInfo.getAbilityIncreases(ability)
                    + subRaceInfo.getExtraAbilityIncreases(ability);
            abilitiesMap.put(ability, score);
        }
        abilities = new Abilities(abilitiesMap);

        savingThrows = classInfo.getSavingThrows();
        addSkillProficiencies(classInfo.getSkillProficiencies(), classInfo.getSkillProficienciesQuantity());
        weapon = classInfo.getStartWeapon();
        funds = classInfo.getFunds().rollFunds();
        hp = classInfo.getHitDie() + abilities.getModifier(AbilitySkillConstants.AbilityEnum.CONSTITUTION);

        age = new Random().nextInt(raceInfo.getAgeUpperBound() - raceInfo.getAgeLowerBound()) + raceInfo
                .getAgeLowerBound();
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
            SubRace subRace = Races.getRaceInfo(subRaceEnum);
            if (race != subRace.getMainRace()) {
                // TODO: Handle
                throw new IllegalArgumentException("Invalid subrace");
            }
            else {
                return subRace;
            }
        }
        else {
            return new SubRace();
        }
    }


    private void addSkillProficiencies(Set<AbilitySkillConstants.SkillEnum> possibleProficiencies, int quantity) {
        Set<AbilitySkillConstants.SkillEnum> skillProficiencies = new HashSet<>();
        for (int i = 0; i < quantity; i++) {
            int size = possibleProficiencies.size();
            AbilitySkillConstants.SkillEnum chosenSkill = possibleProficiencies
                    .toArray(new AbilitySkillConstants.SkillEnum[size])[new Random().nextInt(size)];
            skillProficiencies.add(chosenSkill);
            possibleProficiencies.remove(chosenSkill);
        }
        this.skillProficiencies = skillProficiencies;
    }


    private void addSubraceSubtleties(SubRace.SubRaceEnum subRace) {
        if (subRace != null) {
            switch (subRace) {
                case HIGH:
                    languages.add(CharacterConstants.getRandomLanguage(languages));
                    break;
                case HILL:
                    hp++;
                    break;
                case WOOD:
                    speed = 35;
                    break;
            }
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
                abilities.getStat(AbilitySkillConstants.AbilityEnum.STRENGTH),
                abilities.getStat(AbilitySkillConstants.AbilityEnum.DEXTERITY),
                abilities.getStat(AbilitySkillConstants.AbilityEnum.CONSTITUTION),
                abilities.getStat(AbilitySkillConstants.AbilityEnum.INTELLIGENCE),
                abilities.getStat(AbilitySkillConstants.AbilityEnum.WISDOM),
                abilities.getStat(AbilitySkillConstants.AbilityEnum.CHARISMA)
        );
        string += String.format("Saving Throws: %s\n", getSavingThrowsAsString());
        string += String.format("Skill Proficiencies: %s\n", getSkillProficienciesAsString());
        string += String.format("Languages: %s\n", getLanguagesAsString());
        string += String.format("Funds: %d\n", funds);
        string += String.format("Weapon: %s\n", weapon.toString());
        return string;
    }


    public String getName() {
        return name;
    }


    private String getSavingThrowsAsString() {
        String string = "";
        for (AbilitySkillConstants.AbilityEnum ability : savingThrows) {
            string += ability.toString() + ", ";
        }
        string = string.trim();
        string = string.substring(0, string.length() - 1);

        return string;
    }


    private String getSkillProficienciesAsString() {
        String string = "";
        for (AbilitySkillConstants.SkillEnum skill : skillProficiencies) {
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


    public Weapon getWeaponInfo() {
        return Weapons.getWeaponInfo(weapon);
    }


    public Roll.RollResult attackRoll() {
        return new Roll(1, 20, getAttackModifier()).roll();
    }


    public int getAttackModifier() {
        int modifier = AbilitySkillConstants.getProficiencyBonus(level);
        return modifier + getAbilityAttackModifier(Weapons.getWeaponInfo(weapon).getWeaponAttackTypeEnum());
    }


    private int getAbilityAttackModifier(Weapons.AttackTypeEnum attackType) {
        switch (attackType) {
            case MELEE:
                return abilities.getModifier(AbilitySkillConstants.AbilityEnum.STRENGTH);
            case RANGE:
                return abilities.getModifier(AbilitySkillConstants.AbilityEnum.DEXTERITY);
            case FINESSE:
            default:
                if (abilities.getStat(AbilitySkillConstants.AbilityEnum.STRENGTH) > abilities
                        .getStat(AbilitySkillConstants.AbilityEnum.DEXTERITY))
                {
                    return getAbilityAttackModifier(Weapons.AttackTypeEnum.MELEE);
                }
                else {
                    return getAbilityAttackModifier(Weapons.AttackTypeEnum.RANGE);
                }
        }
    }
}
