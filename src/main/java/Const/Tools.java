package main.java.Const;

public class Tools {
    public static String replaceStringWithAnother(String line, String toReplace, String replaceWith) {
        String newStr = "";
        for (String letter : line.split("")) {
            if (letter.equals(toReplace)) {
                newStr += replaceWith;
            }
            else {
                newStr += letter;
            }
        }
        return newStr;
    }
}
