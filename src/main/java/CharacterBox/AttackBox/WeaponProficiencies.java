package main.java.CharacterBox.AttackBox;

import java.util.HashSet;
import java.util.Set;



public class WeaponProficiencies {
    private Set<Weapons.WeaponProficiency> typeProficency = new HashSet<>();
    private Set<Weapons.WeaponsEnum> specificProficency = new HashSet<>();

    public void add(Weapons.WeaponProficiency proficiency) {
        typeProficency.add(proficiency);
    }

    public void add(Weapons.WeaponsEnum proficiency) {
        specificProficency.add(proficiency);
    }

    public boolean contains(Weapons.WeaponProficiency proficiency) {
        return typeProficency.contains(proficiency);
    }

    public boolean contains(Weapons.WeaponsEnum proficiency) {
        return specificProficency.contains(proficiency);
    }
}
