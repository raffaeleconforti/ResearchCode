package com.raffaeleconforti.memorylog;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeContinuous;

import java.util.Objects;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/08/2016.
 */
public class XAttributeContinuousImpl extends XAttributeImpl implements
        XAttributeContinuous {

    /**
     *
     */
    private static final long serialVersionUID = -1789813595800348876L;

    /**
     * Value of the attribute.
     */
    private double value;

    /**
     * Creates a new instance.
     *
     * @param key
     *            The key of the attribute.
     * @param value
     *            Value of the attribute.
     */
    public XAttributeContinuousImpl(String key, double value) {
        this(key, value, null);
    }

    /**
     * Creates a new instance.
     *
     * @param key
     *            The key of the attribute.
     * @param value
     *            Value of the attribute.
     * @param extension
     *            The extension of the attribute.
     */
    public XAttributeContinuousImpl(String key, double value,
                                    XExtension extension) {
        super(key, extension);
        this.value = value;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.XAttributeContinuous#getValue()
     */
    public double getValue() {
        return value;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.XAttributeContinuous#setValue(boolean)
     */
    public void setValue(double value) {
        this.value = value;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Double.toString(this.value);
    }

    public Object clone() {
        return super.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof XAttributeContinuous) { // compares types
            XAttributeContinuous other = (XAttributeContinuous) obj;
            return super.equals(other) // compares keys
                    && (value == other.getValue()); // compares values
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), value);
    }

    @Override
    public int compareTo(XAttribute other) {
        if (!(other instanceof XAttributeContinuous)) {
            throw new ClassCastException();
        }
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }
        return ((Double)value).compareTo(((XAttributeContinuous)other).getValue());
    }
}
