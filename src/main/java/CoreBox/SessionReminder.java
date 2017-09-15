package CoreBox;


import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Date;
import java.util.Map;



public class SessionReminder extends SessionTimes implements Runnable {
    private Guild guild;
    private boolean isWithinAnHour = false;


    public SessionReminder(Guild guild) {
        if (guild.getId().equals(IDs.teenTitansGoID)) {
            this.guild = guild;
        }
        else {
            throw new IllegalArgumentException("Wrong guild ID to start session times thread");
        }
    }


    @Override
    public void run() {
        while (true) {
            final Map<String, SessionTimes> gameTimes = SessionTimes.getGameTimes();
            for (SessionTimes sessionTimes : gameTimes.values()) {
                if (sessionTimes.getGameTime() != null && !sessionTimes.isReminderHappened()
                        && sessionTimes.getGameTime().before(new Date()))
                {
                    // TODO: trigger this only 2 hours before session
                    sessionTimes.setReminderHappened(true);
                    final Role role = guild.getRolesByName(sessionTimes.getRoleName(), true).get(0);
                    final TextChannel textChannel = guild.getTextChannelsByName("general", true).get(0);
                    textChannel.sendMessage(getSessionReminder(role)).queue();
                }
            }

            // Smaller increments when isWithinAnHour
            try {
                Thread.sleep(1000 * 60 * 10);
            } catch (InterruptedException e) {
            }
        }
    }
}
