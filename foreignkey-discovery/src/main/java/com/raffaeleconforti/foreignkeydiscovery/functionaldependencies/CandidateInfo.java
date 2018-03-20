package com.raffaeleconforti.foreignkeydiscovery.functionaldependencies;

import java.util.BitSet;

/**
 * The objects of this class contain the stipped partition and the possible Right-Hand-Side candidates (RHS-candidates)
 * for any attribute sets X.
 * Each attribute set X is related to a stripped partition and the set of possible RHS-candidates (C(X)).
 * To save memory BitSets are used to store attributes.
 * <p/>
 * In TANEjava, two UnifiedMaps<BitSet,CandidateInfo> are used to hold at most two levels.
 *
 * @author Tobias
 */
public class CandidateInfo {
    private BitSet RHSattribute = new BitSet();
    private StrippedPartition sp = null;

    //public methods

    /**
     * The standard constructor initializes the BitSet of the RHS attribute set to all false.
     */
    public CandidateInfo() {

    }

    /**
     * Returns the set of RHS candidates --> C(X)
     *
     * @return BitSet    -RHS candiates.
     */
    public BitSet getRHS() {
        return RHSattribute;
    }

    /**
     * Replaces the RHS candiate by a new BitSet.
     *
     * @param bs - the new RHS candidate
     */
    public void setRHS(BitSet bs) {
        RHSattribute = bs;
    }

    /**
     * Returns the stripped parition
     *
     * @return StrippedPartition    the stripped partition
     */
    public StrippedPartition getStrippedPartition() {
        return sp;
    }

    /**
     * Sets the stripped Parition
     *
     * @param s -the stripped partition
     */
    public void setStrippedPartition(StrippedPartition s) {
        sp = s;
    }

    /**
     * Sets the bit of the RHS attribute set at the specified index to true.
     *
     * @param bitIndex -a bit index
     */
    public void setRHS_Bit(int bitIndex) {
        RHSattribute.set(bitIndex);
    }

    /**
     * Sets the bits of the RHS attribute set from the specified fromIndex (inclusive) to the specified
     * toIndex (exclusive) to true.
     *
     * @param from - index at the first bit to be set
     * @param to   - index after the last bit to be set
     */
    public void setRHS_BitRange(int from, int to) {
        RHSattribute.set(from, to);
    }

    /**
     * Releases the objects and sets all internal variable to null.
     */
    public void clear() {
        if (sp != null) sp.clear();

        sp = null;
        RHSattribute = null;
    }


}
