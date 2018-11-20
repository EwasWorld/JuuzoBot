package CharacterBox.AttackBox;

import CharacterBox.CharacterConstants;
import CharacterBox.DiscordPrintable;
import CoreBox.Bot;
import CoreBox.Die;
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



    // TODO Implement Breath Weapons
    public enum WeaponProficiencyEnum {
        SIMPLE, MARTIAL
    }



    public enum WeaponsEnum implements DiscordPrintable {
        IMPROVISED(WeaponProficiencyEnum.SIMPLE), SHORTSWORD(WeaponProficiencyEnum.MARTIAL),
        LONGSWORD(WeaponProficiencyEnum.MARTIAL), SHORTBOW(WeaponProficiencyEnum.SIMPLE),
        LONGBOW(WeaponProficiencyEnum.MARTIAL), RAPIER(WeaponProficiencyEnum.MARTIAL),
        GREATAXE(WeaponProficiencyEnum.MARTIAL), MACE(WeaponProficiencyEnum.SIMPLE),
        CROSSBOW(WeaponProficiencyEnum.MARTIAL), LIGHTCROSSBOW(WeaponProficiencyEnum.SIMPLE),
        UNARMED(WeaponProficiencyEnum.SIMPLE), LIGHTHAMMER(WeaponProficiencyEnum.SIMPLE);


        private WeaponProficiencyEnum weaponProficiency;


        WeaponsEnum(WeaponProficiencyEnum weaponProficiency) {
            this.weaponProficiency = weaponProficiency;
        }


        public WeaponProficiencyEnum getWeaponProficiency() {
            return weaponProficiency;
        }


        @Override
        public String toPrintableString() {
            return toString().toLowerCase();
        }
    }



    // TODO Implement damage types
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
                  String[] hitLines, String[] missLines) {
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
        return "Available weapons: " + DiscordPrintable.getAsPrintableString(WeaponsEnum.values());
    }


    public AttackTypeEnum getWeaponAttackTypeEnum() {
        return weaponTypeEnum;
    }


    /*
     * Die the weapon's damage die and return the result (no modifier)
     */
    public int rollDamage() {
        return new Die(damageQuantity, damageDie, 0).roll().getTotal();
    }


    /*
     * Die the weapon's critical damage die and return the result (no modifier)
     */
    public int rollCriticalDamage() {
        return new Die(damageQuantity + 1, damageDie, 0).roll().getTotal();
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
        return Die.quickRoll(damageDie);
    }


    private static class WeaponDeserializer implements JsonDeserializer<Weapon> {
        public Weapon deserialize(JsonElement json, Type typeOfT,
                                  JsonDeserializationContext context) throws JsonParseException {
            return new Weapon(json.getAsJsonObject().get("weapons").getAsJsonArray());
        }
    }
}
