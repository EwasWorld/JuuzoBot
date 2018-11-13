package CommandsBox;

import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.core.entities.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;



/**
 * created: 1/10/18
 */
public enum Emoji {
    POOP("poop", "\uD83D\uDCA9"), OK_HAND("ok_hand", "\uD83D\uDC4C"), PUNCH("punch", "\uD83D\uDC4A"),
    RAISED_HAND("raised_hand", "✋"), METAL("metal", "heavy_metal", "\uD83E\uDD18"), WAVE("wave", "\uD83D\uDC4B"),
    X("x", "❌"), HEARTS("hearts", "♥"), CLUBS("clubs", "♣"), DIAMONDS("diamonds", "♦"), SPADES("spades", "♠"),
    MONEYBAG("moneybag", "💰"), GEM("gem", "\uD83D\uDC8E"), SCALES("scales", "⚖"),
    VULCAN("vulcan", "vulcan_salute", "\uD83D\uDD96"), ONE_FINGER("point_up", "☝");

    private static Map<String, Emoji> parseMap = setUpParseMap();
    private String discordAlias;
    // TODO Get this from unicode2char
    private String unicodeFullString;
    private String unicode2char;


    Emoji(String discordAlias, String emojiManagerAliasAlt, String unicodeFullString) {
        final String aliasSurround = ":";
        this.discordAlias = aliasSurround + discordAlias.replaceAll(aliasSurround, "") + aliasSurround;
        if (emojiManagerAliasAlt != null) {
            emojiManagerAliasAlt = emojiManagerAliasAlt.replaceAll(aliasSurround, "");
        }
        else {
            emojiManagerAliasAlt = this.discordAlias;
        }
        this.unicodeFullString = unicodeFullString;
        unicode2char = EmojiManager.getForAlias(emojiManagerAliasAlt).getUnicode();
        if (unicode2char == null) {
            throw new IllegalArgumentException("Could not find alias " + discordAlias + " in EmojiManager");
        }
    }


    Emoji(String discordAlias, String unicodeFullString) {
        this(discordAlias, null, unicodeFullString);
    }


    static Optional<Emoji> getFromString(String string) {
        if (parseMap.containsKey(string)) {
            return Optional.of(parseMap.get(string));
        }
        else {
            return Optional.empty();
        }
    }


    private static Map<String, Emoji> setUpParseMap() {
        Map<String, Emoji> map = new HashMap<>();
        for (Emoji emoji : Emoji.values()) {
            map.put(emoji.unicode2char, emoji);
        }
        return map;
    }


    public String getDiscordAlias() {
        return discordAlias;
    }


    public void addAsReaction(Message message) {
        message.addReaction(unicodeFullString).complete();
    }
}
