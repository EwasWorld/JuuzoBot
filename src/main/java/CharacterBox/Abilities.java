package CharacterBox;

import java.io.Serializable;
import java.util.Map;



public class Abilities implements Serializable {
    Map<AbilitySkillConstants.AbilityEnum, Integer> abilities;


    public Abilities(Map<AbilitySkillConstants.AbilityEnum, Integer> abilities) {
        this.abilities = abilities;
    }

    public int getStat(AbilitySkillConstants.AbilityEnum ability) {
        return abilities.get(ability);
    }

    public int getModifier(AbilitySkillConstants.AbilityEnum ability) {
        return AbilitySkillConstants.getModifier(abilities.get(ability));
    }
}
