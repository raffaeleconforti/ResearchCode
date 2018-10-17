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

import com.raffaeleconforti.bpmnminer.preprocessing.functionaldependencies.DiscoverERmodel;
import com.raffaeleconforti.foreignkeydiscovery.conceptualmodels.Attribute;

import java.util.List;
import java.util.Scanner;

public class DiscoverERmodel_UI_foreignKey {

    private boolean[] selectedForeignKeys;

    public DiscoverERmodel_UI_foreignKey(List<DiscoverERmodel.ForeignKeyData> data) {

        selectedForeignKeys = new boolean[data.size()];
        for(int i = 0; i < selectedForeignKeys.length; i++) {
            selectedForeignKeys[i] = true;
        }

        String token = null;
        Scanner console = new Scanner(System.in);

        int fkNumIndex = 0;
        for (DiscoverERmodel.ForeignKeyData currentData : data) {
            System.out.println("Select " + currentData.e1.getName() + "->" + currentData.e2.getName() + " Foreign Key-Primary Key Relations? (y/n)");
            System.out.println(getKeyString(currentData.e1_foreignKey) + " is a foreign key to " + getKeyString(currentData.e2_primaryKey));

            token = null;
            while(token == null) {
                token = console.nextLine();
                if(!token.isEmpty()) {
                    if(token.equalsIgnoreCase("y")) {
                        selectedForeignKeys[fkNumIndex] = true;
                    }else if(token.equalsIgnoreCase("n")) {
                        selectedForeignKeys[fkNumIndex] = false;
                    }else {
                        token = null;
                        System.out.println("Accepted parameter Y or N");
                    }
                }
            }

            fkNumIndex++;
        }

    }

    private static String getKeyString(List<Attribute> key) {
        StringBuilder result = new StringBuilder("(");
        for (int i = 0; i < key.size(); i++) {
            if (i > 0) result.append(", ");
            result.append(key.get(i).getName());
        }
        result.append(")");
        return result.toString();
    }

    public boolean[] getSelection() {
        return selectedForeignKeys;
    }

}
