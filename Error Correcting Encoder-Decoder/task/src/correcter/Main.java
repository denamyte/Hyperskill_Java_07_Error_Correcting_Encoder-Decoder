package correcter;

import java.io.*;
import java.util.*;
import java.util.stream.*;

public class Main {
    private static final Map<Command, Operation> operationMap = Map.of(
            Command.ENCODE, new EncodeOperation(),
            Command.SEND, new SendOperation(),
            Command.DECODE, new DecodeOperation()
    );

    public static void main(String[] args) {
        System.out.print("Write a mode: ");
        String input = new Scanner(System.in).nextLine().toUpperCase();
        operationMap.get(Command.valueOf(input)).go();
    }
}

enum Command {
    ENCODE, SEND, DECODE
}

abstract class Operation {
    protected abstract String getReadName();

    protected abstract String getWriteName();

    /** Performs a specific operation with bytes */
    protected abstract byte[] performOperation(byte[] bytes);

    public void go() {
        try (InputStream inputStream = new FileInputStream(getReadName());
             OutputStream outputStream = new FileOutputStream(getWriteName())) {
            byte[] bytesBefore = inputStream.readAllBytes();
            byte[] bytesAfter = performOperation(bytesBefore);
            outputStream.write(bytesAfter);
        } catch (IOException ignored) {
        }
    }

}

class EncodeOperation extends Operation {

    @Override
    protected String getReadName() {
        return "send.txt";
    }

    @Override
    protected String getWriteName() {
        return "encoded.txt";
    }

    @Override
    protected byte[] performOperation(byte[] bytes) {
        BitsReader reader = new BitsReader(bytes);
        int writeSize = (int) Math.ceil((double) bytes.length * 8 / 3);
        BitsWriter writer = new BitsWriter(writeSize);

        while (reader.hasNext()) {
            int[] bits = reader.readBits(3);
            for (int bit : bits) {
                writer.writeBits(bit, bit);
            }
            int parity = Arrays.stream(bits).reduce(0, Integer::sum) % 2;
            writer.writeBits(parity, parity);
        }
        return writer.getBytes();
    }
}

class SendOperation extends Operation {
    Random rnd = new Random();

    @Override
    protected String getReadName() {
        return "encoded.txt";
    }

    @Override
    protected String getWriteName() {
        return "received.txt";
    }

    @Override
    protected byte[] performOperation(byte[] bytes) {
        byte[] copy = Arrays.copyOf(bytes, bytes.length);
        for (int i = 0; i < copy.length; i++) {
            int bit = 1 << rnd.nextInt(8);
            copy[i] += ((bit & copy[i]) == 0 ? 1 : -1) * bit;  // Spoiling a random bit
        }
        return copy;
    }
}

class DecodeOperation extends Operation {

    @Override
    protected String getReadName() {
        return "received.txt";
    }

    @Override
    protected String getWriteName() {
        return "decoded.txt";
    }

    @Override
    protected byte[] performOperation(byte[] bytes) {
        BitsReader reader = new BitsReader(bytes);
        int writeSize = (int) Math.floor((double) bytes.length * 3 / 8);
        BitsWriter writer = new BitsWriter(writeSize);
        while (reader.hasNext()) {
            final int[] raw = reader.readBits(8);
            int[] bits = IntStream.range(0, 4).map(i -> raw[i * 2] == raw[i * 2 + 1] ? raw[i * 2] : -1).toArray();
            int badIndex = linearSearch(bits, -1);
            // if some bit is distorted and the index of the distorted bit is not the parity index
            if (badIndex != -1 && bits[3] != -1) {
                int recoveredValue = (bits[0] + bits[1] + bits[2] + 1) % 2 == bits[3] ? 0 : 1;
                bits[badIndex] = recoveredValue;
            }
            for (int i = 0; i < 3; i++) {
                writer.writeBits(bits[i]);
            }
        }
        return writer.getBytes();
    }

    private int linearSearch(int[] ar, int value) {
        for (int i = 0; i < ar.length; i++) {
            if (ar[i] == value) {
                return i;
            }
        }
        return -1;
    }
}

class BitsOperations {
    protected final byte[] bytes;
    /** The position of the bit pointer. */
    protected int pos;

    public BitsOperations(byte[] bytes) {
        this.bytes = bytes;
    }

    public int bitPos() {
        return 1 << 7 - pos % 8;
    }

    public boolean hasNext() {
        return pos < bytes.length * 8;
    }

}

/** A class for reading an arbitrary amount of bits from a byte array. */
class BitsReader extends BitsOperations {

    public BitsReader(byte[] bytes) {
        super(bytes);
    }

    public Integer next() {
        if (hasNext()) {
            var res = bytes[pos / 8] & bitPos();
            pos++;
            return res;
        }
        return -1;
    }

    public int[] readBits(int count) {
        return hasNext()
                ? IntStream.generate(this::next).limit(count).map(bit -> bit > 0 ? 1 : 0).toArray()
                : new int[0];
    }
}

/** A class for writing an arbitrary amount of bits into a predefined array. */
class BitsWriter extends BitsOperations {

    public BitsWriter(int size) {
        super(new byte[size]);
    }

    public void writeBits(int... bits) {
        for (int bit : bits) {
            if (!hasNext()) {
                return;
            }
            if (bit == 1) {
                bytes[pos / 8] |= bitPos();
            }
            pos++;
        }
    }

    public byte[] getBytes() {
        return bytes;
    }
}
