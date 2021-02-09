package previous;

import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

public class Main5 {
    private static final Map<String, Operation> operationMap = Map.of(
            "encode", new EncodeOperation(),
            "send", new SendOperation(),
            "decode", new DecodeOperation()
    );

    public static void main_(String[] args) {
        System.out.print("Write a mode: ");
        operationMap.get(new Scanner(System.in).nextLine()).go();
    }
}

abstract class Operation {
    protected abstract String getReadName();

    protected abstract String getWriteName();

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

    protected static int hammingCodeParity(int[] bits, int powerOf2) {
        int s = 0;
        for (int i = powerOf2 - 1; i < 8; i += powerOf2 * 2) {
            for (int j = i; j < i + powerOf2; j++) {
                s += bits[j];
            }
        }
        return s % 2;
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
        BitsWriter writer = new BitsWriter(bytes.length * 2);
        while (reader.hasNext()) {
            int[] dst = copySignificantBits(reader);
            IntStream.of(1, 2, 4).forEach(power -> dst[power - 1] = hammingCodeParity(dst, power));
            writer.writeBits(dst);
        }
        return writer.getBytes();
    }

    private int[] copySignificantBits(BitsReader reader) {
        int[] src = reader.readBits(4);
        int[] dst = new int[8];
        dst[2] = src[0];
        System.arraycopy(src, 1, dst, 4, 3);
        return dst;
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
        BitsWriter writer = new BitsWriter(bytes.length / 2);
        while (reader.hasNext()) {
            int[] src = reader.readBits(8);
            int badIndex = findBadIndex(src);

            // Correcting wrong bit
            if (badIndex >= 0) {
                src[badIndex] = src[badIndex] == 1 ? 0 : 1;
            }

            // Writing down the bits from the corrected array
            writer.writeBits(src[2], src[4], src[5], src[6]);
        }
        return writer.getBytes();
    }

    private int findBadIndex(int[] src) {
        // Obtaining an array without parity bits for calculating parities again
        int[] srcParityCalc = Arrays.copyOf(src, 8);
        IntStream.of(1, 2, 4).forEach(i -> srcParityCalc[i - 1] = 0);

        // Finding out the index with a wrong bit
        // (Selecting the powers of 2 which parities don't match, summing them - here is the wrong index)
        return -1 + IntStream.of(1, 2, 4)
                .map(power -> {
                    int parity = hammingCodeParity(srcParityCalc, power);
                    return parity == src[power - 1] ? 0 : power;
                }).sum();
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
