package CharacterBox;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;



public class Abilities implements Serializable {
    private Map<CharacterConstants.AbilityEnum, Integer> abilities = new HashMap<>();


    public Abilities(CharacterConstants.AbilityEnum[] abilityOrder) {
        for (int i = 0; i < abilityOrder.length; i++) {
            abilities.put(abilityOrder[i], CharacterConstants.startingAbilityScores[i]);
        }
    }


    public int getStat(CharacterConstants.AbilityEnum ability) {
        return abilities.get(ability);
    }


    public int getModifier(CharacterConstants.AbilityEnum ability) {
        return CharacterConstants.getModifier(abilities.get(ability));
    }


    public void addIncreases(Map<CharacterConstants.AbilityEnum, Integer> increases) {
        for (CharacterConstants.AbilityEnum ability : CharacterConstants.AbilityEnum.values()) {
            abilities.put(ability, abilities.get(ability) + increases.get(ability));
        }
    }
}
