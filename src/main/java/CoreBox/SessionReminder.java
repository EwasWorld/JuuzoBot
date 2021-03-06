package CoreBox;


import net.dv8tion.jda.core.entities.Guild;

import java.util.PriorityQueue;



// TODO Implement this
// Triggers a reminder 2 hours before the session
public class SessionReminder implements Runnable {
    private Guild guild;
    private PriorityQueue<SessionQueueElement> upcomingReminders = new PriorityQueue<>();


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
        // prevents this from being run
        throw new IllegalStateException("This doesn't work yet");
/*
        while (true) {
            final Map<String, SessionTimes> gameTimes = SessionTimes.getGameTimes();
            for (SessionTimes sessionTimes : gameTimes.values()) {
                if (sessionTimes.getGameTime() != null && !sessionTimes.isReminderHappened()
                        && sessionTimes.getGameTime().before(new Date()))
                {
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
*/
    }


    private void removeElement() {
        // TODO call from removeSessionTime in GameSession
    }


    private void addElement(String shortName) {
        upcomingReminders.add(new SessionQueueElement(shortName));
    }


    private void setAutoReminderSetToFalse() {
        // TODO
        removeElement();
    }


    class SessionQueueElement implements Comparable<SessionQueueElement> {
        private long sessionTime;
        private String shortName;


        public SessionQueueElement(String shortName) {
            this.shortName = shortName;
//            sessionTime = GameSession.getNextSession(shortName).getTime();
        }


        @Override
        public int compareTo(SessionQueueElement o) {
            return Long.compare(o.sessionTime, sessionTime);
        }


        public String getShortName() {
            return shortName;
        }
    }
}
