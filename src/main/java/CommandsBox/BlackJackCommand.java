package CommandsBox;

import BlackJackBox.GameInstance;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import ExceptionsBox.ContactEwaException;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;



/**
 * refactored: 1/10/18
 */
public class BlackJackCommand extends AbstractCommand {
    private static GameInstance gameInstance = null;
    private static Message currentGamesChannelMessage;


    public static void setGameInstanceToNull() {
        currentGamesChannelMessage.editMessage(gameInstance.toString()).queue();
        currentGamesChannelMessage.clearReactions().queue();
        gameInstance = null;
    }


    /**
     * Executes the command associated with the emoji. Does nothing if there is no command associated with the emoji
     *
     * @param emoji an emoji in the form of a one/two char array
     * @param player the one who invoked the command
     * @return true if the reaction belonged to this method
     */
    public static boolean executeFromRemoveReaction(Long messageID, String emoji, Member player) {
        if (gameInstance == null || currentGamesChannelMessage == null || messageID != currentGamesChannelMessage
                .getIdLong()) {
            return false;
        }
        final Optional<BlackJackEmojiArgument> argumentOptional = emojiToPlayArgument(emoji);
        if (argumentOptional.isPresent()) {
            argumentOptional.get().removeAction(player);
            currentGamesChannelMessage.editMessage(gameInstance.toString()).queue();
        }
        return true;
    }


    /**
     * @param emoji an emoji in the form of a one/two char array
     * @return the argument represented by the given emoji
     */
    private static Optional<BlackJackEmojiArgument> emojiToPlayArgument(String emoji) {
        final Optional<Emoji> emojiOptional = Emoji.getFromString(emoji);
        if (emojiOptional.isPresent()) {
            return BlackJackEmojiArgument.getFromEmoji(emojiOptional.get());
        }
        return Optional.empty();
    }


    /**
     * Executes the command associated with the emoji. Does nothing if there is no command associated with the emoji
     *
     * @param messageReaction an emoji in the form of a one/two char array
     * @param player the one who invoked the command
     * @return true if the reaction belonged to this method
     */
    public static boolean executeFromAddReaction(Long messageID, MessageReaction messageReaction, Member player) {
        if (gameInstance == null || currentGamesChannelMessage == null || messageID != currentGamesChannelMessage
                .getIdLong()) {
            return false;
        }
        final Optional<BlackJackEmojiArgument> argument = emojiToPlayArgument(
                messageReaction.getReactionEmote().getName());
        argument.ifPresent(blackJackPlayArgument -> {
            argumentSwitcher(blackJackPlayArgument, player);
            if (blackJackPlayArgument.removeReaction) {
                messageReaction.removeReaction(player.getUser()).queue();
            }
        });
        return true;
    }


    /**
     * Carries out the appropriate action for the command given
     *
     * @param player the one who invoked the command
     */
    private static void argumentSwitcher(BlackJackEmojiArgument argument, Member player) {
        switch (argument) {
            case END:
                if (gameInstance == null) {
                    throw new BadStateException("You need to create a game first");
                }
                else if (!gameInstance.isGameOwner(player) && AbstractCommand.getRank(player) != Rank.ADMIN) {
                    gameInstance.setMostRecentGameErrorInvalidPermissions();
                }
                break;
            default:
                if (gameInstance == null) {
                    throw new BadStateException("You need to create a game first");
                }
                break;
        }
        argument.addAction(player);
        if (gameInstance != null) {
            currentGamesChannelMessage.editMessage(gameInstance.toString()).queue();
            if (argument.resetAllReactions) {
                resetReactions();
            }
        }
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
                    argument.emoji.addAsReaction(currentGamesChannelMessage);
                }
            }
        }
    }


    public static void setCurrentGamesChannelMessage(Message message) {
        currentGamesChannelMessage = message;
        resetReactions();
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


    private static void endGame() {
        voidGameMessage("FORCEFULLY ENDED");
        gameInstance = null;
    }


    /**
     * Cross out the game message and remove reactions from it so that it can no longer be played
     *
     * @param message explanation to be prepended onto the game string
     */
    private static void voidGameMessage(String message) {
        currentGamesChannelMessage.editMessage(message + ": ~~" + currentGamesChannelMessage.getContentRaw() + "~~")
                .queue();
        currentGamesChannelMessage.clearReactions().queue();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    CommandInterface[] getSecondaryCommands() {
        return BlackJackArgument.values();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
        checkPermission(event.getMember());
        executeSecondaryArgument(BlackJackArgument.class, 1, args, event);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return "blackjack";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Create a game of blackjack, have some people join, then start it";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getArguments() {
        // TODO Optimisation remove duplication of this across classes with secondary commands
        final StringBuilder sb = new StringBuilder();
        for (BlackJackArgument argument : BlackJackArgument.values()) {
            sb.append(argument.getCommand());
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }


    /**
     * Arguments that are triggered from the blackjack command args
     */
    private enum BlackJackArgument implements CommandInterface {
        NEW {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                if (gameInstance == null) {
                    gameInstance = new GameInstance(event.getMember());
                    sendMessage(event.getTextChannel(), gameInstance.toString());
                }
                else {
                    throw new BadStateException(
                            "Game already created, try joining it (if you can't find it try !blackjack reprint");
                }
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Create a new game";
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public Rank getRequiredRank() {
                return Rank.USER;
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getArguments() {
                return "";
            }
        },
        END {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                if (gameInstance != null) {
                    endGame();
                }
                else {
                    throw new BadStateException("There doesn't seem to be a game running, try making a new one");
                }
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Forcefully end the game";
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public Rank getRequiredRank() {
                return Rank.USER;
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getArguments() {
                return "";
            }
        },
        REPRINT {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                if (gameInstance != null) {
                    voidGameMessage("GAME REPRINTED");
                    sendMessage(event.getTextChannel(), gameInstance.toString());
                }
                else {
                    throw new BadStateException("There doesn't seem to be a game running, try making a new one");
                }
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Reprint the game (use if the board messes up or command emojis are not showing)";
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public Rank getRequiredRank() {
                return Rank.USER;
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getArguments() {
                return "";
            }
        },
        BET {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                if (gameInstance == null) {
                    throw new BadStateException("There must be a game running");
                }
                else if (gameInstance.getGameState() != GameInstance.GameState.LOBBY) {
                    throw new BadStateException("The game can't have started!");
                }

                // Remove leading "bet "
                args = args.substring(4);

                if (args.equalsIgnoreCase("clear") || args.equalsIgnoreCase("c")) {
                    gameInstance.clearBet(event.getMember());
                }
                else {
                    final int numberOfBets = 3;
                    String[] splitArgs = args.split(" ");
                    if (splitArgs.length != numberOfBets) {
                        splitArgs = args.split("/");
                    }
                    if (splitArgs.length != numberOfBets) {
                        throw new BadUserInputException(
                                "Bets need to be in the form '!blackjack bet 0 0 0' in the order main, perfect pair, "
                                        + "21+3");
                    }

                    int[] bets = new int[numberOfBets];
                    for (int i = 0; i < numberOfBets; i++) {
                        try {
                            bets[i] = Integer.parseInt(splitArgs[i]);
                        } catch (NumberFormatException e) {
                            throw new BadUserInputException("Bets must be just numbers!");
                        }
                    }

                    gameInstance.placeBet(event.getMember(), bets[0], bets[1], bets[2]);
                }

                currentGamesChannelMessage.editMessage(gameInstance.toString()).queue();
                event.getMessage().delete().queue();
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Add some bets to the game";
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public Rank getRequiredRank() {
                return Rank.USER;
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getArguments() {
                return "{main bet} {perfect pairs bet} {21+3 bet}";
            }
        },
        KILL {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                if (getRank(event.getMember()).hasPermission(Rank.ADMIN)) {
                    gameInstance = null;
                }
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Forcefully end the game instance";
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public Rank getRequiredRank() {
                return Rank.ADMIN;
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getArguments() {
                return "";
            }
        };


        /**
         * {@inheritDoc}
         */
        @Override
        public String getCommand() {
            return this.toString().toLowerCase().replaceAll("_", " ");
        }
    }



    /**
     * Arguments that are triggered from emojis
     */
    private enum BlackJackEmojiArgument implements EmojiReactionAction {
        JOIN("join game", Emoji.POOP, false) {
            /**
             * {@inheritDoc}
             */
            @Override
            public void addAction(@NotNull Member player) {
                gameInstance.join(player);
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void removeAction(@NotNull Member player) {
                if (this.removeReaction) {
                    throw new ContactEwaException("Bad BlackJackArgument declaration");
                }
                gameInstance.leave(player);
            }
        },
        START("start game", Emoji.OK_HAND, false, true) {
            /**
             * {@inheritDoc}
             */
            @Override
            public void addAction(@NotNull Member player) {
                if (gameInstance != null) {
                    gameInstance.startGame();
                }
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void removeAction(@NotNull Member player) {
            }
        },
        HIT("hit", Emoji.PUNCH, true) {
            /**
             * {@inheritDoc}
             */
            @Override
            public void addAction(@NotNull Member player) {
                gameInstance.hitMe(player);
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void removeAction(@NotNull Member player) {
            }
        },
        STAND("stand", Emoji.RAISED_HAND, true) {
            /**
             * {@inheritDoc}
             */
            @Override
            public void addAction(@NotNull Member player) {
                gameInstance.stand(player);
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void removeAction(@NotNull Member player) {
            }
        },
        SPLIT("split", Emoji.METAL, true) {
            /**
             * {@inheritDoc}
             */
            @Override
            public void addAction(@NotNull Member player) {
                gameInstance.split(player);
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void removeAction(@NotNull Member player) {
            }
        },
        LEAVE("leave game", Emoji.WAVE, false) {
            /**
             * {@inheritDoc}
             */
            @Override
            public void addAction(@NotNull Member player) {
                gameInstance.leave(player);
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void removeAction(@NotNull Member player) {
            }
        },
        END("end game", Emoji.X, false) {
            /**
             * {@inheritDoc}
             */
            @Override
            public void addAction(@NotNull Member player) {
                endGame();
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void removeAction(@NotNull Member player) {
            }
        },
        MAIN_BET("default main bet", Emoji.MONEYBAG, false) {
            /**
             * {@inheritDoc}
             */
            @Override
            public void addAction(@NotNull Member player) {
                gameInstance.placeDefaultMainBet(player);
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void removeAction(@NotNull Member player) {
                if (this.removeReaction) {
                    throw new ContactEwaException("Bad BlackJackArgument declaration");
                }
                gameInstance.removeDefaultMainBet(player);
            }
        },
        SIDE_BET("default side bet", Emoji.GEM, false) {
            /**
             * {@inheritDoc}
             */
            @Override
            public void addAction(@NotNull Member player) {
                gameInstance.placeDefaultSideBets(player);
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void removeAction(@NotNull Member player) {
                if (this.removeReaction) {
                    throw new ContactEwaException("Bad BlackJackArgument declaration");
                }
                gameInstance.removeDefaultSideBets(player);
            }
        },
        BET_SPLIT("split with a bet", Emoji.VULCAN, true) {
            /**
             * {@inheritDoc}
             */
            @Override
            public void addAction(@NotNull Member player) {
                gameInstance.betSplit(player);
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void removeAction(@NotNull Member player) {
            }
        },
        DOUBLE_DOWN("double down", Emoji.ONE_FINGER, true) {
            /**
             * {@inheritDoc}
             */
            @Override
            public void addAction(@NotNull Member player) {
                gameInstance.doubleDown(player);
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void removeAction(@NotNull Member player) {
            }
        };

        private static Map<Emoji, BlackJackEmojiArgument> emojiMappings = setEmojiMappings();
        protected boolean removeReaction;
        private String description;
        private Emoji emoji;
        private boolean resetAllReactions = false;


        BlackJackEmojiArgument(String description, Emoji emoji, boolean removeReaction) {
            this.description = description;
            this.emoji = emoji;
            this.removeReaction = removeReaction;
        }


        BlackJackEmojiArgument(String description, Emoji emoji, boolean removeReaction, boolean resetAllReactions) {
            this.description = description;
            this.emoji = emoji;
            this.removeReaction = removeReaction;
            this.resetAllReactions = resetAllReactions;
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
            return new BlackJackEmojiArgument[]{JOIN, START, MAIN_BET, SIDE_BET};
        }


        static BlackJackEmojiArgument[] getGameCommands() {
            return new BlackJackEmojiArgument[]{HIT, STAND, DOUBLE_DOWN, SPLIT, BET_SPLIT, LEAVE, END};
        }
    }
}
