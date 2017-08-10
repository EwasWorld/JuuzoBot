package main.java.CharacterBox;


import main.java.CharacterBox.Attacking.Weapon;
import main.java.CharacterBox.Attacking.Weapons;
import main.java.CharacterBox.ClassBox.Class_;
import main.java.CharacterBox.ClassBox.Classes;
import main.java.CharacterBox.RaceBox.Race;
import main.java.CharacterBox.RaceBox.Races;
import main.java.Foo.Roll;

import java.util.*;



public class Character {
    private String name;
    private int age;
    private Classes.ClassEnum class_;
    private Races.RaceEnum race;
    private int level;
    private Map<AbilitySkillConstants.AbilityEnum, Integer> abilities;
    private Set<AbilitySkillConstants.AbilityEnum> savingThrows;
    private Set<AbilitySkillConstants.SkillEnum> skillProficiencies;
    private Set<CharacterConstants.Language> languages;
    private int funds;
    private Weapons.WeaponsEnum weapon;


    public Character(String name, Races.RaceEnum race, Classes.ClassEnum class_) {
        this.name = name;
        this.class_ = class_;
        this.race = race;
        level = 1;

        final Class_ classInfo = Classes.getClassInfo(class_);
        final Race raceInfo = Races.getRaceInfo(race);

        abilities = new HashMap<>();
        for (int i = 0; i < classInfo.getAbilityOrder().length; i++) {
            AbilitySkillConstants.AbilityEnum ability = classInfo.getAbilityOrder()[i];
            int score = AbilitySkillConstants.startingAbilityScores[i] + raceInfo.getAbilityIncreases().get(ability);

            abilities.put(ability, score);
        }

        savingThrows = classInfo.getSavingThrows();
        addSkillProficiencies(classInfo.getSkillProficiencies(), classInfo.getSkillProficienciesQuantity());
        weapon = classInfo.getStartWeapon();
        funds = classInfo.getFunds().rollFunds();

        age = new Random().nextInt(raceInfo.getAgeUpperBound() - raceInfo.getAgeLowerBound()) + raceInfo
                .getAgeLowerBound();
        languages = raceInfo.getLanguages();

        // TODO Accommodate multiple wildcards
        if (languages.contains(CharacterConstants.Language.WILDCARD)) {
            languages.remove(CharacterConstants.Language.WILDCARD);
            languages.add(CharacterConstants.getRandomLanguage(languages));
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


    public String getDescription() {
        String string = "";
        string += String.format("Name: %s\n", name);
        string += String.format("Race/Class: %s %s\n", race.toString(), class_.toString());
        string += String.format("Age: %s\n", age);
        string += String.format(
                "Stats: Str %d, Dex %d, Con %d, Int %d, Wis %d, Cha %d\n",
                abilities.get(AbilitySkillConstants.AbilityEnum.STRENGTH),
                abilities.get(AbilitySkillConstants.AbilityEnum.DEXTERITY),
                abilities.get(AbilitySkillConstants.AbilityEnum.CONSTITUTION),
                abilities.get(AbilitySkillConstants.AbilityEnum.INTELLIGENCE),
                abilities.get(AbilitySkillConstants.AbilityEnum.WISDOM),
                abilities.get(AbilitySkillConstants.AbilityEnum.CHARISMA)
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


    public int getAge() {
        return age;
    }


    public Classes.ClassEnum getClass_() {
        return class_;
    }


    public Races.RaceEnum getRace() {
        return race;
    }


    public Map<AbilitySkillConstants.AbilityEnum, Integer> getAbilities() {
        return abilities;
    }


    public Set<AbilitySkillConstants.AbilityEnum> getSavingThrows() {
        return savingThrows;
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


    public Set<AbilitySkillConstants.SkillEnum> getSkillProficiencies() {
        return skillProficiencies;
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


    public Set<CharacterConstants.Language> getLanguages() {
        return languages;
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


    public int getFunds() {
        return funds;
    }


    public Weapon getWeapon() {
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
                return AbilitySkillConstants.getModifier(abilities.get(AbilitySkillConstants.AbilityEnum.STRENGTH));
            case RANGE:
                return AbilitySkillConstants.getModifier(abilities.get(AbilitySkillConstants.AbilityEnum.DEXTERITY));
            case FINESSE:
            default:
                if (abilities.get(AbilitySkillConstants.AbilityEnum.STRENGTH) > abilities.get(AbilitySkillConstants.AbilityEnum.DEXTERITY)) {
                    return getAbilityAttackModifier(Weapons.AttackTypeEnum.MELEE);
                }
                else {
                    return getAbilityAttackModifier(Weapons.AttackTypeEnum.RANGE);
                }
        }
    }
}
