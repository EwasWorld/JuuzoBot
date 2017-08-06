package main.java.CharacterBox;


import main.java.CharacterBox.ClassBox.Classes;
import main.java.CharacterBox.RaceBox.Races;

import java.util.Map;
import java.util.Set;

public class Character {
    private String charName;
    private Classes.ClassEnum charClass;
    private Races.RaceEnum race;
    private Map<AbilitySkillConstants.AbilityEnum, Integer> Abilities;
    private Set<AbilitySkillConstants.AbilityEnum> savingThrows;
    private Set<AbilitySkillConstants.SkillEnum> skillProficiencies;
    private int funds;


    public Character(String charName, Classes.ClassEnum charClass, Races.RaceEnum race) {
        this.charName = charName;
        this.charClass = charClass;
        this.race = race;


    }


    private void createNewCharacter() {
        // TODO
    }
}
