package CharacterBox.AttackBox;

import DatabaseBox.DatabaseTable;
import DatabaseBox.SetArgs;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;



public class WeaponProficiencies implements Serializable {
    public static String databaseTypeType = "WEAPONTYPE";
    public static String databaseTypeSpecific = "WEAPON";
    private Set<Weapon.WeaponProficiencyEnum> typeProficiencies = new HashSet<>();
    private Set<Weapon.WeaponsEnum> specificProficiencies = new HashSet<>();


    public void add(@NotNull Weapon.WeaponProficiencyEnum proficiency) {
        typeProficiencies.add(proficiency);
    }


    public void add(@NotNull Weapon.WeaponsEnum proficiency) {
        specificProficiencies.add(proficiency);
    }


    public boolean contains(@NotNull Weapon.WeaponsEnum proficiency) {
        return specificProficiencies.contains(proficiency)
                || typeProficiencies.contains(proficiency.getWeaponProficiency());
    }


    public List<SetArgs> toDatabaseArgs(@NotNull DatabaseTable databaseTable,
                                        @NotNull DatabaseTable.DatabaseField proficiencyFieldName,
                                        @NotNull DatabaseTable.DatabaseField typeField) {
        final List<SetArgs> args = new ArrayList<>();
        for (Weapon.WeaponProficiencyEnum proficiency : typeProficiencies) {
            args.add(new SetArgs(databaseTable, Map.of(typeField.getFieldName(), databaseTypeType,
                                                       proficiencyFieldName.getFieldName(), proficiency.toString())));
        }
        for (Weapon.WeaponsEnum proficiency : specificProficiencies) {
            args.add(new SetArgs(databaseTable, Map.of(typeField.getFieldName(), databaseTypeSpecific,
                                                       proficiencyFieldName.getFieldName(), proficiency.toString())));
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
