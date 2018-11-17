package CharacterBox.BroadInfo;

import CharacterBox.Alignment;
import CharacterBox.CharacterConstants;
import CharacterBox.UserBackground;
import CoreBox.Bot;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import com.google.gson.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static java.nio.file.Files.readAllBytes;



public class Background {
    public enum BackgroundEnum {
        ACOLYTE, CHARLATAN, CRIMINAL, ENTERTAINER, FOLKHERO, GUILDARTISAN,
        HERMIT, NOBLE, OUTLANDER, SAGE, SAILOR, SOLDIER, URCHIN;


        public UserBackground generateRandomBackground(Alignment alignment) {
            final Background bg = Background.getBackgroundInfo(this);
            return new UserBackground(this, getRandomInt(bg.possibilities), getRandomInt(bg.traits), bg.getRandomIdealInt(alignment), getRandomInt(bg.bonds), getRandomInt(bg.flaws));
        }


        private int getRandomInt(Object[] objects) {
            return new Random().nextInt(objects.length);
        }
    }



    // TODO Implement Instruments feature
    public enum InstrumentEnum {
        BAGPIPES("Bagpipes"), DRUM("Drum"), DULCIMER("Dulcimer"), FLUTE("Flute"),
        LUTE("Lute"), LYRE("Lyre"), HORN("Horn"), PANFLUTE("Pan Flute"),
        SHAWM("Shawm"), VIOL("Viol"), Castanets("Castanets"), Didgeridoo("Digeridoo"),
        Euphonium("Euphonium"), HurdyGurdy("HurdyGurdy"), Violin("Violin");

        private String name;


        InstrumentEnum(String name) {
            this.name = name;
        }


        public String getName() {
            return name;
        }
    }



    private static final String fileLocation = Bot.getResourceFilePath() + "CharacterGeneration/Backgrounds.json";
    private static Map<BackgroundEnum, Background> backgrounds;
    private String[] possibilities;
    private Set<CharacterConstants.SkillEnum> proficiencies;
    private Set<CharacterConstants.Language> languages;
    private String[] traits;
    private Ideal[] ideals;
    private String[] bonds;
    private String[] flaws;

    private static File file = new File(fileLocation);


    public Background(Object rawObj) {
        backgrounds = new HashMap<>();

        for (JsonElement element : (JsonArray) rawObj) {
            final JsonObject object = (JsonObject) element;

            backgrounds.put(
                    BackgroundEnum.valueOf(object.get("name").getAsString().toUpperCase()),
                    new Background(
                            CharacterConstants.getStringArrayFromJsonArray(object.getAsJsonArray("possibilities")),
                            CharacterConstants.createSkillProficiencies(object.getAsJsonArray("proficiencies")),
                            CharacterConstants.createLanguages(object.getAsJsonArray("languages")),
                            CharacterConstants.getStringArrayFromJsonArray(object.getAsJsonArray("traits")),
                            createIdeals(object.getAsJsonArray("ideals")),
                            CharacterConstants.getStringArrayFromJsonArray(object.getAsJsonArray("bonds")),
                            CharacterConstants.getStringArrayFromJsonArray(object.getAsJsonArray("flaws"))
                    )
            );
        }
    }


    public String getBackgroundDescription(UserBackground background) {
        Ideal ideal = ideals[background.getIdeal()];
        String flaw = flaws[background.getFlaw()];
        if (flaw.charAt(0) != "I".charAt(0)) {
            flaw = String.valueOf(flaw.charAt(0)).toLowerCase() + flaw.substring(1);
        }

        String string = "";
        string += String.format("%s %s", possibilities[background.getPossibility()], traits[background.getTrait()]);
        string += String.format(" I'm driven by %s. %s", ideal.name.toLowerCase(), ideal.description);
        string += String.format(" %s However, %s", bonds[background.getBond()], flaw);

        return string;
    }


    private Ideal[] createIdeals(JsonArray idealsJsonArray) {
        List<Ideal> ideals = new ArrayList<>();
        for (JsonElement element : idealsJsonArray) {
            final JsonObject object = (JsonObject) element;
            ideals.add(new Ideal(
                    object.get("name").getAsString(),
                    object.get("description").getAsString(),
                    object.get("alignment").getAsString()
            ));
        }
        return ideals.toArray(new Ideal[ideals.size()]);
    }


    private Background(String[] possibilities, Set<CharacterConstants.SkillEnum> proficiencies,
                       Set<CharacterConstants.Language> languages, String[] traits,
                       Ideal[] ideals, String[] bonds, String[] flaws)
    {
        this.possibilities = possibilities;
        this.proficiencies = proficiencies;
        this.languages = languages;
        this.traits = traits;
        this.ideals = ideals;
        this.bonds = bonds;
        this.flaws = flaws;
    }


    public static Background getBackgroundInfo(BackgroundEnum background) {
        if (backgrounds == null) {
            getBackgroundsFromFile();
        }
        if (backgrounds.containsKey(background)) {
            return backgrounds.get(background);
        }
        else {
            throw new BadUserInputException("Invalid background");
        }
    }


    private static void getBackgroundsFromFile() {
        try {
            final GsonBuilder gsonBuilder = new GsonBuilder();
            final Gson gson = gsonBuilder.registerTypeAdapter(Background.class, new BackgroundsSetUpDeserializer())
                    .create();

            blah();
            final String backgroundsSetUpJSON = new String(readAllBytes(file.toPath()));
            gson.fromJson(backgroundsSetUpJSON, Background.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void blah() {
        try {
            if (!file.exists()) {
                file.createNewFile();
                InputStream inputStream = Background.class.getResourceAsStream(fileLocation);
                Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * Returns whether the background given exists
     */
    public static boolean contains(String background) {
        if (backgrounds == null) {
            getBackgroundsFromFile();
        }
        return backgrounds.containsKey(background.toUpperCase());
    }


    /*
     * Returns a random ideal ensuring the the alignment given allows for the ideal
     */
    public int getRandomIdealInt(Alignment alignment) {
        final List<Integer> validIdeals = new ArrayList<>();
        for (int i = 0; i < ideals.length; i++) {
            if (ideals[i].alignment.checkMatches(alignment)) {
                validIdeals.add(i);
            }
        }
        return validIdeals.get(new Random().nextInt(validIdeals.size()));
    }


    public Set<CharacterConstants.SkillEnum> getProficiencies() {
        return proficiencies;
    }


    public Set<CharacterConstants.Language> getLanguages() {
        return languages;
    }


    private static class BackgroundsSetUpDeserializer implements JsonDeserializer<Background> {
        public Background deserialize(JsonElement json, Type typeOfT,
                                      JsonDeserializationContext context) throws JsonParseException
        {
            return new Background(json.getAsJsonObject().get("backgrounds").getAsJsonArray());
        }
    }



    public class Ideal {
        private String name;
        private String description;
        private Alignment alignment;


        private Ideal(String name, String description, String alignment) {
            this.name = name;
            this.description = description;

            alignment = alignment.toUpperCase();
            if (alignment.equals("ANY")) {
                this.alignment = new Alignment();
            }
            else {
                try {
                    this.alignment = new Alignment(Alignment.GoodEvilEnum.valueOf(alignment));
                } catch (IllegalArgumentException e) {
                    try {
                        this.alignment = new Alignment(Alignment.LawChaosEnum.valueOf(alignment));
                    } catch (IllegalArgumentException e1) {
                        throw new BadStateException("Incorrect alignment for ideal");
                    }
                }
            }
        }


        public String getName() {
            return name;
        }


        public String getDescription() {
            return description;
        }
    }
}
