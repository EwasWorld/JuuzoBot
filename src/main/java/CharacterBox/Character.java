package main.java.CharacterBox;


import main.java.CharacterBox.Attacking.Weapons;
import main.java.CharacterBox.ClassBox.Class_;
import main.java.CharacterBox.ClassBox.Classes;
import main.java.CharacterBox.ClassBox.Funds;
import main.java.CharacterBox.RaceBox.Race;
import main.java.CharacterBox.RaceBox.Races;
import main.java.Foo.DiceRoller;

import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Character {
    private String name;
    private int age;
    private Classes.ClassEnum class_;
    private Races.RaceEnum race;
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

        final Class_ classInfo = Classes.getClassInfo(class_);
        final Race raceInfo = Races.getRaceInfo(race);

        for (int i = 0; i < classInfo.getAbilityOrder().length; i++) {
            AbilitySkillConstants.AbilityEnum ability = classInfo.getAbilityOrder()[i];
            int score = AbilitySkillConstants.startingAbilityScores[i] + raceInfo.getAbilityIncreases().get(ability);

            abilities.put(ability, score);
        }

        savingThrows = classInfo.getSavingThrows();
        skillProficiencies = classInfo.getSkillProficiencies();
        weapon = classInfo.getStartWeapon();

        Funds fundsSetUp = classInfo.getFunds();
        funds = DiceRoller.roll(fundsSetUp.getQuantity(), 4, 0);
        if (fundsSetUp.isMultiply()) {
            funds *= 10;
        }

        age = new Random().nextInt(raceInfo.getAgeUpperBound() - raceInfo.getAgeLowerBound()) + raceInfo.getAgeLowerBound();
        languages = raceInfo.getLanguages();

        // TODO Accommodate multiple wildcards
        if (languages.contains(CharacterConstants.Language.WILDCARD)) {
            languages.remove(CharacterConstants.Language.WILDCARD);
            languages.add(CharacterConstants.getRandomLanguage(languages));
        }
    }
}
