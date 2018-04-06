package com.raffaeleconforti.noisefiltering.event.commandline.ui;

import com.raffaeleconforti.noisefiltering.event.commandline.InfrequentBehaviourFilterCommandLine;
import com.raffaeleconforti.noisefiltering.event.selection.NoiseFilterResult;

import java.util.Scanner;

/**
 * Created by conforti on 26/02/15.
 */
public class NoiseFilterPanel {

    private final InfrequentBehaviourFilterCommandLine infrequentBehaviourFilterCommandLine;
    private final double[] arcs;

    private final NoiseFilterResult result;
    private final Scanner console = new Scanner(System.in);
    private final double initialPercentile = 0.125;

    public NoiseFilterPanel(InfrequentBehaviourFilterCommandLine infrequentBehaviourFilterCommandLine, double[] arcs) {

        this.infrequentBehaviourFilterCommandLine = infrequentBehaviourFilterCommandLine;
        this.arcs = arcs;

        double noiseLevelValue = infrequentBehaviourFilterCommandLine.discoverThreshold(arcs, initialPercentile);

        result = new NoiseFilterResult();
        result.setRepeated(true);
        result.setFixLevel(false);
        result.setNoiseLevel(noiseLevelValue);
        result.setPercentile(initialPercentile);

        String token;

        System.out.println("Percentile: " + initialPercentile);
        System.out.println("Repeated: true");
        System.out.println("Fixed noise leve: false");
        System.out.println("Noise level threshold: " + noiseLevelValue);

        System.out.println("Do you want to use different parameters? (y/n)");
        token = null;
        boolean changeParameters = false;
        while(token == null) {
            token = console.nextLine();
            if(!token.isEmpty()) {
                if(token.equalsIgnoreCase("y")) {
                    changeParameters = true;
                }else if(token.equalsIgnoreCase("n")) {
                    changeParameters = false;
                }else {
                    token = null;
                    System.out.println("Accepted parameter Y or N");
                }
            }else {
                token = null;
                System.out.println("Accepted parameter Y or N");
            }
        }

        if(changeParameters) {
            Double percentile = null;
            while (percentile == null || percentile < 0.0 || percentile > 1.0) {
                System.out.println("Select Percentile - Click Enter for default value");
                try {
                    token = console.nextLine();
                    percentile = Double.parseDouble(token);
                    if (percentile < 0.0 || percentile > 1.0) {
                        System.out.println("Select a number between 0 and 1");
                    }
                } catch (NumberFormatException nfe) {
                    if (token.isEmpty()) {
                        percentile = initialPercentile;
                        System.out.println("Value selected: " + initialPercentile);
                    } else {
                        System.out.println("Select a number between 0.0 and 1.0");
                    }
                }
            }
            result.setPercentile(percentile);
            noiseLevelValue = infrequentBehaviourFilterCommandLine.discoverThreshold(arcs, percentile);

            Integer repeated = null;
            while (repeated == null || (repeated != 0.0 && repeated != 1.0)) {
                System.out.println("Do you want to repeat the filtering several times - Click Enter for default value");
                try {
                    token = console.nextLine();
                    repeated = Integer.parseInt(token);
                    if (repeated != 0.0 && repeated != 1.0) {
                        System.out.println("Select a 0 for true and 1 false");
                    }
                } catch (NumberFormatException nfe) {
                    if (token.isEmpty()) {
                        repeated = 0;
                        System.out.println("Value selected: 0");
                    } else {
                        System.out.println("Select a 0 for true and 1 false");
                    }
                }
            }
            result.setRepeated(repeated == 0);

            Integer fixed = null;
            while (fixed == null || (fixed != 0.0 && fixed != 1.0)) {
                System.out.println("Do you want to use a fix noise level - Click Enter for default value");
                try {
                    token = console.nextLine();
                    fixed = Integer.parseInt(token);
                    if (fixed != 0.0 && fixed != 1.0) {
                        System.out.println("Select a 0 for true and 1 false");
                    }
                } catch (NumberFormatException nfe) {
                    if (token.isEmpty()) {
                        fixed = 1;
                        System.out.println("Value selected: 1");
                    } else {
                        System.out.println("Select a 0 for true and 1 false");
                    }
                }
            }
            result.setFixLevel(fixed == 0);

            if(fixed == 0) {
                Double noiseLevel = null;
                while (noiseLevel == null || noiseLevel < 0.0 || noiseLevel > 1.0) {
                    System.out.println("Select noise level - Click Enter for default value");
                    try {
                        token = console.nextLine();
                        noiseLevel = Double.parseDouble(token);
                        if (noiseLevel < 0.0 || noiseLevel > 1.0) {
                            System.out.println("Select a number between 0 and 1");
                        }
                    } catch (NumberFormatException nfe) {
                        if (token.isEmpty()) {
                            noiseLevel = noiseLevelValue;
                            System.out.println("Value selected: " + noiseLevel);
                        } else {
                            System.out.println("Select a number between 0.0 and 1.0");
                        }
                    }
                }
                result.setNoiseLevel(noiseLevel);
            }
        }
    }

    public NoiseFilterResult getSelections() {
        return result;
    }

}
