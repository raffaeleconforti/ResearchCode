package com.raffaeleconforti.statistics.mapbuilder;

import com.raffaeleconforti.statistics.outlieridentifiers.OutlierIdentifierGenerator;
import org.deckfour.xes.classification.XEventClassifier;

/**
 * Created by conforti on 12/02/15.
 */
public class MapBuilderFactory {

    public final static int NEXT_AND_PREVIOUS = 0;
    public final static int BRIDGE = 1;

    public static OutlierMapBuilder getOutlierMapBuilder(OutlierIdentifierGenerator<String> outlierIdentifierGenerator, int type, XEventClassifier xEventClassifier) {
        OutlierMapBuilder outlierMapBuilder;
        if(type == NEXT_AND_PREVIOUS) {
            outlierMapBuilder = new OutlierMapBuilderNextAndPrevious(xEventClassifier);
        }else {
            outlierMapBuilder = new OutlierMapBuilderBridge(xEventClassifier);
        }
        outlierMapBuilder.setOutlierIdentifierGenerator(outlierIdentifierGenerator);
        return outlierMapBuilder;
    }

}
