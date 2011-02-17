package com.planetj.math.rabinhash;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class RabinHashFunction32TestCase extends TestCase {

    public static Test suite() {
        return new TestSuite(RabinHashFunction32TestCase.class);
    }

    public void testConstructor() {
        new RabinHashFunction32(0);
        // shouldn't throw any exception
        assertNotNull(RabinHashFunction32.DEFAULT_HASH_FUNCTION);
    }

    public void testEquals() {
        assertEquals(RabinHashFunction32.DEFAULT_HASH_FUNCTION,
                     new RabinHashFunction32(RabinHashFunction32.DEFAULT_HASH_FUNCTION.getP()));
    }

    public void testSerializable() throws IOException, ClassNotFoundException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(RabinHashFunction32.DEFAULT_HASH_FUNCTION);

        final byte[] bytes = baos.toByteArray();

        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final RabinHashFunction32 deserialized = (RabinHashFunction32) ois.readObject();

        assertEquals(RabinHashFunction32.DEFAULT_HASH_FUNCTION, deserialized);
        assertEquals(RabinHashFunction32.DEFAULT_HASH_FUNCTION.hash(bytes), deserialized.hash(bytes));
    }

    public void testByteArray() {
        runByteArrayTest(new byte[]{}, 0);
        runByteArrayTest(new byte[]{0}, 0);
        runByteArrayTest(new byte[]{1}, 1);
        runByteArrayTest(new byte[]{1, 0, 0, 0, 0}, RabinHashFunction32.DEFAULT_HASH_FUNCTION.getP());
        runByteArrayTest(toBytes(RabinHashFunction32.DEFAULT_HASH_FUNCTION.getP()), 0);
    }

    private void runByteArrayTest(final byte[] b, final int hash) {
        assertEquals(hash, RabinHashFunction32.DEFAULT_HASH_FUNCTION.hash(b));
    }

    private static byte[] toBytes(final int P) {
        return new byte[]{1,
                          (byte) ((P >> 24) & 0xFF),
                          (byte) ((P >> 16) & 0xFF),
                          (byte) ((P >> 8) & 0xFF),
                          (byte) (P & 0xFF)};
    }

}
