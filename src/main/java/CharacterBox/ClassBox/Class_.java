package CharacterBox.ClassBox;

import CharacterBox.AbilitySkillConstants;
import CharacterBox.AttackBox.Weapon;
import CharacterBox.AttackBox.WeaponProficiencies;
import Foo.IDs;
import com.google.gson.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.nio.file.Files.readAllBytes;



public class Class_ {
    public enum ClassEnum {
        BARBARIAN, BARD, CLERIC, DRUID, FINESSEFIGHTER,
        FIGHTER, MONK, PALADIN, RANGER, ROGUE,
        SORCERER, WARLOCK, WIZARD
    }



    private static final String fileLocation = IDs.mainFilePath + "CharacterBox/ClassBox/Classes.json";
    private static Map<ClassEnum, Class_> classes;
    private int hitDie;
    // [0] will be the highest stat
    private AbilitySkillConstants.AbilityEnum[] abilityOrder;
    private Set<AbilitySkillConstants.AbilityEnum> savingThrows;
    // The number of skill proficiencies to be chosen from skillProficiencies
    private int skillQuantity;
    // Possible skill proficiencies
    private Set<AbilitySkillConstants.SkillEnum> skillProficiencies;
    private FundsSetUp fundsSetUp;
    private WeaponProficiencies weaponProficiencies;
    private Weapon.WeaponsEnum startWeapon;


    private Class_(Object rawObj) {
        classes = new HashMap<>();

        for (JsonElement element : (JsonArray) rawObj) {
            final JsonObject object = (JsonObject) element;

            final ClassEnum classEnum = ClassEnum.valueOf(object.get("name").getAsString().toUpperCase());
            final JsonObject funds = object.getAsJsonObject("fundsSetUp");
            classes.put(classEnum, new Class_(
                    object.get("hitDie").getAsInt(),
                    createAbilityOrder(object.getAsJsonObject("abilityOrder")),
                    createSavingThrows(object.getAsJsonArray("savingThrows")),
                    object.get("skillQuantity").getAsInt(),
                    createSkillProficiencies(object.getAsJsonArray("skillProficiencies")),
                    new FundsSetUp(
                            funds.get("quantity").getAsInt(),
                            funds.get("multiply").getAsBoolean()
                    ),
                    createWeaponProficiencies(object.getAsJsonArray("weaponProficiencies")),
                    Weapon.WeaponsEnum.valueOf(object.get("startWeapon").getAsString().toUpperCase())
            ));
        }
    }


    private static AbilitySkillConstants.AbilityEnum[] createAbilityOrder(JsonObject abilityOrderObj) {
        final AbilitySkillConstants.AbilityEnum[] newAbilityOrder = new AbilitySkillConstants.AbilityEnum[6];
        newAbilityOrder[abilityOrderObj.get("str").getAsInt() - 1] = AbilitySkillConstants.AbilityEnum.STRENGTH;
        newAbilityOrder[abilityOrderObj.get("dex").getAsInt() - 1] = AbilitySkillConstants.AbilityEnum.DEXTERITY;
        newAbilityOrder[abilityOrderObj.get("con").getAsInt() - 1] = AbilitySkillConstants.AbilityEnum.CONSTITUTION;
        newAbilityOrder[abilityOrderObj.get("int").getAsInt() - 1] = AbilitySkillConstants.AbilityEnum.INTELLIGENCE;
        newAbilityOrder[abilityOrderObj.get("wis").getAsInt() - 1] = AbilitySkillConstants.AbilityEnum.WISDOM;
        newAbilityOrder[abilityOrderObj.get("cha").getAsInt() - 1] = AbilitySkillConstants.AbilityEnum.CHARISMA;
        return newAbilityOrder;
    }


    private static Set<AbilitySkillConstants.AbilityEnum> createSavingThrows(JsonArray savingThrowsArray) {
        final Set<AbilitySkillConstants.AbilityEnum> savingThrows = new HashSet<>();
        for (JsonElement savingThrow : savingThrowsArray) {
            savingThrows.add(AbilitySkillConstants.AbilityEnum.valueOf(savingThrow.getAsString().toUpperCase()));
        }
        return savingThrows;
    }


    private static Set<AbilitySkillConstants.SkillEnum> createSkillProficiencies(JsonArray skillProficienciesArray) {
        final Set<AbilitySkillConstants.SkillEnum> skillProficiencies = new HashSet<>();
        for (JsonElement skillProficiency : skillProficienciesArray) {
            skillProficiencies
                    .add(AbilitySkillConstants.SkillEnum.valueOf(skillProficiency.getAsString().toUpperCase()));
        }
        return skillProficiencies;
    }


    private static WeaponProficiencies createWeaponProficiencies(JsonArray weaponProficienciesArray) {
        final WeaponProficiencies weaponProficiencies = new WeaponProficiencies();
        for (JsonElement weaponProficiency : weaponProficienciesArray) {
            try {
                weaponProficiencies.add(Weapon.WeaponsEnum.valueOf(weaponProficiency.getAsString().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Ignore - proficiency may be in proficiencyEnum or may not be in the bot yet
            }
            try {
                weaponProficiencies
                        .add(Weapon.WeaponProficiencyEnum.valueOf(weaponProficiency.getAsString().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Ignore - proficiency may not be in the bot yet
            }
        }
        return weaponProficiencies;
    }


    private Class_(int hitDie, AbilitySkillConstants.AbilityEnum[] abilityOrder,
                   Set<AbilitySkillConstants.AbilityEnum> savingThrows, int skillQuantity,
                   Set<AbilitySkillConstants.SkillEnum> skillProficiencies, FundsSetUp fundsSetUp,
                   WeaponProficiencies weaponProficiencies, Weapon.WeaponsEnum startWeapon)
    {
        this.hitDie = hitDie;
        this.abilityOrder = abilityOrder;
        this.savingThrows = savingThrows;
        this.skillQuantity = skillQuantity;
        this.skillProficiencies = skillProficiencies;
        this.fundsSetUp = fundsSetUp;
        this.weaponProficiencies = weaponProficiencies;
        this.startWeapon = startWeapon;
    }


    public static Class_ getClassInfo(ClassEnum classEnum) {
        if (classes == null) {
            getClassesFromFile();
        }
        return classes.get(classEnum);
    }


    private static void getClassesFromFile() {
        try {
            final GsonBuilder gsonBuilder = new GsonBuilder();
            final Gson gson = gsonBuilder.registerTypeAdapter(Class_.class, new ClassSetUpDeserializer()).create();

            final String classSetUpJSON = new String(readAllBytes(Paths.get(fileLocation)));
            gson.fromJson(classSetUpJSON, Class_.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String getClassesList() {
        if (classes == null) {
            getClassesFromFile();
        }

        String classes = "Available classes: ";
        final ClassEnum[] classEnums = ClassEnum.values();

        for (int i = 0; i < classEnums.length; i++) {
            classes += classEnums[i].toString();

            if (i < classEnums.length - 1) {
                classes += ", ";
            }
        }

        return classes;
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


    public int getSkillQuantity() {
        return skillQuantity;
    }


    public Set<AbilitySkillConstants.SkillEnum> getSkillProficiencies() {
        return skillProficiencies;
    }


    public int rollFunds() {
        return fundsSetUp.rollFunds();
    }


    public WeaponProficiencies getWeaponProficiencies() {
        return weaponProficiencies;
    }


    public Weapon.WeaponsEnum getStartWeapon() {
        return startWeapon;
    }


    private static class ClassSetUpDeserializer implements JsonDeserializer<Class_> {
        public Class_ deserialize(JsonElement json, Type typeOfT,
                                  JsonDeserializationContext context) throws JsonParseException
        {
            return new Class_(json.getAsJsonObject().get("classes").getAsJsonArray());
        }
    }
}
