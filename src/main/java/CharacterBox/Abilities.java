package CharacterBox;

import java.io.Serializable;
import java.util.Map;



public class Abilities implements Serializable {
    private Map<CharacterConstants.AbilityEnum, Integer> abilities;


    public Abilities(Map<CharacterConstants.AbilityEnum, Integer> abilities) {
        this.abilities = abilities;
    }


    public int getStat(CharacterConstants.AbilityEnum ability) {
        return abilities.get(ability);
    }


    public int getModifier(CharacterConstants.AbilityEnum ability) {
        return CharacterConstants.getModifier(abilities.get(ability));
    }
}
