package main.java.CharacterBox.Attacking;

import main.java.Foo.Roll;



public class Weapon {
    private Weapons.AttackTypeEnum weaponTypeEnum;
    private int damageQuantity;
    private int damageDie;
    private String[] attackLines;
    private String[] hitLines;
    private String[] missLines;


    public Weapon(Weapons.AttackTypeEnum weaponTypeEnum, int damageQuantity, int damageDie, String[] attackLines,
                  String[] hitLines, String[] missLines)
    {
        this.weaponTypeEnum = weaponTypeEnum;
        this.damageQuantity = damageQuantity;
        this.damageDie = damageDie;
        this.attackLines = attackLines;
        this.hitLines = hitLines;
        this.missLines = missLines;
    }


    public Weapons.AttackTypeEnum getWeaponTypeEnum() {
        return weaponTypeEnum;
    }


    public Roll.RollResult rollDamage() {
        return new Roll(damageQuantity, damageDie, 0).roll();
    }


    public Roll.RollResult rollCriticalDamage() {
        return new Roll(damageQuantity + 1, damageDie, 0).roll();
    }


    public String[] getAttackLines() {
        return attackLines;
    }


    public String[] getHitLines() {
        return hitLines;
    }


    public String[] getMissLines() {
        return missLines;
    }
}
