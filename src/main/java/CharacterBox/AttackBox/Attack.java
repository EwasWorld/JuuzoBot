package main.java.CharacterBox.AttackBox;

import main.java.CharacterBox.Character;
import main.java.CharacterBox.UsersCharacters;
import main.java.Foo.Roll;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Optional;



public class Attack {
    private static final int defenderAC = 13;


    public static void attack(User author, String victim, MessageChannel channel) {
        String attacker = author.getName();
        victim = victim.trim().replace("@", "");

        Optional<Character> character = UsersCharacters.getCharacter(author.getIdLong());
        if (character.isPresent()) {
            Weapon weapon = character.get().getWeaponInfo();
            String message = weapon.getAttackLine();

            Roll.RollResult attackRoll = character.get().attackRoll();
            if (attackRoll.getResult() >= defenderAC && !attackRoll.isCritFail()) {
                message += " " + weapon.getHitLine();

                int damage;
                if (attackRoll.isNaddy20()) {
                    damage = weapon.rollCriticalDamage();
                }
                else {
                    damage = weapon.rollDamage();
                }
                message += String.format(" VIC took %d damage", damage);
            }
            else {
                message += " " + weapon.getMissLine();
            }

            message = message.replaceAll("PC", character.get().getName());
            message = message.replaceAll("VIC", victim);
            sendMessage(channel, message);
        }
        else {
            sendMessage(channel, attacker
                    + ", I see you're eager to get to the violence but you'll need to make a character first using "
                    + "!newChar");
        }
    }


    private static void sendMessage(MessageChannel channel, String message) {
        channel.sendMessage(message).queue();
    }
}
