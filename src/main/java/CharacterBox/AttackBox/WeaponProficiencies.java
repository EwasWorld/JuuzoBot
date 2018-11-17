package CharacterBox.AttackBox;

import DatabaseBox.DatabaseTable;

import java.io.Serializable;
import java.util.*;



public class WeaponProficiencies implements Serializable {
    public static String databaseTypeType = "WEAPONTYPE";
    public static String databaseTypeSpecific = "WEAPON";
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


    public List<Map<String, Object>> toDatabaseArgs(DatabaseTable.DatabaseFieldsEnum proficiencyFieldName, DatabaseTable.DatabaseFieldsEnum typeField) {
        final List<Map<String, Object>> args = new ArrayList<>();
        for (Weapon.WeaponProficiencyEnum proficiency : typeProficiencies) {
            final Map<String, Object> proficiencyArgs = new HashMap<>();
            proficiencyArgs.put(typeField.getFieldName(), databaseTypeType);
            proficiencyArgs.put(proficiencyFieldName.getFieldName(), proficiency.toString());
            args.add(proficiencyArgs);
        }
        for (Weapon.WeaponsEnum proficiency : specificProficiencies) {
            final Map<String, Object> proficiencyArgs = new HashMap<>();
            proficiencyArgs.put(typeField.getFieldName(), databaseTypeSpecific);
            proficiencyArgs.put(proficiencyFieldName.getFieldName(), proficiency.toString());
            args.add(proficiencyArgs);
        }
        return args;
    }


    public String toString() {
        StringBuilder string = new StringBuilder();
        for (Weapon.WeaponProficiencyEnum weaponProficiency : typeProficiencies) {
            string.append(weaponProficiency.toString()).append(", ");
        }
        for (Weapon.WeaponsEnum weapon : specificProficiencies) {
            string.append(weapon.toString()).append(", ");
        }

        return string.substring(0, string.length() - 2).toLowerCase();
    }
}
