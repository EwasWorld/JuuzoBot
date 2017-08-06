package main.java.CharacterBox;


import main.java.CharacterBox.ClassBox.Class_;
import main.java.CharacterBox.ClassBox.Classes;
import main.java.CharacterBox.RaceBox.Race;
import main.java.CharacterBox.RaceBox.Races;

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


    public Character(String name, Classes.ClassEnum class_, Races.RaceEnum race) {
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

        // TODO funds

        age = new Random().nextInt(raceInfo.getAgeUpperBound() - raceInfo.getAgeLowerBound()) + raceInfo.getAgeLowerBound();
        languages = raceInfo.getLanguages();

        // TODO Accommodate multiple wildcards
        if (languages.contains(CharacterConstants.Language.WILDCARD)) {
            languages.remove(CharacterConstants.Language.WILDCARD);
            languages.add(CharacterConstants.getRandomLanguage(languages));
        }
    }
}
