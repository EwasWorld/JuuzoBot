package main.java.Grog;

import main.java.Foo.DiceRoller;
import main.java.Foo.Main;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class GrogList {
    public static final String fileLocation = "src/Grog/GrogEffects.json";
    private String[] effects;

    public GrogList(String[] effects) {
        this.effects = effects;
    }

    public String[] getEffects() {
        return effects;
    }

    public static void drinkGrog(MessageReceivedEvent event) {
        try {
            String author = event.getAuthor().getName();
            int roll = DiceRoller.roll(1000) - 1;
            String effect
                    = Main.getInformationWrapper().getGrogList().getEffects()[roll];
            effect = effect.replaceAll("PC", author);
            effect = author + " drinks an Essence of Balthazar potion. " + effect;

            event.getChannel().sendMessage(effect).queue();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
