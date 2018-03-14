package com.raffaeleconforti.efficientlog;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeTimestamp;

import java.util.Date;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/08/2016.
 */
public class XAttributeTimestampImpl extends XAttributeImpl implements
        XAttributeTimestamp {

    /**
     *
     */
    private static final long serialVersionUID = -4627152242051009472L;

    /**
     * Value of the attribute.
     */
    private Date value;

    /**
     * Creates a new instance.
     *
     * @param key
     *            The key of the attribute.
     * @param value
     *            Value of the attribute.
     */
    public XAttributeTimestampImpl(String key, Date value) {
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
    public XAttributeTimestampImpl(String key, Date value, XExtension extension) {
        super(key, extension);
        setValue(value);
    }

    /**
     * Creates a new instance.
     *
     * @param key
     *            The key of the attribute.
     * @param millis
     *            Value of the attribute, in milliseconds.
     */
    public XAttributeTimestampImpl(String key, long millis) {
        this(key, millis, null);
    }

    /**
     * Creates a new instance.
     *
     * @param key
     *            The key of the attribute.
     * @param millis
     *            Value of the attribute, in milliseconds.
     * @param extension
     *            The extension of the attribute.
     */
    public XAttributeTimestampImpl(String key, long millis, XExtension extension) {
        this(key, new Date(millis), extension);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.XAttributeTimestamp#getValue()
     */
    public Date getValue() {
        return this.value;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.XAttributeTimestamp#getValueMillis()
     */
    public long getValueMillis() {
        return this.value.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.XAttributeTimestamp#setValue(java.util.Date)
     */
    public void setValue(Date value) {
        if (value == null) {
            throw new NullPointerException(
                    "No null value allowed in timestamp attribute!");
        }
        this.value = value;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.XAttributeTimestamp#setValueMillis(long)
     */
    public void setValueMillis(long value) {
        this.value.setTime(value);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        synchronized (FORMATTER) {
            return FORMATTER.format(this.value);
        }
    }

    public Object clone() {
        XAttributeTimestampImpl clone = (XAttributeTimestampImpl) super.clone();
        clone.value = new Date(clone.value.getTime());
        return clone;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof XAttributeTimestamp) { // compares types
            XAttributeTimestamp other = (XAttributeTimestamp) obj;
            return super.equals(other) // compares keys
                    && value.equals(other.getValue()); // compares values
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(XAttribute other) {
        if (!(other instanceof XAttributeTimestamp)) {
            throw new ClassCastException();
        }
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }
        return value.compareTo(((XAttributeTimestamp)other).getValue());
    }
}
