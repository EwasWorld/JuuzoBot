package main.java.CharacterBox.ClassBox;

import main.java.CharacterBox.AbilitySkillConstants;
import main.java.CharacterBox.Attacking.Weapons;

import java.util.Optional;
import java.util.Set;



public class Class_ {
    private String secondary;
    private int hitDie;
    private AbilitySkillConstants.AbilityEnum[] abilityOrder;
    private Set<AbilitySkillConstants.AbilityEnum> savingThrows;
    private Set<AbilitySkillConstants.SkillEnum> skillProficiencies;
    private Funds funds;
    private Weapons.WeaponsEnum startWeapon;


    public Class_(String secondary, int hitDie, AbilitySkillConstants.AbilityEnum[] abilityOrder,
                  Set<AbilitySkillConstants.AbilityEnum> savingThrows,
                  Set<AbilitySkillConstants.SkillEnum> skillProficiencies, Funds funds,
                  Weapons.WeaponsEnum startWeapon)
    {
        this.secondary = secondary;
        this.hitDie = hitDie;
        this.abilityOrder = abilityOrder;
        this.savingThrows = savingThrows;
        this.skillProficiencies = skillProficiencies;
        this.funds = funds;
        this.startWeapon = startWeapon;
    }


    public Optional<String> getSecondary() {
        return Optional.of(secondary);
    }


    public int getHitDie() {
        return hitDie;
    }


    public AbilitySkillConstants.AbilityEnum[] getAbilityOrder() {
        return abilityOrder;
    }


    public Set<AbilitySkillConstants.AbilityEnum> getSavingThrows() {
        return savingThrows;
    }


    public Set<AbilitySkillConstants.SkillEnum> getSkillProficiencies() {
        return skillProficiencies;
    }


    public Funds getFunds() {
        return funds;
    }


    public Weapons.WeaponsEnum getStartWeapon() {
        return startWeapon;
    }
}
