package main.java.Foo;


import main.java.CharClassBox.Classes;
import main.java.Const.AbilitySkillConstants;
import main.java.RaceBox.Races;

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
