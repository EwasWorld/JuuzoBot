package CharacterBox.AttackBox;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;



public class WeaponProficiencies implements Serializable {
    private Set<Weapons.WeaponProficiency> typeProficiencies = new HashSet<>();
    private Set<Weapons.WeaponsEnum> specificProficiencies = new HashSet<>();

    public void add(Weapons.WeaponProficiency proficiency) {
        typeProficiencies.add(proficiency);
    }

    public void add(Weapons.WeaponsEnum proficiency) {
        specificProficiencies.add(proficiency);
    }

    public boolean contains(Weapons.WeaponsEnum proficiency) {
        return specificProficiencies.contains(proficiency)
                || typeProficiencies.contains(proficiency.getWeaponProficiency());
    }

    public String toString() {
        String string = "";
        for (Weapons.WeaponProficiency weaponProficiency : typeProficiencies) {
            string += weaponProficiency.toString() + ", ";
        }
        for (Weapons.WeaponsEnum weapon : specificProficiencies) {
            string += weapon.toString() + ", ";
        }

        return string.substring(0, string.length() - 2);
    }
}
