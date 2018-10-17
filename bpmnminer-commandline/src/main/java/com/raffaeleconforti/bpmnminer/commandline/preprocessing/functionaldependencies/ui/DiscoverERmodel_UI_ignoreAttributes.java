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

package com.raffaeleconforti.bpmnminer.commandline.preprocessing.functionaldependencies.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DiscoverERmodel_UI_ignoreAttributes {

    private boolean[] selectedAttributes;
    private String[] attributes;

    public DiscoverERmodel_UI_ignoreAttributes(List<String> attributeNames) {

        selectedAttributes = new boolean[attributeNames.size()];
        attributes = new String[attributeNames.size()];

        String token = null;
        Scanner console = new Scanner(System.in);

        int fkNumIndex = 0;
        for (String attribute : attributeNames) {

            selectedAttributes[fkNumIndex] = true;
            attributes[fkNumIndex] = attribute;

            token = null;
            while(token == null) {
                System.out.println("Consider attribute " + attribute + " as a potential primary key? (y/n)");
                token = console.nextLine();
                if(!token.isEmpty()) {
                    if(token.equalsIgnoreCase("y")) {
                        selectedAttributes[fkNumIndex] = true;
                    }else if(token.equalsIgnoreCase("n")) {
                        selectedAttributes[fkNumIndex] = false;
                    }else {
                        token = null;
                        System.out.println("Accepted parameter Y or N");
                    }
                }else {
                    token = null;
                }
            }

            fkNumIndex++;
        }
    }

    public List<String> getIgnoreAttributes() {
        List<String> ignored = new ArrayList<String>();
        for (int i = 0; i < selectedAttributes.length; i++) {
            if (!selectedAttributes[i]) ignored.add(attributes[i]);
        }
        return ignored;
    }

}
