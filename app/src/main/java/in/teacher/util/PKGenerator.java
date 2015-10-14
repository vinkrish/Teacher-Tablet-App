package in.teacher.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by vinkrish.
 */
public class PKGenerator {

    public static long returnPrimaryKey(int schoolId) {
        long unixTime = System.currentTimeMillis() / 1L;
        String s = schoolId + "" + unixTime;
        return Long.parseLong(s, 10);
    }

    public static long getPrimaryKey() {
        return System.currentTimeMillis() / 1L;
    }

    public static String trim(int pos1, int pos2, String s) {
        if (s.length() > pos2) {
            StringBuilder sb = new StringBuilder(s.substring(0, pos2)).append("..");
            return sb.toString();
        } else {
            return s;
        }
    }

    public static int getMD5(int schoolId, int classId, String plaintext) throws NoSuchAlgorithmException {
        StringBuilder sb = new StringBuilder(""+schoolId+classId);

        MessageDigest m = MessageDigest.getInstance("MD5");
        m.reset();
        m.update(plaintext.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        String hashtext = bigInt.toString(16);
        while(hashtext.length() < 32 ){
            hashtext = "0"+hashtext;
        }

        // hashtext is md5 of plaintext

        int count = 0;
        for (int i = 0; i < hashtext.length(); i++) {
            char c = hashtext.charAt(i);
            /*
                int inte = Character.getNumericValue(c);
                System.out.println("character here " + inte);
            */
            if(Character.isDigit(c)) {
                count++;
                sb.append(c+"");
            }
            if (count == 3) break;
        }

        return Integer.parseInt(sb.toString());
    }

}
