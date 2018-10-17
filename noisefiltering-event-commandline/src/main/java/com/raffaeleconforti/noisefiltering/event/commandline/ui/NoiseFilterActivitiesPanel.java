/*
 *  Copyright (C) 2018 Raffaele Conforti (www.raffaeleconforti.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.raffaeleconforti.noisefiltering.event.commandline.ui;

import com.raffaeleconforti.automaton.Node;
import com.raffaeleconforti.noisefiltering.event.selection.NoiseFilterResult;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by conforti on 17/09/15.
 */
public class NoiseFilterActivitiesPanel {

    private final NoiseFilterResult result;
    private final Node<String>[] possibleStates;
    private final Scanner console = new Scanner(System.in);

    public NoiseFilterActivitiesPanel(Set<Node<String>> states) {

        result = new NoiseFilterResult();
        result.setRequiredStates(states);

        Set<Node<String>> selectedStates = new UnifiedSet<Node<String>>();

        int i = 0;
        possibleStates = states.toArray(new Node[states.size()]);
        Arrays.sort(possibleStates, new Comparator<Node<String>>() {
            @Override
            public int compare(Node<String> o1, Node<String> o2) {
                int val = Double.valueOf(o1.getFrequency()).compareTo(o2.getFrequency());
                if(val == 0) val = o1.getData().compareTo(o2.getData());
                return -val;
            }
        });

        double total = 0.0;
        DecimalFormat decimalFormat = new DecimalFormat("##0.000");
        for(Node<String> state : possibleStates) {
            if(!state.getData().equals("ArtificialStartEvent") && !state.getData().equals("ArtificialEndEvent")) {
                total += state.getFrequency();
            }
        }

        String token = null;
        System.out.println("Do you want to select all states? (y/n)");
        boolean selectAll = true;
        while(token == null) {
            token = console.nextLine();
            if(!token.isEmpty()) {
                if(token.equalsIgnoreCase("y")) {
                    selectAll = true;
                }else if(token.equalsIgnoreCase("n")) {
                    selectAll = false;
                }else {
                    token = null;
                    System.out.println("Accepted parameter Y or N");
                }
            }else {
                token = null;
                System.out.println("Accepted parameter Y or N");
            }
        }

        if(!selectAll) {
            for (Node<String> state : possibleStates) {
                if (state.getData().equals("ArtificialStartEvent")) {
                    selectedStates.add(state);
                } else if (state.getData().equals("ArtificialEndEvent")) {
                    selectedStates.add(state);
                } else {
                    Integer selected = null;
                    while (selected == null || (selected != 0.0 && selected != 1.0)) {
                        System.out.println("Do you want to maintain the following state? " + state.getData() + ", frequency: " + decimalFormat.format(state.getFrequency() / total) + "% (0 = true, 1 = false)");
                        try {
                            token = console.nextLine();
                            selected = Integer.parseInt(token);
                            if (selected != 0.0 && selected != 1.0) {
                                System.out.println("Select a 0 for true and 1 false");
                            } else {
                                selectedStates.add(possibleStates[i]);
                                i++;
                            }
                        } catch (NumberFormatException nfe) {
                            if (token.isEmpty()) {
                                selectedStates.add(possibleStates[i]);
                                i++;
                            } else {
                                System.out.println("Select a 0 for true and 1 false");
                            }
                        }
                    }
                }
            }
        }else {
            for (i = 0; i < possibleStates.length; i++) {
                selectedStates.add(possibleStates[i]);
            }
        }
        result.setRequiredStates(selectedStates);
    }

    public NoiseFilterResult getSelections() {
        return result;
    }
}
