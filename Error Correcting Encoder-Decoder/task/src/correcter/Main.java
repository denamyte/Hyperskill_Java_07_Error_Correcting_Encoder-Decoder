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
        operationMap.get(Command.valueOf(input)).action();
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

    public void action() {
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
            copy[i] += ((bit & copy[i]) == 0 ? 1 : -1) * bit;
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
        // TODO: 2/3/21 Implement

        return null;
    }
}

/** A class for reading an arbitrary amount of bits from a byte array. */
class BitsReader {
    private final byte[] bytes;
    /** The size of the storage in bits. */
    private int pos;

    public BitsReader(byte[] bytes) {
        this.bytes = bytes;
    }

    public boolean hasNext() {
        return pos < bytes.length * 8;
    }

    public Integer next() {
        int positioned = 1 << 7 - pos % 8;
        return hasNext() ? bytes[pos++ / 8] & positioned : -1;
    }

    public int[] readBits(int count) {
        return hasNext()
                ? IntStream.generate(this::next).limit(count).map(bit -> bit > 0 ? 1 : 0).toArray()
                : new int[0];
    }
}

/** A class for writing an arbitrary amount of bits into a predefined array. */
class BitsWriter {
    private final byte[] bytes;
    private int pos;

    public BitsWriter(int size) {
        bytes = new byte[size];
    }

    public void writeBits(int... bits) {
        for (int bit : bits) {
            if (pos >= bytes.length * 8) {
                return;
            }
            if (bit == 1) {
                bytes[pos / 8] |= bit << 7 - pos % 8;
            }
            pos++;
        }
    }

    public byte[] getBytes() {
        return bytes;
    }
}
