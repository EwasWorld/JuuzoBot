package main.java.Foo;


import main.java.CharClassBox.CharClass;
import main.java.Const.AbilitySkillConstants;

import java.util.Map;
import java.util.Set;

public class Character {
    private String charName;
    private CharClass.ClassEnum charClass;
    private main.java.RaceBox.Race.RaceEnum race;
    private Map<AbilitySkillConstants.AbilityEnum, Integer> Abilities;
    private Set<AbilitySkillConstants.AbilityEnum> savingThrows;
    private Set<AbilitySkillConstants.SkillEnum> skillProficiencies;

    private void createNewCharacter() {
        // TODO
    }
}
