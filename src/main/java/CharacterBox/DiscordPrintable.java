package CharacterBox;

import java.util.Arrays;



/**
 * created 19/11/2018
 */
public interface DiscordPrintable {
    static <E extends DiscordPrintable> String getAsPrintableString(E[] objects) {
        return getAsPrintableString(Arrays.asList(objects));
    }


    /**
     * @return each item in the list separated by ,
     */
    static <E extends DiscordPrintable, I extends Iterable<E>> String getAsPrintableString(I objects) {
        final StringBuilder sb = new StringBuilder();
        for (E object : objects) {
            sb.append(object.toPrintableString());
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }


    String toPrintableString();
}
