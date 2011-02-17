package com.planetj.math.rabinhash;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class RabinHashFunction64TestCase extends TestCase {

    public static Test suite() {
        return new TestSuite(RabinHashFunction64TestCase.class);
    }

    public void testConstructor() {
        new RabinHashFunction64(0);
        // shouldn't throw any exception
        assertNotNull(RabinHashFunction64.DEFAULT_HASH_FUNCTION);
    }

    public void testEquals() {
        assertEquals(RabinHashFunction64.DEFAULT_HASH_FUNCTION,
                     new RabinHashFunction64(RabinHashFunction64.DEFAULT_HASH_FUNCTION.getP()));
    }

    public void testByteArray() {
        runByteArrayTest(new byte[]{}, 0);
        runByteArrayTest(new byte[]{0}, 0);
        runByteArrayTest(new byte[]{1}, 1);
        runByteArrayTest(new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0}, RabinHashFunction64.DEFAULT_HASH_FUNCTION.getP());
        runByteArrayTest(toBytes(RabinHashFunction64.DEFAULT_HASH_FUNCTION.getP()), 0);
    }

    public void testSerializable() throws IOException, ClassNotFoundException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(RabinHashFunction64.DEFAULT_HASH_FUNCTION);

        final byte[] bytes = baos.toByteArray();

        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final RabinHashFunction64 deserialized = (RabinHashFunction64) ois.readObject();

        assertEquals(RabinHashFunction64.DEFAULT_HASH_FUNCTION, deserialized);
        assertEquals(RabinHashFunction64.DEFAULT_HASH_FUNCTION.hash(bytes), deserialized.hash(bytes));
    }

    private void runByteArrayTest(final byte[] b, final long hash) {
        assertEquals(hash, RabinHashFunction64.DEFAULT_HASH_FUNCTION.hash(b));
    }

    private static byte[] toBytes(final long P) {
        return new byte[]{1,
                          (byte) ((P >> 56) & 0xFF),
                          (byte) ((P >> 48) & 0xFF),
                          (byte) ((P >> 40) & 0xFF),
                          (byte) ((P >> 32) & 0xFF),
                          (byte) ((P >> 24) & 0xFF),
                          (byte) ((P >> 16) & 0xFF),
                          (byte) ((P >> 8) & 0xFF),
                          (byte) (P & 0xFF)};
    }

}
