package main.java.CharacterBox.ClassBox;

import main.java.CharacterBox.AbilitySkillConstants;
import main.java.CharacterBox.Attacking.Weapons;

import java.util.Optional;
import java.util.Set;



public class Class_ {
    private int hitDie;
    private AbilitySkillConstants.AbilityEnum[] abilityOrder;
    private Set<AbilitySkillConstants.AbilityEnum> savingThrows;
    private int skillProficienciesQuantity;
    private Set<AbilitySkillConstants.SkillEnum> skillProficiencies;
    private Funds funds;
    private Weapons.WeaponsEnum startWeapon;


    public Class_(int hitDie, AbilitySkillConstants.AbilityEnum[] abilityOrder,
                  Set<AbilitySkillConstants.AbilityEnum> savingThrows, int skillProficienciesQuantity,
                  Set<AbilitySkillConstants.SkillEnum> skillProficiencies, Funds funds,
                  Weapons.WeaponsEnum startWeapon)
    {
        this.hitDie = hitDie;
        this.abilityOrder = abilityOrder;
        this.savingThrows = savingThrows;
        this.skillProficienciesQuantity = skillProficienciesQuantity;
        this.skillProficiencies = skillProficiencies;
        this.funds = funds;
        this.startWeapon = startWeapon;
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


    public int getSkillProficienciesQuantity() {
        return skillProficienciesQuantity;
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
