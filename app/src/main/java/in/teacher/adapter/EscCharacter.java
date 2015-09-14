package in.teacher.adapter;

/**
 * Created by vinkrish.
 */

public class EscCharacter {
    public static String removeSplChar(String s) {
        return s.replaceAll("[^a-zA-Z0-9,/ ]+", " ");
    }

}
