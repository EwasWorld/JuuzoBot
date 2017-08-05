package main.java.Foo;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Random;

public class DiceRoller {
    public static void roll(MessageReceivedEvent event) {
        String message = event.getMessage().getContent();

        try {
            if (message.startsWith("!roll d")) {
                message = message.substring(7);
                int roll = Integer.parseInt(message);
                message = event.getAuthor().getName() + " rolled a ";

                if (roll > 2) {
                    message += new Random().nextInt(roll) + 1;
                }
                else if (roll == 2) {
                    if (Math.random() > 0.5) {
                        message += "heads";
                    }
                    else {
                        message += "tails";
                    }
                }
                else if (roll == 1) {
                    // TODO
                }
                else {
                    throw new NumberFormatException("Invalid input");
                }

                event.getChannel().sendMessage(message).queue();
            }
            else {
                throw new NumberFormatException("Invalid input");
            }
        }
        catch (NumberFormatException e) {
            event.getChannel().sendMessage("Invalid input. Use '!roll d*'").queue();
        }

    }

    public static int roll(int dieSize) throws NumberFormatException {
        if (dieSize >= 2) {
            return new Random().nextInt(dieSize) + 1;
        }
        else {
            throw new NumberFormatException("Invalid input");
        }
    }
}
