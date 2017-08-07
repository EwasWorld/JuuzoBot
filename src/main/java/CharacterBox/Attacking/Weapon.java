package main.java.CharacterBox.Attacking;

public class Weapon {
    private Weapons.WeaponTypeEnum weaponTypeEnum;
    private String[] attackLines;
    private String[] hitLines;
    private String[] missLines;


    public Weapon(Weapons.WeaponTypeEnum weaponTypeEnum, String[] attackLines, String[] hitLines,
                  String[] missLines)
    {
        this.weaponTypeEnum = weaponTypeEnum;
        this.attackLines = attackLines;
        this.hitLines = hitLines;
        this.missLines = missLines;
    }
}
