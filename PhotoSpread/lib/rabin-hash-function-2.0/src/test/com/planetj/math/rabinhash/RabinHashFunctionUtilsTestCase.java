package com.planetj.math.rabinhash;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public final class RabinHashFunctionUtilsTestCase extends TestCase {

    public static Test suite() {
        return new TestSuite(RabinHashFunctionUtilsTestCase.class);
    }

    public void testDegree32Polynomial() {
        assertEquals("x^32", RabinHashFunctionUtils.polynomialToString(0));
        assertEquals("x^32 + 1", RabinHashFunctionUtils.polynomialToString(1));
        assertEquals("x^32 + x", RabinHashFunctionUtils.polynomialToString(2));
        assertEquals("x^32 + x^2 + x + 1", RabinHashFunctionUtils.polynomialToString(7));
        assertEquals("x^32 + x^31", RabinHashFunctionUtils.polynomialToString(0x80000000));
    }


    public void testDegree64Polynomial() {
        assertEquals("x^64", RabinHashFunctionUtils.polynomialToString(0L));
        assertEquals("x^64 + 1", RabinHashFunctionUtils.polynomialToString(1L));
        assertEquals("x^64 + x", RabinHashFunctionUtils.polynomialToString(2L));
        assertEquals("x^64 + x^2 + x + 1", RabinHashFunctionUtils.polynomialToString(7L));
        assertEquals("x^64 + x^63", RabinHashFunctionUtils.polynomialToString(0x8000000000000000L));
    }

}
