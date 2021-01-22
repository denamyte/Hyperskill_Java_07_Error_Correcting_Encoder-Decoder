package previous;

import java.util.Random;
import java.util.Scanner;

public class Main1 {
    public static void main(String[] args) {
        String subject = new Scanner(System.in).nextLine();
        System.out.println(new ErrorEmulator(subject).getDistorted());
    }
}

class ErrorEmulator {
    private static final String SYMBOLS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ";
    private final Random rnd = new Random();
    private final String subject;
    private StringBuilder sbDistorted;

    ErrorEmulator(String subject) {
        this.subject = subject;
        distortSubject();
    }

    void distortSubject() {
        sbDistorted = new StringBuilder();
        int lastInd = subject.length() - subject.length() % 3;
        for (int start = 0; start < lastInd; start += 3) {
            int distortIndex = rnd.nextInt(3) + start;
            for (int j = start; j < start + 3; j++) {
                char ch = subject.charAt(j);
                sbDistorted.append(j != distortIndex ? ch : distortChar(ch));
            }
        }
        sbDistorted.append(subject.substring(lastInd));
    }

    char distortChar(char ch) {
        int chIndex = SYMBOLS.indexOf(ch);
        int rndIndex = -1;
        while (rndIndex < 0 || chIndex == rndIndex) {
            rndIndex = rnd.nextInt(SYMBOLS.length());
        }
        return SYMBOLS.charAt(rndIndex);
    }

    public String getDistorted() {
        return sbDistorted.toString();
    }
}
