package com.raffaeleconforti.splitminer.splitminer.dfgp;

import com.raffaeleconforti.splitminer.log.graph.LogNode;

/**
 * Created by Adriano on 24/10/2016.
 */
public class DFGNode extends LogNode {

    public DFGNode() {
        super();
    }

    public DFGNode(String label) {
        super(label);
    }

    public DFGNode(String label, int code) {
        super(label, code);
    }

    public String print() {
        return getCode() + " [" + getFrequency() + "]";
    }
}
