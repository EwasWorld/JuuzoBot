package CommandsBox;

import CharacterBox.AttackBox.Weapon;
import CharacterBox.BroadInfo.Background;
import CharacterBox.BroadInfo.Clazz;
import CharacterBox.BroadInfo.Race;
import CharacterBox.BroadInfo.SubRace;
import CharacterBox.UserCharacter;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import ExceptionsBox.ContactEwaException;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;



/**
 * created 13/11/18 (collated other character commands)
 */
public class CharacterCommand extends AbstractCommand {
    // TODO Optimisation - this better
    private static Map<Long, UserCharacter> partialCharacters = new HashMap<>();
    private static Map<Long, CreationStage> creationStages = new HashMap<>();
    private static Map<Long, Message> creationMessages = new HashMap<>();
    private static Map<Long, Integer> creationMessageReactionLengths = new HashMap<>();
    private static BiMap<Emoji, Integer> emojiArguments = createEmojiArgumentsMap();


    private static BiMap<Emoji, Integer> createEmojiArgumentsMap() {
        BiMap<Emoji, Integer> emojiArguments = HashBiMap.create();
        emojiArguments.put(Emoji.LETTER_A, 0);
        emojiArguments.put(Emoji.LETTER_B, 1);
        emojiArguments.put(Emoji.LETTER_C, 2);
        emojiArguments.put(Emoji.LETTER_D, 3);
        emojiArguments.put(Emoji.LETTER_E, 4);
        emojiArguments.put(Emoji.LETTER_F, 5);
        emojiArguments.put(Emoji.LETTER_G, 6);
        emojiArguments.put(Emoji.LETTER_H, 7);
        emojiArguments.put(Emoji.LETTER_I, 8);
        emojiArguments.put(Emoji.LETTER_J, 9);
        emojiArguments.put(Emoji.LETTER_K, 10);
        emojiArguments.put(Emoji.LETTER_L, 11);
        emojiArguments.put(Emoji.LETTER_M, 12);
        emojiArguments.put(Emoji.LETTER_N, 13);
        emojiArguments.put(Emoji.LETTER_O, 14);
        emojiArguments.put(Emoji.LETTER_P, 15);
        emojiArguments.put(Emoji.LETTER_Q, 16);
        emojiArguments.put(Emoji.LETTER_R, 17);
        emojiArguments.put(Emoji.LETTER_S, 18);
        emojiArguments.put(Emoji.LETTER_T, 19);
        emojiArguments.put(Emoji.LETTER_U, 20);
        emojiArguments.put(Emoji.LETTER_V, 21);
        emojiArguments.put(Emoji.LETTER_W, 22);
        emojiArguments.put(Emoji.LETTER_X, 23);
        emojiArguments.put(Emoji.LETTER_Y, 24);
        emojiArguments.put(Emoji.LETTER_Z, 25);
        return emojiArguments;
    }


    /**
     * Executes the command associated with the emoji. Does nothing if there is no command associated with the emoji
     *
     * @param messageReaction an emoji in the form of a one/two char array
     * @param player the one who invoked the command
     * @return true if the reaction belonged to this method
     */
    public static boolean executeFromAddReaction(Long messageID, MessageReaction messageReaction, Member player) {
        if (!creationMessages.containsKey(messageID)) {
            return false;
        }
        final Message message = creationMessages.get(messageID);
        if (message.getMentionedMembers().size() != 1 || !message.getMentionedMembers().contains(player)) {
            return false;
        }

        final Optional<Emoji> emojiOptional = Emoji.getFromString(messageReaction.getReactionEmote().getName());
        if (!emojiOptional.isPresent() || !emojiArguments.containsKey(emojiOptional.get())) {
            return false;
        }
        final Long authorID = player.getUser().getIdLong();
        if (!partialCharacters.containsKey(authorID)) {
            throw new BadStateException("I can't find your character for some reason... Maybe try starting again?");
        }

        /*
         * Is emoji valid for this creation stage
         */
        final UserCharacter character = partialCharacters.get(authorID);
        final int selection = emojiArguments.get(emojiOptional.get());
        final CreationStage creationStage = creationStages.get(authorID);
        Race.RaceEnum race = null;
        if (creationStage != CreationStage.RACE) {
            race = character.getRace();
        }
        if (selection > creationStage.getOptions(race).length) {
            return false;
        }

        /*
         * Update
         */
        creationStage.update(character, selection);

        final Optional<CreationStage> nextStageOptional = creationStage.getNextStage();
        if (nextStageOptional.isPresent()) {
            /*
             * Prepare for the next choice
             */
            race = character.getRace();
            CreationStage nextStage = nextStageOptional.get();
            if (nextStage == CreationStage.SUBRACE && nextStage.getOptions(race).length == 0) {
                // Next stage will always be valid because subrace is not last
                nextStage = nextStage.getNextStage().get();
            }
            creationStages.put(authorID, nextStage);
            message.editMessage(message.getContentRaw().split("\n")[0] + "\n" + nextStage.optionsToString(race))
                    .queue();

            /*
             * Update reactions
             */
            int reactionsLength = nextStage.getOptions(character.getRace()).length;
            if (reactionsLength > creationMessageReactionLengths.get(messageID)) {
                for (int i = creationMessageReactionLengths.get(messageID); i < reactionsLength; i++) {
                    emojiArguments.inverse().get(i).addAsReaction(message);
                }
            }
            messageReaction.removeReaction(player.getUser()).complete();
        }
        else {
            /*
             * Complete the creation and clean up
             */
            character.completeCreation(player.getUser().getId());
            message.editMessage("__**Creation complete**__ :tada: \n" + character.getDescription()).queue();
            partialCharacters.remove(authorID);
            creationStages.remove(authorID);
            creationMessages.remove(messageID);
            message.clearReactions().queue();
        }

        return true;
    }


    /**
     * Adds the given message to the map of creationMessages under the authorID
     */
    public static void addMessage(Message message) {
        if (message.getContentRaw().startsWith(SecondaryArg.NEW.messageStart)) {
            creationMessages.put(message.getIdLong(), message);

            Long authorID = message.getMentionedUsers().get(0).getIdLong();
            int reactionsLength = creationStages.get(authorID).getOptions(null).length;
            for (int i = 0; i < reactionsLength; i++) {
                emojiArguments.inverse().get(i).addAsReaction(message);
            }
            creationMessageReactionLengths.put(message.getIdLong(), reactionsLength);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    CommandInterface[] getSecondaryCommands() {
        return SecondaryArg.values();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.DND;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
        checkPermission(event.getMember());
        executeSecondaryArgument(SecondaryArg.class, 2, args, event);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return "char";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Create a d&d character... nerd.";
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
        final StringBuilder sb = new StringBuilder();
        for (SecondaryArg argument : SecondaryArg.values()) {
            sb.append(argument.getCommand());
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }


    private interface CreationStageActions {
        /**
         * Update the character with the desired update for the stage
         *
         * @param index the chosen option's index in getOptions
         */
        void update(UserCharacter userCharacter, int index);


        /**
         * @param race cannot be null for SubRace
         * @return all possible options the user can choose at this stage
         */
        Object[] getOptions(Race.RaceEnum race);
    }



    private enum SecondaryArg implements CommandInterface {
        NEW("New character for ") {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                if (args.length() == 0) {
                    throw new BadUserInputException("No name for your brave adventurer?");
                }
                String message = messageStart + event.getMember().getAsMention() + "\n";
                final long authorID = event.getAuthor().getIdLong();
                if (partialCharacters.containsKey(authorID)) {
                    message
                            += "You're already in the process of making a character, but I guess you can start again."
                            + "..\n";
                }
                partialCharacters.put(authorID, new UserCharacter(args));
                creationStages.put(authorID, CreationStage.RACE);
                message += CreationStage.RACE.optionsToString(null);
                sendMessage(event.getChannel(), message);
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Create a new character";
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
                return "{character name}";
            }
        },
        DELETE {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                final List<User> mentioned = event.getMessage().getMentionedUsers();
                if (mentioned.size() == 0) {
                    UserCharacter.deleteCharacter(event.getAuthor().getId(), args);
                }
                else {
                    // Allow admins to delete any character
                    checkPermission(event.getMember(), Rank.ADMIN);
                    if (mentioned.size() > 1) {
                        throw new BadUserInputException("Only mention one person");
                    }
                    int mentionLength = args.split(" ")[0].length() + 1;
                    if (args.length() > mentionLength) {
                        UserCharacter.deleteCharacter(mentioned.get(0).getId(), args.substring(mentionLength));
                    }
                    else {
                        throw new BadUserInputException(
                                "What is the name of the character to be deleted? Put this after the @user)");
                    }
                }
                sendMessage(event.getChannel(), "Character deleted");
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Delete one of your character (admins can delete any character)";
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
                return "[@character owner (admins only)] {character name}";
            }
        },
        ATTACK {
            /**
             * {@inheritDoc}
             * @param args {victim}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                final String[] splitArgs = SecondaryArg.splitArgsNameAndOther(args);
                sendMessage(
                        event.getChannel(),
                        UserCharacter.attack(event.getAuthor().getId(), splitArgs[0], splitArgs[1]));
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Your character shall attempt a vicious attack on the unsuspecting victim";
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
                return "{character name} {victim's name}";
            }
        },
        WEAPONS_LIST {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                sendMessage(event.getChannel(), Weapon.getWeaponsList());
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "All available weapons";
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
        CHANGE_WEAPON {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                final String[] splitArgs = SecondaryArg.splitArgsNameAndOther(args);
                UserCharacter.changeCharacterWeapon(event.getAuthor().getId(), splitArgs[0], splitArgs[1]);
                sendMessage(event.getChannel(), "Weapon change successful, enjoy your new toy.");
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Swap your character's weapon for a new one";
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
                return "{character name} {weapon name}";
            }
        },
        DESCRIPTION {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                sendMessage(event.getChannel(), UserCharacter.getCharacterDescription(event.getAuthor().getId(), args));
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "A description of your mighty warrior";
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
                return "{character name}";
            }
        };

        String messageStart;


        SecondaryArg() {
        }


        SecondaryArg(String messageStart) {
            this.messageStart = messageStart;
        }


        /**
         * @return new String[] {rest of args, last word}
         */
        private static String[] splitArgsNameAndOther(String args) {
            final String[] argsSplit = args.split(" ");
            final String other = argsSplit[argsSplit.length - 1];
            final int endLength = args.length() - other.length() - 1;
            if (endLength <= 0) {
                throw new BadUserInputException(
                        "You forgot to tell me which of your fine characters you'd like to do this for");
            }
            else {
                return new String[]{args.substring(0, endLength), other};
            }
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public String getCommand() {
            return this.toString().toLowerCase().replaceAll("_", " ");
        }
    }



    enum CreationStage implements CreationStageActions {
        RACE {
            /**
             * {@inheritDoc}
             */
            @Override
            public Object[] getOptions(Race.RaceEnum race) {
                return Race.RaceEnum.values();
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void update(UserCharacter userCharacter, int index) {
                userCharacter.setRace(Race.RaceEnum.values()[index]);
            }
        },
        SUBRACE {
            /**
             * {@inheritDoc}
             */
            @Override
            public Object[] getOptions(Race.RaceEnum race) {
                if (race == null) {
                    throw new ContactEwaException("Some problem with racism and nulls...");
                }
                final SubRace.SubRaceEnum[] values = SubRace.SubRaceEnum.values();
                final List<SubRace.SubRaceEnum> validValues = new ArrayList<>();
                for (SubRace.SubRaceEnum value : values) {
                    if (Race.getRaceInfo(value).getMainRace() == race) {
                        validValues.add(value);
                    }
                }
                return validValues.toArray();
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void update(UserCharacter userCharacter, int index) {
                userCharacter.setSubRace((SubRace.SubRaceEnum) getOptions(userCharacter.getRace())[index]);
            }
        },
        CLASS {
            /**
             * {@inheritDoc}
             */
            @Override
            public Object[] getOptions(Race.RaceEnum race) {
                return Clazz.ClassEnum.values();
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void update(UserCharacter userCharacter, int index) {
                userCharacter.setClazz(Clazz.ClassEnum.values()[index]);
            }
        },
        BACKGROUND {
            /**
             * {@inheritDoc}
             */
            @Override
            public Object[] getOptions(Race.RaceEnum race) {
                return Background.BackgroundEnum.values();
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void update(UserCharacter userCharacter, int index) {
                userCharacter.setBackground(Background.BackgroundEnum.values()[index]);
            }
        };


        Optional<CreationStage> getNextStage() {
            final CreationStage[] values = CreationStage.values();
            for (int i = 0; i < values.length; i++) {
                if (values[i] == this) {
                    if (i + 1 < values.length) {
                        return Optional.of(values[i + 1]);
                    }
                    else {
                        return Optional.empty();
                    }
                }
            }
            throw new IllegalArgumentException("This is somehow not a value...");
        }


        String optionsToString(Race.RaceEnum race) {
            StringBuilder sb = new StringBuilder("```");
            sb.append(String.format("Choose a %s:\n", this.toString().toLowerCase()));
            for (int i = 0; i < getOptions(race).length; i++) {
                sb.append('\t');
                sb.append((char) (i + 65));
                sb.append(" ");
                sb.append(getOptions(race)[i].toString());
                sb.append('\n');
            }
            sb.delete(sb.length() - 1, sb.length());
            sb.append("```");
            return sb.toString();
        }
    }
}