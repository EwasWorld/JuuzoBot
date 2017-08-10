package main.java.CharacterBox;

import java.util.Map;



public class CharacterAbilities {
    Map<AbilitySkillConstants.AbilityEnum, Integer> abilities;


    public CharacterAbilities(Map<AbilitySkillConstants.AbilityEnum, Integer> abilities) {
        this.abilities = abilities;
    }

    public int getStat(AbilitySkillConstants.AbilityEnum ability) {
        return abilities.get(ability);
    }

    public int getModifier(AbilitySkillConstants.AbilityEnum ability) {
        return AbilitySkillConstants.getModifier(abilities.get(ability));
    }
}
