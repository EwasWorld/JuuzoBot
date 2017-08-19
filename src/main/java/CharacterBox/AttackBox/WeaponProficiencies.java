package CharacterBox.AttackBox;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;



public class WeaponProficiencies implements Serializable {
    private Set<Weapon.WeaponProficiencyEnum> typeProficiencies = new HashSet<>();
    private Set<Weapon.WeaponsEnum> specificProficiencies = new HashSet<>();


    public void add(Weapon.WeaponProficiencyEnum proficiency) {
        typeProficiencies.add(proficiency);
    }


    public void add(Weapon.WeaponsEnum proficiency) {
        specificProficiencies.add(proficiency);
    }


    public boolean contains(Weapon.WeaponsEnum proficiency) {
        return specificProficiencies.contains(proficiency)
                || typeProficiencies.contains(proficiency.getWeaponProficiency());
    }


    public String toString() {
        String string = "";
        for (Weapon.WeaponProficiencyEnum weaponProficiency : typeProficiencies) {
            string += weaponProficiency.toString() + ", ";
        }
        for (Weapon.WeaponsEnum weapon : specificProficiencies) {
            string += weapon.toString() + ", ";
        }

        return string.substring(0, string.length() - 2);
    }
}
