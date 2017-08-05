package main.java.Foo;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Attack {
    public static void attack (MessageReceivedEvent event) {
        String[] messageParts = event.getMessage().getContent().split(" ");
        String attacker = event.getAuthor().getName();
        String victim = messageParts[1];

    }
}
