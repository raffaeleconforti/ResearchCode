package com.raffaeleconforti.entropy;

import java.util.Arrays;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 26/08/2016.
 */
public class EntanglementState {

    private int[] stage;
    private boolean skip = false;
    Integer hashCode = null;

    public EntanglementState(int[] stage) {
        this.stage = Arrays.copyOf(stage, stage.length);
    }

    public EntanglementState(EntanglementState entanglementState) {
        this.stage = Arrays.copyOf(entanglementState.stage, entanglementState.stage.length);
    }

    public void set(int pos, int value) {
        stage[pos] = value;
    }

    public int get(int pos) {
        return stage[pos];
    }

    public void skip() {
        this.skip = true;
    }

    public boolean isSkip() {
        return skip;
    }

    public int size() {
        return stage.length;
    }

    public String toString() {
        return Arrays.toString(stage);
    }

    public boolean equals(Object o) {
        if(o instanceof EntanglementState) {
            EntanglementState entanglementState = (EntanglementState) o;
            return Arrays.equals(stage, entanglementState.stage);
        }
        return false;
    }

    public int hashCode() {
        if(hashCode == null) {
            hashCode = Arrays.hashCode(stage);
        }
        return hashCode;
    }

    public EntanglementState getSignatureState() {
        int[] signature = Arrays.copyOfRange(stage, 1, stage.length);
        if(signature.length > 0) Arrays.sort(signature);

        int[] uniqueStage = new int[stage.length];
        uniqueStage[0] = stage[0];
        for(int i = 1; i < uniqueStage.length; i++) {
            uniqueStage[i] = signature[i - 1];
        }

        return new EntanglementState(uniqueStage);
    }
}
