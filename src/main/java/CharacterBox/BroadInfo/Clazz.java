package CharacterBox.BroadInfo;

import CharacterBox.AttackBox.Weapon;
import CharacterBox.AttackBox.WeaponProficiencies;
import CharacterBox.CharacterConstants;
import CharacterBox.DiscordPrintable;
import CoreBox.Bot;
import com.google.gson.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.Files.readAllBytes;



public class Clazz {
    public enum ClassEnum implements DiscordPrintable {
        BARBARIAN, BARD, CLERIC, DRUID, FINESSEFIGHTER,
        FIGHTER, MONK, PALADIN, RANGER, ROGUE,
        SORCERER, WARLOCK, WIZARD;


        @Override
        public String toPrintableString() {
            return toString().toLowerCase();
        }
    }



    private static final String fileLocation = Bot.getResourceFilePath() + "CharacterGeneration/Classes.json";
    private static Map<ClassEnum, Clazz> classes;
    private int hitDie;
    // [0] will be the highest stat
    private CharacterConstants.AbilityEnum[] abilityOrder;
    private Set<CharacterConstants.AbilityEnum> savingThrows;
    // The number of skill proficiencies to be chosen from skillProficiencies
    private int skillQuantity;
    // Possible skill proficiencies
    private Set<CharacterConstants.SkillEnum> skillProficiencies;
    private FundsSetUp fundsSetUp;
    private WeaponProficiencies weaponProficiencies;
    private Weapon.WeaponsEnum startWeapon;


    private Clazz(Object rawObj) {
        classes = new HashMap<>();

        for (JsonElement element : (JsonArray) rawObj) {
            final JsonObject object = (JsonObject) element;

            final ClassEnum classEnum = ClassEnum.valueOf(object.get("name").getAsString().toUpperCase());
            final JsonObject funds = object.getAsJsonObject("funds");
            classes.put(classEnum, new Clazz(
                    object.get("hitDie").getAsInt(),
                    createAbilityOrder(object.getAsJsonObject("abilityOrder")),
                    createSavingThrows(object.getAsJsonArray("savingThrows")),
                    object.get("skillProficienciesQuantity").getAsInt(),
                    CharacterConstants.createSkillProficiencies(object.getAsJsonArray("skillProficiencies")),
                    new FundsSetUp(
                            funds.get("quantity").getAsInt(),
                            funds.get("multiply").getAsBoolean()
                    ),
                    createWeaponProficiencies(object.getAsJsonArray("weaponProficiencies")),
                    Weapon.WeaponsEnum.valueOf(object.get("startWeapon").getAsString().toUpperCase())
            ));
        }
    }


    private static CharacterConstants.AbilityEnum[] createAbilityOrder(JsonObject abilityOrderObj) {
        final CharacterConstants.AbilityEnum[] newAbilityOrder = new CharacterConstants.AbilityEnum[6];
        newAbilityOrder[abilityOrderObj.get("str").getAsInt() - 1] = CharacterConstants.AbilityEnum.STRENGTH;
        newAbilityOrder[abilityOrderObj.get("dex").getAsInt() - 1] = CharacterConstants.AbilityEnum.DEXTERITY;
        newAbilityOrder[abilityOrderObj.get("con").getAsInt() - 1] = CharacterConstants.AbilityEnum.CONSTITUTION;
        newAbilityOrder[abilityOrderObj.get("int").getAsInt() - 1] = CharacterConstants.AbilityEnum.INTELLIGENCE;
        newAbilityOrder[abilityOrderObj.get("wis").getAsInt() - 1] = CharacterConstants.AbilityEnum.WISDOM;
        newAbilityOrder[abilityOrderObj.get("cha").getAsInt() - 1] = CharacterConstants.AbilityEnum.CHARISMA;
        return newAbilityOrder;
    }


    private static Set<CharacterConstants.AbilityEnum> createSavingThrows(JsonArray savingThrowsArray) {
        final Set<CharacterConstants.AbilityEnum> savingThrows = new HashSet<>();
        for (JsonElement savingThrow : savingThrowsArray) {
            savingThrows.add(CharacterConstants.AbilityEnum.valueOf(savingThrow.getAsString().toUpperCase()));
        }
        return savingThrows;
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


    private Clazz(int hitDie, CharacterConstants.AbilityEnum[] abilityOrder,
                  Set<CharacterConstants.AbilityEnum> savingThrows, int skillQuantity,
                  Set<CharacterConstants.SkillEnum> skillProficiencies, FundsSetUp fundsSetUp,
                  WeaponProficiencies weaponProficiencies, Weapon.WeaponsEnum startWeapon) {
        this.hitDie = hitDie;
        this.abilityOrder = abilityOrder;
        this.savingThrows = savingThrows;
        this.skillQuantity = skillQuantity;
        this.skillProficiencies = skillProficiencies;
        this.fundsSetUp = fundsSetUp;
        this.weaponProficiencies = weaponProficiencies;
        this.startWeapon = startWeapon;
    }


    public static Clazz getClassInfo(ClassEnum classEnum) {
        if (classes == null) {
            getClassesFromFile();
        }
        return classes.get(classEnum);
    }


    private static void getClassesFromFile() {
        try {
            final GsonBuilder gsonBuilder = new GsonBuilder();
            final Gson gson = gsonBuilder.registerTypeAdapter(Clazz.class, new ClassSetUpDeserializer()).create();

            final String classSetUpJSON = new String(readAllBytes(Paths.get(fileLocation)));
            gson.fromJson(classSetUpJSON, Clazz.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public int getStartHP(int constitutionModifier) {
        return hitDie + constitutionModifier;
    }


    public CharacterConstants.AbilityEnum[] getAbilityOrder() {
        return abilityOrder;
    }


    public Set<CharacterConstants.AbilityEnum> getSavingThrows() {
        return savingThrows;
    }


    /**
     * @return the required number of class proficiencies chosen at random that are not already being used
     */
    public Set<CharacterConstants.SkillEnum> getRandomSkillProficiencies(
            Set<CharacterConstants.SkillEnum> currentProficiencies) {
        final Set<CharacterConstants.SkillEnum> possibleProficienciesClone = new HashSet<>(skillProficiencies);
        final Set<CharacterConstants.SkillEnum> newProficiencies = new HashSet<>();

        for (int i = 0; i < skillQuantity; i++) {
            CharacterConstants.SkillEnum chosenSkill;
            do {
                int size = possibleProficienciesClone.size();
                // select a random proficiency
                chosenSkill = possibleProficienciesClone.toArray(new CharacterConstants.SkillEnum[size])[new Random()
                        .nextInt(size)];
                possibleProficienciesClone.remove(chosenSkill);
            } while (currentProficiencies.contains(chosenSkill) && possibleProficienciesClone.size() > 0);
            newProficiencies.add(chosenSkill);
        }

        return newProficiencies;
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


    private static class ClassSetUpDeserializer implements JsonDeserializer<Clazz> {
        public Clazz deserialize(JsonElement json, Type typeOfT,
                                 JsonDeserializationContext context) throws JsonParseException {
            return new Clazz(json.getAsJsonObject().get("classes").getAsJsonArray());
        }
    }
}
