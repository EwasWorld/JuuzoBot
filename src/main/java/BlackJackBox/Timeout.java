package BlackJackBox;

import net.dv8tion.jda.core.entities.TextChannel;



/*
 * If a player takes too long it automatically makes them stand
 */
public class Timeout implements Runnable {
    private static final int timeoutTime = 90 * 1000;
    private int turn;
    private GameInstance gi;
    private TextChannel gameChannel;


    Timeout(int turn, GameInstance gi, TextChannel gameChannel) {
        this.turn = turn;
        this.gi = gi;
        this.gameChannel = gameChannel;
    }


    @Override
    public void run() {
        try { Thread.sleep(timeoutTime);          } catch (InterruptedException e) { }
        if (gi.getTurnAsInt() == turn) {
            gameChannel.sendMessage(gi.timeoutStand()).queue();
        }
    }
}
