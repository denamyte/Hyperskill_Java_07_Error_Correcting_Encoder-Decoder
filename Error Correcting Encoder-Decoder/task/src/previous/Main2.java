package previous;

import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Main2 {
    public static void main_(String[] args) {
        String msg = new Scanner(System.in).nextLine();
        EncoderDecoder.rockAndRoll(msg);
    }
}

class EncoderDecoder {
    private static final String SYMBOLS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ";
    private static final Random rnd = new Random();

    public static void rockAndRoll(String msg) {
        System.out.println(msg);

        String tripled = triple(msg);
        System.out.println(tripled);

        String distorted = distortMessage(tripled);
        System.out.println(distorted);

        String recovered = recover(distorted);
        System.out.println(recovered);

    }

    private static String triple(String msg) {
        StringBuilder sb = new StringBuilder();
        char[] multiplier = new char[3];
        for (char next : msg.toCharArray()) {
            Arrays.fill(multiplier, next);
            sb.append(multiplier);
        }
        return sb.toString();
    }

    static String distortMessage(String msg) {
        char[] chars = msg.toCharArray();
        for (int i = 0; i < chars.length; i += 3) {
            int rndInd = i + rnd.nextInt(3);
            chars[rndInd] = distortChar(chars[rndInd]);
        }
        return new String(chars);
    }

    static char distortChar(char ch) {
        int chIndex = SYMBOLS.indexOf(ch);
        int rndIndex = -1;
        while (rndIndex < 0 || chIndex == rndIndex) {
            rndIndex = rnd.nextInt(SYMBOLS.length());
        }
        return SYMBOLS.charAt(rndIndex);
    }

    private static String recover(String msg) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < msg.length(); i += 3) {
            sb.append(chooseChar(msg, i));
        }
        return sb.toString();
    }

    private static char chooseChar(String msg, int startInd) {
        return msg.charAt(startInd) == msg.charAt(startInd + 1)
                ? msg.charAt(startInd) : msg.charAt(startInd + 2);
    }
}
