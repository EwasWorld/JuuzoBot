package CommandsBox;

import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.core.entities.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;



/**
 * created: 1/10/18
 * alias lookups: https://github.com/vdurmont/emoji-java
 */
public enum Emoji {
    POOP("poop", "\uD83D\uDCA9"), OK_HAND("ok_hand", "\uD83D\uDC4C"), PUNCH("punch", "\uD83D\uDC4A"),
    RAISED_HAND("raised_hand", "✋"), METAL("metal", "heavy_metal", "\uD83E\uDD18"), WAVE("wave", "\uD83D\uDC4B"),
    X("x", "❌"), HEARTS("hearts", "♥"), CLUBS("clubs", "♣"), DIAMONDS("diamonds", "♦"), SPADES("spades", "♠"),
    MONEYBAG("moneybag", "💰"), GEM("gem", "\uD83D\uDC8E"), SCALES("scales", "⚖"),
    VULCAN("vulcan", "vulcan_salute", "\uD83D\uDD96"), ONE_FINGER("point_up", "☝"), ZERO("zero", "0⃣ "),
    ONE("one", "1⃣ "), TWO("two", "2⃣ "), THREE("three", "3⃣ "), FOUR("four", "4⃣ "), FIVE("five", "5⃣ "),
    SIX("six", "6⃣ "), SEVEN("seven", "7⃣ "), EIGHT("eight", "8⃣ "), NINE("nine", "9⃣ "),
    TEN("keycap_ten", "\uD83D\uDD1F"),
    LETTER_A(":regional_indicator_a:", ":regional_indicator_symbol_a:", "🇦"),
    LETTER_B(":regional_indicator_b:", ":regional_indicator_symbol_b:", "🇧"),
    LETTER_C(":regional_indicator_c:", ":regional_indicator_symbol_c:", "🇨"),
    LETTER_D(":regional_indicator_d:", ":regional_indicator_symbol_d:", "🇩"),
    LETTER_E(":regional_indicator_e:", ":regional_indicator_symbol_e:", "🇪"),
    LETTER_F(":regional_indicator_f:", ":regional_indicator_symbol_f:", "🇫"),
    LETTER_G(":regional_indicator_g:", ":regional_indicator_symbol_g:", "🇬"),
    LETTER_H(":regional_indicator_h:", ":regional_indicator_symbol_h:", "🇭"),
    LETTER_I(":regional_indicator_i:", ":regional_indicator_symbol_i:", "🇮"),
    LETTER_J(":regional_indicator_j:", ":regional_indicator_symbol_j:", "🇯"),
    LETTER_K(":regional_indicator_k:", ":regional_indicator_symbol_k:", "🇰"),
    LETTER_L(":regional_indicator_l:", ":regional_indicator_symbol_l:", "🇱"),
    LETTER_M(":regional_indicator_m:", ":regional_indicator_symbol_m:", "🇲"),
    LETTER_N(":regional_indicator_n:", ":regional_indicator_symbol_n:", "🇳"),
    LETTER_O(":regional_indicator_o:", ":regional_indicator_symbol_o:", "🇴"),
    LETTER_P(":regional_indicator_p:", ":regional_indicator_symbol_p:", "🇵"),
    LETTER_Q(":regional_indicator_q:", ":regional_indicator_symbol_q:", "🇶"),
    LETTER_R(":regional_indicator_r:", ":regional_indicator_symbol_r:", "🇷"),
    LETTER_S(":regional_indicator_s:", ":regional_indicator_symbol_s:", "🇸"),
    LETTER_T(":regional_indicator_t:", ":regional_indicator_symbol_t:", "🇹"),
    LETTER_U(":regional_indicator_u:", ":regional_indicator_symbol_u:", "🇺"),
    LETTER_V(":regional_indicator_v:", ":regional_indicator_symbol_v:", "🇻"),
    LETTER_W(":regional_indicator_w:", ":regional_indicator_symbol_w:", "🇼"),
    LETTER_X(":regional_indicator_x:", ":regional_indicator_symbol_x:", "🇽"),
    LETTER_Y(":regional_indicator_y:", ":regional_indicator_symbol_y:", "🇾"),
    LETTER_Z(":regional_indicator_z:", ":regional_indicator_symbol_z:", "🇿");

    private static Map<String, Emoji> parseMap = setUpParseMap();
    private String discordAlias;
    // TODO Optimisation - get this from unicode2char
    private String unicodeFullString;
    private String unicode2char;


    Emoji(String discordAlias, String unicodeFullString) {
        this(discordAlias, null, unicodeFullString);
    }


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


    public static Optional<Emoji> getFromString(String string) {
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
