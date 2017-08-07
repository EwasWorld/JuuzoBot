package main.java.CharacterBox.Attacking;

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


    public int getDamageQuantity() {
        return damageQuantity;
    }


    public int getDamageDie() {
        return damageDie;
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
