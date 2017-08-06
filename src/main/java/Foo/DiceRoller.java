package main.java.Foo;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;



public class DiceRoller {
    private enum RollType {NORMAL, ADVANTAGE, DISADVANTAGE}


    public static void getStringForRoll(MessageReceivedEvent event) {
        final RollType rollType;
        String message = event.getMessage().getContent().substring(6).replace(" ", "");

        if (message.startsWith("adv")) {
            rollType = RollType.ADVANTAGE;
            message = message.substring(3);
        }
        else if (message.startsWith("dis")) {
            rollType = RollType.DISADVANTAGE;
            message = message.substring(3);
        }
        else {
            rollType = RollType.NORMAL;
        }


        int quantity;
        int dieSize;
        int modifier;

        String[] messageParts;

        try {
            if (message.contains("d")) {
                messageParts = message.split("d");
                if (messageParts[0].length() != 0) {
                    quantity = Integer.parseInt(messageParts[0]);
                }
                else {
                    quantity = 1;
                }
                message = messageParts[1];
            }
            else {
                sendInvalidFormatMessage(event.getChannel());
                return;
            }

            if (message.contains("+") || message.contains("-")) {
                if (message.contains("+")) {
                    message = message.replaceAll("\\+", "plus");
                    messageParts = message.split("plus");
                }
                else {
                    messageParts = message.split("-");
                }

                if (messageParts.length != 2) {
                    sendInvalidFormatMessage(event.getChannel());
                    return;
                }
                else {
                    dieSize = Integer.parseInt(messageParts[0]);
                    modifier = Integer.parseInt(messageParts[1]);

                    if (message.contains("-")) {
                        modifier -= modifier * 2;
                    }
                }
            }
            else {
                dieSize = Integer.parseInt(messageParts[1]);
                modifier = 0;
            }

            message = event.getAuthor().getName() + " " + getStringForRoll(rollType, quantity, dieSize, modifier);
            event.getChannel().sendMessage(message).queue();
        } catch (NumberFormatException e) {
            sendInvalidFormatMessage(event.getChannel());
        }
    }


    private static String getStringForRoll(RollType rollType, int quantity, int dieSize, int modifier) {
        if (dieSize <= 0) {
            throw new NumberFormatException("Invalid input");
        }
        else if (dieSize == 1) {
            // TODO roll a d1
            // quantity is number of bad things to happen or always just one thing?
            return "has incurred Eywa's wrath and is struck by a bolt of lightning";
        }
        else {
            return "rolled a " + roll(rollType, quantity, dieSize, modifier);
        }
    }


    public static int roll(RollType rollType, int quantity, int dieSize, int modifier) {
        if (dieSize > 1) {
            Random random = new Random();
            int[] totals = new int[2];

            for (int j = 0; j < quantity; j++) {
                totals[0] += random.nextInt(dieSize) + 1;
                totals[1] += random.nextInt(dieSize) + 1;
            }
            totals[0] += modifier;
            totals[1] += modifier;


            if (rollType == RollType.NORMAL) {
                return totals[0];
            }
            else {
                if (totals[0] < totals[1]) {
                    int temp = totals[0];
                    totals[0] = totals[1];
                    totals[1] = temp;
                }

                if (rollType == RollType.ADVANTAGE) {
                    return totals[0];
                }
                else {
                    return totals[1];
                }
            }
        }
        else {
            throw new NumberFormatException("Invalid dieSize");
        }
    }

    public static int roll(int quantity, int dieSize, int modifier) {
        return roll(RollType.NORMAL, quantity, dieSize, modifier);
    }


    public static int roll(int dieSize) {
        return roll(RollType.NORMAL, 1, dieSize, 0);
    }


    private static void sendInvalidFormatMessage(MessageChannel channel) {
        channel.sendMessage("Invalid input use '!roll [adv/dis] [quantity] d {size} [modifier]' e.g. '!roll adv 1d20+2'").queue();
    }
}
