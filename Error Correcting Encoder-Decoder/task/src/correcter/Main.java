package correcter;

import java.io.*;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        new EncoderDecoder().rw();
    }
}

class EncoderDecoder {
    Random rnd = new Random();

    public void rw() {
        try (InputStream send = new FileInputStream("send.txt");
             OutputStream receive = new FileOutputStream("received.txt")) {
            int next;
            while ((next = send.read()) != -1) {
                receive.write(distort(next));
            }
        } catch (IOException ignored) {
        }
    }

    private int distort(int value) {
        int bit = 1 << rnd.nextInt(8);
        return value + ((bit & value) == 0 ? 1 : -1) * bit;
    }
}
