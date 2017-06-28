package com.raffaeleconforti.bpmnminer.commandline.subprocessminer.ui;

import com.raffaeleconforti.bpmnminer.subprocessminer.selection.SelectMinerResult;

import java.util.List;
import java.util.Scanner;

/**
 * Created by Raffaele Conforti on 20/02/14.
 */
public class SelectMiner {

	private Integer selectedAlgorithm;
    private Double interruptingEventTolerance;
    private Double multiInstancePercentage;
    private Double multiInstanceTolerance;
    private Double timerEventPercentage;
    private Double timerEventTolerance;
	private Double noiseThreshold;
    SelectMinerResult result = null;
    Scanner console = new Scanner(System.in);

    public SelectMiner(List<String> attributeNames) {

        double initialEventTolerance = 0.0;
        double initialMultiinstancePercentage = 0.0;
        double initialMultiinstanceTolerance = 0.5;
        double initialTimerPercentage = 0.0;
        double initialTimerTolerance = 0.0;
		double initialNoiseThreshold = 0.3;

        result = new SelectMinerResult(0, null, initialEventTolerance, initialMultiinstancePercentage, initialMultiinstanceTolerance, initialTimerPercentage, initialTimerTolerance, initialNoiseThreshold);

		String token = null;

		while(selectedAlgorithm == null || selectedAlgorithm < 0 || selectedAlgorithm >= attributeNames.size()) {
			System.out.println("Select Mining Algorithm");
			for (int i = 0; i < attributeNames.size(); i++) {
				System.out.println((i+1) + ") " + attributeNames.get(i));
			}
			token = console.nextLine();
			try {
				selectedAlgorithm = Integer.parseInt(token)-1;
				if(selectedAlgorithm < 0 || selectedAlgorithm >= attributeNames.size()) {
					System.out.println("Select a number between 1 and " + attributeNames.size());
				}
			}catch (NumberFormatException nfe) {
				System.out.println("Select a number between 1 and " + attributeNames.size());
			}
		}

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
			while (interruptingEventTolerance == null || interruptingEventTolerance < 0.0 || interruptingEventTolerance > 1.0) {
				System.out.println("Select InterruptingEvent Tolerance Value - Click Enter for default value");
				try {
					token = console.nextLine();
					interruptingEventTolerance = Double.parseDouble(token);
					if (interruptingEventTolerance < 0.0 || interruptingEventTolerance > 1.0) {
						System.out.println("Select a number between 1 and " + attributeNames.size());
					}
				} catch (NumberFormatException nfe) {
					if (token.isEmpty()) {
						interruptingEventTolerance = initialEventTolerance;
						System.out.println("Value selected: " + initialEventTolerance);
					} else {
						System.out.println("Select a number between 0.0 and 1.0");
					}
				}
			}

			while (multiInstancePercentage == null || multiInstancePercentage < 0.0 || multiInstancePercentage > 1.0) {
				System.out.println("Select multiInstance Percentage Value - Click Enter for default value");
				try {
					token = console.nextLine();
					multiInstancePercentage = Double.parseDouble(token);
					if (multiInstancePercentage < 0.0 || multiInstancePercentage > 1.0) {
						System.out.println("Select a number between 1 and " + attributeNames.size());
					}
				} catch (NumberFormatException nfe) {
					if (token.isEmpty()) {
						multiInstancePercentage = initialMultiinstancePercentage;
						System.out.println("Value selected: " + multiInstancePercentage);
					} else {
						System.out.println("Select a number between 0.0 and 1.0");
					}
				}
			}

			while (multiInstanceTolerance == null || multiInstanceTolerance < 0.0 || multiInstanceTolerance > 1.0) {
				System.out.println("Select multiInstance Tolerance Value - Click Enter for default value");
				try {
					token = console.nextLine();
					multiInstanceTolerance = Double.parseDouble(token);
					if (multiInstanceTolerance < 0.0 || multiInstanceTolerance > 1.0) {
						System.out.println("Select a number between 1 and " + attributeNames.size());
					}
				} catch (NumberFormatException nfe) {
					if (token.isEmpty()) {
						multiInstanceTolerance = initialMultiinstanceTolerance;
						System.out.println("Value selected: " + multiInstanceTolerance);
					} else {
						System.out.println("Select a number between 0.0 and 1.0");
					}
				}
			}

			while (timerEventPercentage == null || timerEventPercentage < 0.0 || timerEventPercentage > 1.0) {
				System.out.println("Select timerEvent Percentage Value - Click Enter for default value");
				try {
					token = console.nextLine();
					timerEventPercentage = Double.parseDouble(token);
					if (timerEventPercentage < 0.0 || timerEventPercentage > 1.0) {
						System.out.println("Select a number between 1 and " + attributeNames.size());
					}
				} catch (NumberFormatException nfe) {
					if (token.isEmpty()) {
						timerEventPercentage = initialTimerPercentage;
						System.out.println("Value selected: " + timerEventPercentage);
					} else {
						System.out.println("Select a number between 0.0 and 1.0");
					}
				}
			}

			while (timerEventTolerance == null || timerEventTolerance < 0.0 || timerEventTolerance > 1.0) {
				System.out.println("Select timerEvent Tolerance Value - Click Enter for default value");
				try {
					token = console.nextLine();
					timerEventTolerance = Double.parseDouble(token);
					if (timerEventTolerance < 0.0 || timerEventTolerance > 1.0) {
						System.out.println("Select a number between 1 and " + attributeNames.size());
					}
				} catch (NumberFormatException nfe) {
					if (token.isEmpty()) {
						timerEventTolerance = initialEventTolerance;
						System.out.println("Value selected: " + timerEventTolerance);
					} else {
						System.out.println("Select a number between 0.0 and 1.0");
					}
				}
			}

			while (noiseThreshold == null || noiseThreshold < 0.0 || noiseThreshold > 1.0) {
				System.out.println("Select Noise Threshold Value - Click Enter for default value");
				try {
					token = console.nextLine();
					noiseThreshold = Double.parseDouble(token);
					if (noiseThreshold < 0.0 || noiseThreshold > 1.0) {
						System.out.println("Select a number between 1 and " + attributeNames.size());
					}
				} catch (NumberFormatException nfe) {
					if (token.isEmpty()) {
						noiseThreshold = initialNoiseThreshold;
						System.out.println("Value selected: " + noiseThreshold);
					} else {
						System.out.println("Select a number between 0.0 and 1.0");
					}
				}
			}
		}else {
			interruptingEventTolerance = initialEventTolerance;
			multiInstancePercentage = initialMultiinstancePercentage;
			multiInstanceTolerance = initialMultiinstanceTolerance;
			timerEventPercentage = initialTimerPercentage;
			timerEventTolerance = initialEventTolerance;
			noiseThreshold = initialNoiseThreshold;
		}


    }

    public SelectMinerResult getSelectedAlgorithm() {
        result = new SelectMinerResult(selectedAlgorithm, null, interruptingEventTolerance, multiInstancePercentage, multiInstanceTolerance, timerEventPercentage, timerEventTolerance, noiseThreshold);
        return result;
    }

	public SelectMinerResult getSelections() {
        return result;
    }

}
