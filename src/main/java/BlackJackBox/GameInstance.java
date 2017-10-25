package BlackJackBox;

import CommandsBox.BlackJackCommand;
import ExceptionsBox.BadStateException;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;



/*
 * Not at all thread safe
 * TODO Implement Bets
 * TODO Implement Insurance
 */
public class GameInstance {
    private Table table;
    private List<Member> players;
    private int turn;
    private boolean gameStarted;
    private TextChannel gameChannel;
    private boolean hasPlayerTimedOut = false;


    /*
     * Sets up a new game with an initial player and allows others to join it
     */
    public GameInstance(TextChannel gameChannel, Member player) {
        this.gameChannel = gameChannel;
        players = new ArrayList<>();
        players.add(player);
        gameStarted = false;
    }


    /*
     * Adds a player to the game (game must not have started)
     */
    public void join(Member player) {
        if (gameStarted) {
            throw new BadStateException("This game is already running");
        }
        if (isPlayerInGame(player)) {
            throw new BadStateException("You're already in the game!!!");
        }

        players.add(player);
    }


    /*
     * Returns whether a player has already joined the game
     */
    private boolean isPlayerInGame(Member player) {
        for (Member member : players) {
            if (member.getUser().getIdLong() == player.getUser().getIdLong()) {
                return true;
            }
        }
        return false;
    }


    /*
     * Creates the board, sets the first player's turn, and prevents more players from joining
     * Returns initial hands and who the first player is as a string
     */
    public String startGame() {
        if (gameStarted) {
            throw new BadStateException("Game has already started");
        }

        gameStarted = true;
        turn = 0;
        table = new Table(players, 1);
        new Thread(new Timeout(turn)).start();

        return buildStartGameString();
    }


    /*
     * Returns a string of the hand of the dealer and each player and who the first player is
     */
    private String buildStartGameString() {
        StringBuilder stringBuilder = new StringBuilder("Game started\n\n");
        stringBuilder.append(String.format("Dealer: %s\n", table.getDealersInitialHand()));
        for (Member member : players) {
            stringBuilder.append(String.format("%s: %s\n", getName(member), getHand(member)));
        }
        stringBuilder.append("\n");
        stringBuilder.append(getTurnAsString());

        return stringBuilder.toString();
    }


    static String getName(Member member) {
        return member.getUser().getName();
    }


    /*
     * Returns the hand of the specified member
     */
    public String getHand(Member player) {
        if (!gameStarted) {
            throw new BadStateException("Game hasn't started yet");
        }

        for (int i = 0; i < players.size(); i++) {
            if (playersEqualIDs(players.get(i), player)) {
                return table.getHand(i).toString();
            }
        }
        throw new BadStateException("You don't seem to have a hand");
    }


    /*
     * Returns who's turn it is
     */
    public String getTurnAsString() {
        if (!gameStarted) {
            throw new BadStateException("Game hasn't started yet");
        }

        return String.format("It's %s's turn", getName(players.get(turn)));
    }


    /*
     * Check whether the IDs of two members are equal
     * TODO: Is this necessary? - run tests
     */
    private boolean playersEqualIDs(Member one, Member two) {
        boolean test = one == two;
        boolean test2 = one.getUser() == two.getUser();
        return one.getUser().getIdLong() == two.getUser().getIdLong();
    }


    /*
     * Returns the dealer's hand as one face up one face down card
     */
    public String getDealerHand() {
        if (!gameStarted) {
            throw new BadStateException("Game hasn't started yet");
        }

        return "Dealer: " + table.getDealersInitialHand();
    }


    /*
     * The current player draws another card (calls stand() if they bust)
     * Returns the drawn card and the new hand
     */
    public String hitMe(Member player) {
        if (!gameStarted) {
            throw new BadStateException("Game hasn't started yet");
        }
        if (!isPlayersTurn(player)) {
            throw new BadStateException("It's not your turn");
        }

        final Card drawnCard = table.hitMe(turn);
        final Hand hand = table.getHand(turn);

        if (hand.isBust()) {
            final String bustStr = String.format(
                    "%s - **BUST** (%s)\n\n", drawnCard.toString(), hand.toString());
            return bustStr + stand();
        }

        return String.format("%s\nYour current hand: %s", drawnCard.toString(), hand.toString());
    }


    private boolean isPlayersTurn(Member player) {
        return playersEqualIDs(players.get(turn), player);
    }


    /*
     * Ends the current turn. If all players have had a turn it ends the game
     */
    private String stand() {
        turn++;
        if (turn < players.size()) {
            new Thread(new Timeout(turn));
            return String.format("%s, you're up next!", players.get(turn));
        }
        else {
            return end();
        }
    }


    /*
     * Runs the finish method (plays dealer and calculates which players won and lost)
     * Sets the game running and game started to false
     * Returns the dealer's final hand, which players won/lost/tied/bust, and instructions
     */
    private String end() {
        final String endStr = table.finishRound(players).toString()
                + "\n\nGame has ended and new players can join. Start again or make a new game";
        gameStarted = false;

        BlackJackCommand.setGameRunningFalse();
        if (hasPlayerTimedOut) {
            BlackJackCommand.setGameInstanceToNull();
        }

        return endStr;
    }


    /*
     * Checks that it's the given player's turn then ends the current turn
     */
    public String stand(Member player) {
        if (!gameStarted) {
            throw new BadStateException("Game hasn't started yet");
        }
        if (!isPlayersTurn(player)) {
            throw new BadStateException("It's not your turn");
        }

        return stand();
    }


    /*
     * Splits the hand in two
     * Returns the current hand (other one is played later in the turn order) and instructions
     */
    public String split(Member player) {
        if (!gameStarted) {
            throw new BadStateException("Game hasn't started yet");
        }
        if (!isPlayersTurn(player)) {
            throw new BadStateException("It's not your turn");
        }

        table.split(turn);
        return getHand(player) + "\nSplit complete, "
                + "continue your turn and you can play your other hand at the end.";
    }



    /*
     * If a player takes too long it automatically makes them stand
     * TODO Make a way to terminate these threads if a turn is completed without timing out
     */
    public class Timeout implements Runnable {
        private static final int waitTime = 90 * 1000;
        private static final String timeoutMessage = "Bzzt, you took too long\n\n";
        private int initialPlayerTurn;


        Timeout(int initialPlayerTurn) {
            this.initialPlayerTurn = initialPlayerTurn;
        }


        /*
         * Waits for a period of time. If it's still the same player's turn they are forced to stand
         * Also sets a flag which prevents the game from being started over again
         *      (removing them could be confusing if the others started over again)
         */
        @Override
        public void run() {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
            }

            if (turn == initialPlayerTurn) {
                hasPlayerTimedOut = true;
                gameChannel.sendMessage(timeoutMessage + stand()).queue();
            }
        }
    }
}
