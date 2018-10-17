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

package com.raffaeleconforti.bpmnminer.commandline.preprocessing.synchtracegeneration.ui;

import com.raffaeleconforti.foreignkeydiscovery.conceptualmodels.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TraceGeneration_UI_subprocesses {

    private List<Entity> data;
    private int chosenArtifactsIndex[];

	public TraceGeneration_UI_subprocesses(List<Entity> data, int chosenArtifactsIndex[]) {
		this.data = data;
		this.chosenArtifactsIndex = chosenArtifactsIndex;
	}

    public TraceGeneration_UI_subprocesses(List<Entity> data2, List<Entity> data, boolean all) {
        this.data = data;

        chosenArtifactsIndex = new int[data.size()];
        if(!all) {
            String token = null;
            Scanner console = new Scanner(System.in);

            System.out.println("Select Subprocesses: ");

            // create a new property item for each event type
            int i = 0;
            for (Entity currentData : data) {
                token = null;
                System.out.println("Select subprocess " + currentData.getName() + "? (y/n)");
                while (token == null) {
                    token = console.nextLine();
                    if (!token.isEmpty()) {
                        if (token.equalsIgnoreCase("y")) {
                            chosenArtifactsIndex[i] = 0;
                            i++;
                        } else if (token.equalsIgnoreCase("n")) {
                            chosenArtifactsIndex[i] = 1;
                            i++;
                        } else {
                            token = null;
                            System.out.println("Accepted parameter Y or N");
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
        for (int dataIndex = 0; dataIndex < data.size(); dataIndex++) {
            if (chosenArtifactsIndex[dataIndex] == 0)
                selected.add(data.get(dataIndex));
        }
        return selected;
    }

}
