package com.raffaeleconforti.bpmnminer.commandline.preprocessing.synchtracegeneration.ui;

import com.raffaeleconforti.foreignkeydiscovery.conceptualmodels.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TraceGeneration_UI_other {

    private List<Entity> other;
    private List<Entity> artifacts;
    private int chosenArtifactsIndex[];

    public TraceGeneration_UI_other(List<Entity> other, List<Entity> artifacts, boolean all) {
        this.other = other;
        this.artifacts = artifacts;
        chosenArtifactsIndex = new int[other.size()];

        String[] keyList = new String[artifacts.size()];
        // create a new property item for each event type
        for (int i = 0; i < artifacts.size(); i++) {
            // build a list of all artifacts (sets of attribute names)
            keyList[i] = artifacts.get(i).getName();
        }

        if(!all) {
            Scanner console = new Scanner(System.in);
            Integer value = null;

            for (int i = 0; i < other.size(); i++) {
                if (artifacts.size() > 0) {
                    System.out.println("Select subprocess " + other.get(i).getName() + "?");
                    for (int j = 0; j < artifacts.size(); j++) {
                        System.out.println((j + 1) + ") " + artifacts.get(j).getName());
                    }

                    while (value == null || value < 0 || value >= artifacts.size()) {
                        try {
                            value = Integer.parseInt(console.nextLine()) - 1;
                            if (value < 0 || value >= artifacts.size()) {
                                System.out.println("Select a number between 1 and " + artifacts.size());
                            } else {
                                chosenArtifactsIndex[i] = value;
                                i++;
                            }
                        } catch (NumberFormatException nfe) {
                            System.out.println("Select a number between 1 and " + artifacts.size());
                        }
                    }
                }
            }
        }
    }

    /**
     * @param set
     * @return string to render list of attributes in a tooltip
     */
    protected static String attributesToToolTipString(String set[]) {
        StringBuilder keyString = new StringBuilder("(");
        for (int i = 0; i < set.length; i++) {
            keyString.append(set[i]).append(i == set.length - 1 ? ") " : ",\n");
        }
        return keyString.toString();
    }

    public List<Entity> getSelection() {
        List<Entity> selected = new ArrayList<Entity>();
        for (int dataIndex = 0; dataIndex < other.size(); dataIndex++) {
            if (chosenArtifactsIndex[dataIndex] == 0)
                selected.add(dataIndex, artifacts.get(chosenArtifactsIndex[dataIndex]));
        }
        return selected;
    }

}
