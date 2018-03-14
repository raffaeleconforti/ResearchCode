package com.raffaeleconforti.statistics.outlierremover;

import com.raffaeleconforti.log.util.NameExtractor;
import com.raffaeleconforti.outliers.Outlier;
import com.raffaeleconforti.statistics.OutlierMap;
import com.raffaeleconforti.statistics.outlieridentifiers.OutlierIdentifierGenerator;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;

import java.util.Map;

/**
 * Created by conforti on 12/02/15.
 */
public abstract class OutlierRemoverAbstract implements OutlierRemover {

    protected final OutlierIdentifierGenerator<String> outlierIdentifierGenerator;
    protected OutlierMap<String> mapOutliers;
    protected Outlier<String> outlier;
    private final NameExtractor nameExtractor;

    public OutlierRemoverAbstract(OutlierIdentifierGenerator outlierIdentifierGenerator, XEventClassifier xEventClassifier) {
        this.outlierIdentifierGenerator = outlierIdentifierGenerator;
        this.nameExtractor = new NameExtractor(xEventClassifier);
    }

    public void setMapOutliers(OutlierMap<String> mapOutliers) {
        this.mapOutliers = mapOutliers;
    }

    protected void select(Map<Outlier<String>, Integer> removed, boolean smallestOrLargest) {
        int min = (smallestOrLargest)?Integer.MAX_VALUE:0;
        for (Map.Entry<Outlier<String>, Integer> entry : removed.entrySet()) {
            boolean removeSafe = true;
            for(Outlier<String> outlier1 : mapOutliers.getOutliers(entry.getKey().getIdentifier())) {
                if(!outlier1.isReal()) {
                    removeSafe = false;
                    System.out.println("not safe");
                    break;
                }
            }
            if(removeSafe) {
                if (smallestOrLargest && entry.getValue() > 0 && entry.getValue() < min) {
                    min = entry.getValue();
                    outlier = entry.getKey();
                } else {
                    if (!smallestOrLargest && entry.getValue() > 0 && entry.getValue() > min) {
                        min = entry.getValue();
                        outlier = entry.getKey();
                    }
                }
            }
        }
    }

    protected String getEventName(XEvent event) {
        return nameExtractor.getEventName(event);
    }

}
