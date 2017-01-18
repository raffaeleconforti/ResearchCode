package com.raffaeleconforti.statistics.mapbuilder;

import com.raffaeleconforti.log.util.NameExtractor;
import com.raffaeleconforti.statistics.outlieridentifiers.OutlierIdentifierGenerator;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;

/**
 * Created by conforti on 12/02/15.
 */
public abstract class OutlierMapBuilderAbstract implements OutlierMapBuilder {

    protected OutlierIdentifierGenerator<String> outlierIdentifierGenerator = null;
    private final NameExtractor nameExtractor;

    public OutlierMapBuilderAbstract(XEventClassifier xEventClassifier) {
        nameExtractor = new NameExtractor(xEventClassifier);
    }

    public void setOutlierIdentifierGenerator(OutlierIdentifierGenerator<String> outlierIdentifierGenerator) {
        this.outlierIdentifierGenerator = outlierIdentifierGenerator;
    }

    protected String getEventName(XEvent event) {
        return nameExtractor.getEventName(event);
    }

}
