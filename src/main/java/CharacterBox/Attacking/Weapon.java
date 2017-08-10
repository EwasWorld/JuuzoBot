package main.java.CharacterBox.Attacking;

import main.java.Foo.Roll;

import java.util.Random;



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


    public Weapons.AttackTypeEnum getWeaponAttackTypeEnum() {
        return weaponTypeEnum;
    }


    public int rollDamage() {
        return new Roll(damageQuantity, damageDie, 0).roll().getResult();
    }


    public int rollCriticalDamage() {
        return new Roll(damageQuantity + 1, damageDie, 0).roll().getResult();
    }


    public String getAttackLine() {
        return attackLines[new Random().nextInt(attackLines.length)];
    }


    public String getHitLine() {
        return hitLines[new Random().nextInt(hitLines.length)];
    }


    public String getMissLine() {
        return missLines[new Random().nextInt(missLines.length)];
    }
}
