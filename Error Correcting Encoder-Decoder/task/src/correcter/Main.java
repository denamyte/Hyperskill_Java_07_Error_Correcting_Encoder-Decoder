package correcter;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    private static final Map<Command, Operation> operationMap = Map.of(
            Command.ENCODE, new EncodeOperation(),
            Command.SEND, new SendOperation(),
            Command.DECODE, new DecodeOperation()
    );

    public static void main(String[] args) {
        System.out.print("Write a mode: ");
        String input = new Scanner(System.in).nextLine().toUpperCase();
        Command command = Command.valueOf(input);
        operationMap.get(command).action();
    }
}

enum Command {
    ENCODE, SEND, DECODE
}

abstract class Operation {
    /** Returns the name of the file to read from. */
    protected abstract String getReadName();

    /** Returns the name of the file to write to. */
    protected abstract String getWriteName();

    /** The only open method in this class hierarchy. Performs read-write operations and calls
     * in each successor the method with a specific byte-changing operation. */
    public void action() {
        try (InputStream inputStream = new FileInputStream(getReadName());
             OutputStream outputStream = new FileOutputStream(getWriteName())) {
            ByteArrayInputStream is = new ByteArrayInputStream(inputStream.readAllBytes());
            performOperation(is).writeTo(outputStream);
        } catch (IOException ignored) {
        }
    }

    /** Performs a specific operation in order to change the bytes from the input stream. */
    protected abstract ByteArrayOutputStream performOperation(ByteArrayInputStream is);

    /** Reads bytes from the input stream and converts them to a collection of bits */
    protected static List<Integer> getBits(ByteArrayInputStream is) {
        return IntStream.generate(is::read)
                .takeWhile(value -> value > -1)
                .flatMap(Operation::byteToBits)
                .boxed()
                .collect(Collectors.toList());
    }

    protected static int[] getBits2(ByteArrayInputStream inputStream) {
        // TODO: 2/3/21 Try to work with array instead of collection

        return new int[0];
    }

    private static IntStream byteToBits(int value) {
        return IntStream.iterate(7, i -> i - 1).limit(8)
                .map(shift -> value & (1 << shift))
                .map(result -> result == 0 ? 0 : 1);
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
    protected ByteArrayOutputStream performOperation(ByteArrayInputStream is) {
        //  - turn bytes into bits(0 or 1) and write them to a collection
        //  - complement the collection of bits with 0s to a length multiple of 3
        //  - take portions of 3 bits, complement them with a parity bit, double each bit and save
        //    a byte to the output stream
        List<Integer> bits = getBits(is);
        while (bits.size() % 3 != 0) {
            bits.add(0);
        }
        return IntStream.range(0, bits.size() / 3)
                .map(i -> i * 3)
                .mapToObj(i -> bits.subList(i, i + 3))
                .map(EncodeOperation::encodeBitsToByte)
                .collect(() -> new ByteArrayOutputStream(bits.size() / 3),
                         ByteArrayOutputStream::write, (o1, o2) -> {});
    }

    private static int encodeBitsToByte(final List<Integer> bits) {
        var bits4 = new ArrayList<>(bits);
        int parity = bits.stream().reduce(0, Integer::sum) % 2;
        bits4.add(parity);

        return IntStream.range(0, 4).map(i -> (3 * bits4.get(i)) << (6 - i * 2))
                .sum();
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
    protected ByteArrayOutputStream performOperation(ByteArrayInputStream is) {
        // TODO: 2/3/21 Implement

        return new ByteArrayOutputStream();
    }

    private int distort(int value) {
        int bit = 1 << rnd.nextInt(8);
        return value + ((bit & value) == 0 ? 1 : -1) * bit;
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
    protected ByteArrayOutputStream performOperation(ByteArrayInputStream is) {
        // TODO: 2/3/21 Implement

        return new ByteArrayOutputStream();
    }
}
