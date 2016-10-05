package com.raffaeleconforti.foreignkeydiscovery.functionaldependencies;

/**
 * This class provides basic methods for clearing, setting and testing bits on
 * an integer variable. The underlying code has been adapted from the book
 * "Java ist eine Insel, C. Ulleboom".
 *
 * @author Tobias
 */
public class Bits {
    /**
     * Sets the bit at the specified position to true.
     * <p/>
     * The position range from 0 to 31
     *
     * @param n   The bit coded integer value
     * @param pos The bit position to be set true
     * @return The new coded integer bit
     */
    public static int setBit(int n, int pos) {
        return n | (1 << pos);
    }

    /**
     * Sets the bit at the specified position to false.
     * <p/>
     * The position range from 0 to 31
     *
     * @param n   The bit coded integer value
     * @param pos The bit position to be set false
     * @return The new coded integer bit
     */

    public static int clearBit(int n, int pos) {
        return n & ~(1 << pos);
    }

    /**
     * Tests if the bit at the specified position is true.
     * <p/>
     * The position range from 0 to 31
     *
     * @param n   The bit coded integer value
     * @param pos The bit position to be set false
     * @return True, if bit at specified position is true, false otherwise
     */
    public static boolean testBit(int n, int pos) {
        return (n & 1 << pos) != 0;
    }
}
