package CharacterBox.AttackBox;

import CharacterBox.CharacterConstants;
import CoreBox.Bot;
import CoreBox.Roll;
import com.google.gson.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static java.nio.file.Files.readAllBytes;



public class Weapon {
    public enum AttackTypeEnum {
        RANGE, MELEE, FINESSE
    }



    // TODO: Breath Weapons
    public enum WeaponProficiencyEnum {
        SIMPLE, MARTIAL
    }



    public enum WeaponsEnum {
        IMPROVISED, SHORTSWORD, LONGSWORD, SHORTBOW, LONGBOW, RAPIER, GREATAXE, MACE, CROSSBOW, LIGHTCROSSBOW, UNARMED,
        LIGHTHAMMER;


        static {
            IMPROVISED.weaponProficiency = WeaponProficiencyEnum.SIMPLE;
            MACE.weaponProficiency = WeaponProficiencyEnum.SIMPLE;
            LIGHTHAMMER.weaponProficiency = WeaponProficiencyEnum.SIMPLE;
            UNARMED.weaponProficiency = WeaponProficiencyEnum.SIMPLE;

            SHORTBOW.weaponProficiency = WeaponProficiencyEnum.SIMPLE;
            LIGHTCROSSBOW.weaponProficiency = WeaponProficiencyEnum.SIMPLE;

            SHORTSWORD.weaponProficiency = WeaponProficiencyEnum.MARTIAL;
            RAPIER.weaponProficiency = WeaponProficiencyEnum.MARTIAL;
            GREATAXE.weaponProficiency = WeaponProficiencyEnum.MARTIAL;
            LONGSWORD.weaponProficiency = WeaponProficiencyEnum.MARTIAL;

            LONGBOW.weaponProficiency = WeaponProficiencyEnum.MARTIAL;
            CROSSBOW.weaponProficiency = WeaponProficiencyEnum.MARTIAL;
        }


        private WeaponProficiencyEnum weaponProficiency;


        public WeaponProficiencyEnum getWeaponProficiency() {
            return weaponProficiency;
        }
    }



    public enum DamageType {BLUDGEONING, PIERCING, SLASHING}



    private static final String fileLocation = Bot.getResourceFilePath() + "Attacking/Weapons.json";
    private static Map<WeaponsEnum, Weapon> weapons;
    private AttackTypeEnum weaponTypeEnum;
    private int damageQuantity;
    private int damageDie;
    private String[] attackLines;
    private String[] hitLines;
    private String[] missLines;


    /*
     * Populates weapons map
     */
    private Weapon(Object rawObj) {
        weapons = new HashMap<>();

        for (JsonElement element : (JsonArray) rawObj) {
            final JsonObject object = (JsonObject) element;
            final WeaponsEnum weapon = WeaponsEnum
                    .valueOf(object.get("name").getAsString().replace(" ", "").toUpperCase());
            final JsonObject damage = object.getAsJsonObject("damage");

            weapons.put(weapon, new Weapon(
                    Weapon.AttackTypeEnum.valueOf(object.get("attackType").getAsString().toUpperCase()),
                    damage.get("quantity").getAsInt(),
                    damage.get("die").getAsInt(),
                    CharacterConstants.getStringArrayFromJsonArray(object.getAsJsonArray("attackLines")),
                    CharacterConstants.getStringArrayFromJsonArray(object.getAsJsonArray("hitLines")),
                    CharacterConstants.getStringArrayFromJsonArray(object.getAsJsonArray("missLines"))
            ));
        }
    }


    public Weapon(AttackTypeEnum weaponTypeEnum, int damageQuantity, int damageDie, String[] attackLines,
                  String[] hitLines, String[] missLines)
    {
        this.weaponTypeEnum = weaponTypeEnum;
        this.damageQuantity = damageQuantity;
        this.damageDie = damageDie;
        this.attackLines = attackLines;
        this.hitLines = hitLines;
        this.missLines = missLines;
    }


    public static Weapon getWeaponInfo(WeaponsEnum weapon) {
        if (weapons == null) {
            getWeaponsFromFile();
        }
        return weapons.get(weapon);
    }


    private static void getWeaponsFromFile() {
        try {
            final GsonBuilder gsonBuilder = new GsonBuilder();
            final Gson gson = gsonBuilder.registerTypeAdapter(Weapon.class, new WeaponDeserializer()).create();

            final String weaponsJSON = new String(readAllBytes(Paths.get(fileLocation)));
            gson.fromJson(weaponsJSON, Weapon.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String getWeaponsList() {
        if (weapons == null) {
            getWeaponsFromFile();
        }

        String weapons = "Available weapons: ";
        final WeaponsEnum[] weaponsEnums = WeaponsEnum.values();

        for (int i = 0; i < weaponsEnums.length; i++) {
            weapons += weaponsEnums[i].toString();

            if (i < weaponsEnums.length - 1) {
                weapons += ", ";
            }
        }

        return weapons;
    }


    public AttackTypeEnum getWeaponAttackTypeEnum() {
        return weaponTypeEnum;
    }


    /*
     * Roll the weapon's damage die and return the result (no modifier)
     */
    public int rollDamage() {
        return new Roll(damageQuantity, damageDie, 0).roll().getResult();
    }


    /*
     * Roll the weapon's critical damage die and return the result (no modifier)
     */
    public int rollCriticalDamage() {
        return new Roll(damageQuantity + 1, damageDie, 0).roll().getResult();
    }


    /*
     * Returns a random flavour line for an attack roll
     */
    public String getAttackLine() {
        return attackLines[new Random().nextInt(attackLines.length)];
    }


    /*
     * Returns a random flavour line for a hit
     */
    public String getHitLine() {
        return hitLines[new Random().nextInt(hitLines.length)];
    }


    /*
     * Returns a random flavour line for a miss
     */
    public String getMissLine() {
        return missLines[new Random().nextInt(missLines.length)];
    }


    public int rollOneDamageDie() {
        return Roll.quickRoll(damageDie);
    }


    private static class WeaponDeserializer implements JsonDeserializer<Weapon> {
        public Weapon deserialize(JsonElement json, Type typeOfT,
                                  JsonDeserializationContext context) throws JsonParseException
        {
            return new Weapon(json.getAsJsonObject().get("weapons").getAsJsonArray());
        }
    }
}
