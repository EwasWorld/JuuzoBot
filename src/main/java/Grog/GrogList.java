package main.java.Grog;

import com.google.gson.Gson;
import main.java.Foo.DiceRoller;
import main.java.Foo.Main;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;



public class GrogList {
    private static final String fileLocation = "src/main/java/Grog/GrogEffects.json";
    private static String[] effects;


    private GrogList(String[] effects) {
        GrogList.effects = effects;
    }


    private static String[] getEffects() throws FileNotFoundException {
        if (effects == null) {
            try {
                final String grogJson = new String(Files.readAllBytes(Paths.get(GrogList.fileLocation)));
                new Gson().fromJson(grogJson, GrogList.class);
            } catch (IOException e) {
                throw new FileNotFoundException("Grog file not found");
            }
        }
        return effects;
    }


    public static void drinkGrog(MessageReceivedEvent event) {
        try {
            String author = event.getAuthor().getName();
            int roll = DiceRoller.roll(1000) - 1;
            String effect = GrogList.getEffects()[roll];
            effect = effect.replaceAll("PC", author);
            effect = author + " drinks an Essence of Balthazar potion. " + effect;

            event.getChannel().sendMessage(effect).queue();
        } catch (NullPointerException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
