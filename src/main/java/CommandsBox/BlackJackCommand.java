package CommandsBox;

import BlackJackBox.GameInstance;
import CoreBox.AbstractCommand;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;



/**
 * refactored: 1/10/18
 */
public class BlackJackCommand extends AbstractCommand {
    private static boolean gameRunning = false;
    private static GameInstance gameInstance = null;
    private static Message currentGamesChannelMessage;


    public static void setGameInstanceToNull() {
        currentGamesChannelMessage.editMessage(gameInstance.toString()).queue();
        gameInstance = null;
    }


    /**
     * Executes the command associated with the emoji. Does nothing if there is no command associated with the emoji
     * @param emoji  an emoji in the form of a one/two char array
     * @param player the one who invoked the command
     */
    public static void executeFromRemoveReaction(String emoji, Member player) {
        Optional<BlackJackEmojiArgument> argumentOptional = emojiToPlayArgument(emoji);
        if (argumentOptional.isPresent()) {
            BlackJackEmojiArgument argument = argumentOptional.get();
            if (argument == BlackJackEmojiArgument.JOIN) {
                gameInstance.leave(player);
            }
        }
        currentGamesChannelMessage.editMessage(gameInstance.toString()).queue();
    }


    /**
     * Executes the command associated with the emoji. Does nothing if there is no command associated with the emoji
     * @param emoji  an emoji in the form of a one/two char array
     * @param player the one who invoked the command
     */
    public static void executeFromAddReaction(String emoji, Member player) {
        Optional<BlackJackEmojiArgument> argument = emojiToPlayArgument(emoji);
        argument.ifPresent(blackJackPlayArgument -> argumentSwitcher(blackJackPlayArgument, player));
    }


    /**
     * Carries out the appropriate action for the command given
     * @param player the one who invoked the command
     */
    private static void argumentSwitcher(BlackJackEmojiArgument argument, Member player) {
        if (argument == BlackJackEmojiArgument.START && (gameRunning || gameInstance != null)) {
            gameInstance.startGame();
            gameRunning = true;
        }
        else {
            if (gameRunning) {
                switch (argument) {
                    case JOIN:
                        gameInstance.join(player);
                        currentGamesChannelMessage.editMessage(gameInstance.toString()).queue();
                        return;
                    case HIT:
                        gameInstance.hitMe(player);
                        break;
                    case STAND:
                        gameInstance.stand(player);
                        break;
                    case SPLIT: // banana
                        gameInstance.split(player);
                        break;
                    case LEAVE:
                        gameInstance.leave(player);
                        break;
                    case END:
                        if (gameInstance.isGameOwner(player) || AbstractCommand.getRank(player) == Rank.ADMIN) {
                            endGame();
                        }
                        else {
                            gameInstance.setMostRecentGameErrorInvalidPermissions();
                            currentGamesChannelMessage.editMessage(gameInstance.toString()).queue();
                        }
                        return;
                }
            }
            else {
                throw new BadStateException("You need to create a game first");
            }
        }
        if (gameInstance != null) {
            currentGamesChannelMessage.editMessage(gameInstance.toString()).queue();
            resetReactions();
        }
    }


    /**
     * @param emoji an emoji in the form of a one/two char array
     * @return the argument represented by the given emoji
     */
    public static Optional<BlackJackEmojiArgument> emojiToPlayArgument(String emoji) {
        Optional<Emoji> emojiOptional = Emoji.getFromString(emoji);
        if (emojiOptional.isPresent()) {
            return BlackJackEmojiArgument.getFromEmoji(emojiOptional.get());
        }
        return Optional.empty();
    }


    public static void setCurrentGamesChannelMessage(Message message) {
        currentGamesChannelMessage = message;
        resetReactions();
    }


    /**
     * Clears all reactions from the currentGamesChannelMessage and sets them again as needed
     */
    private static void resetReactions() {
        if (currentGamesChannelMessage != null) {
            currentGamesChannelMessage.clearReactions().complete();
            if (gameInstance != null) {
                final BlackJackEmojiArgument[] arguments;
                if (gameInstance.getGameState() == GameInstance.GameState.STARTED) {
                    arguments = BlackJackEmojiArgument.getGameCommands();
                }
                else if (gameInstance.getGameState() == GameInstance.GameState.LOBBY) {
                    arguments = BlackJackEmojiArgument.getLobbyCommands();
                }
                else {
                    return;
                }
                for (BlackJackEmojiArgument argument : arguments) {
                    argument.getEmoji().addAsReaction(currentGamesChannelMessage);
                }
            }
        }
    }


    public static boolean isGameMessage(String messageID) {
        if (currentGamesChannelMessage == null) {
            return false;
        }
        return messageID.equals(currentGamesChannelMessage.getId());
    }


    /**
     * @return a printable string in the form ":emojiAlias: command" of all currently available commands
     */
    public static String getEmojiHelpReactionString(GameInstance.GameState gameState) {
        final boolean isStarted;
        if (gameState == GameInstance.GameState.STARTED) {
            isStarted = true;
        }
        else if (gameState == GameInstance.GameState.LOBBY) {
            isStarted = false;
        }
        else {
            return "";
        }
        return "**Click the reactions to play!** " + BlackJackEmojiArgument.getHelpReactionString(isStarted);
    }


    @Override
    public String getCommand() {
        return "blackjack";
    }


    @Override
    public String getDescription() {
        return "create a game, have whoever you want join, then start it";
    }


    @Override
    public String getArguments() {
        return "{new/end/reprint}";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        final BlackJackArgument argument = getArgument(args);
        switch (argument) {
            case NEW:
                if (!gameRunning) {
                    gameRunning = true;
                    gameInstance = new GameInstance(event.getMember());
                }
                else {
                    throw new BadStateException("Game already created, try joining it");
                }
                break;
            case END:
                endGame();
                return;
            case REPRINT:
                voidGameMessage("GAME REPRINTED");
                break;
        }
        sendMessage(event.getTextChannel(), gameInstance.toString());
    }


    /**
     * @param args an argument in string form
     * @return the argument represented by args
     * @throws BadUserInputException for an invalid argument
     */
    private BlackJackArgument getArgument(String args) {
        try {
            return BlackJackArgument.valueOf(args.toUpperCase().replaceAll(" ", ""));
        } catch (IllegalArgumentException e) {
            throw new BadUserInputException("I don't understand that argument. Use one of " + getArguments());
        }
    }


    private static void endGame() {
        setGameRunningFalse();
        voidGameMessage("FORCEFULLY ENDED");
    }


    /**
     * Cross out the game message and remove reactions from it so that it can no longer be played
     * @param message explanation to be prepended onto the game string
     */
    private static void voidGameMessage(String message) {
        currentGamesChannelMessage.editMessage(message + ": ~~" + currentGamesChannelMessage.getRawContent() + "~~")
                .queue();
        currentGamesChannelMessage.clearReactions().complete();
    }


    public static void setGameRunningFalse() {
        gameRunning = false;
        currentGamesChannelMessage.clearReactions().complete();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }


    /**
     * Arguments that are triggered from the blackjack command args
     */
    private enum BlackJackArgument {NEW, END, ENDGAME, REPRINT}



    /**
     * Arguments that are triggered from emojis
     */
    private enum BlackJackEmojiArgument {
        JOIN("join game", Emoji.POOP), START("start game", Emoji.OKHAND), HIT("hit", Emoji.PUNCH),
        STAND("stand", Emoji.RAISEDHAND), SPLIT("split", Emoji.METAL), LEAVE("leave game", Emoji.WAVE),
        END("end game", Emoji.X);
        private static Map<Emoji, BlackJackEmojiArgument> emojiMappings = setEmojiMappings();
        private String description;
        private Emoji emoji;


        BlackJackEmojiArgument(String description, Emoji emoji) {
            this.description = description;
            this.emoji = emoji;
        }


        public Emoji getEmoji() {
            return emoji;
        }


        private static Map<Emoji, BlackJackEmojiArgument> setEmojiMappings() {
            Map<Emoji, BlackJackEmojiArgument> map = new HashMap<>();
            for (BlackJackEmojiArgument argument : BlackJackEmojiArgument.values()) {
                map.put(argument.emoji, argument);
            }
            return map;
        }


        static Optional<BlackJackEmojiArgument> getFromEmoji(Emoji emoji) {
            if (emojiMappings.containsKey(emoji)) {
                return Optional.of(emojiMappings.get(emoji));
            }
            else {
                return Optional.empty();
            }
        }


        static String getHelpReactionString(boolean isInLobby) {
            if (!isInLobby) {
                return getReactionString(getLobbyCommands());
            }
            else {
                return getReactionString(getGameCommands());
            }
        }


        private static String getReactionString(BlackJackEmojiArgument[] commands) {
            StringBuilder sb = new StringBuilder();
            for (BlackJackEmojiArgument argument : commands) {
                sb.append(argument.emoji.getDiscordAlias());
                sb.append(" ");
                sb.append(argument.description);
                sb.append(", ");
            }
            sb.delete(sb.length() - 2, sb.length());
            return sb.toString();
        }


        static BlackJackEmojiArgument[] getLobbyCommands() {
            return new BlackJackEmojiArgument[]{JOIN, START};
        }


        static BlackJackEmojiArgument[] getGameCommands() {
            return new BlackJackEmojiArgument[]{HIT, STAND, SPLIT, LEAVE, END};
        }
    }
}
