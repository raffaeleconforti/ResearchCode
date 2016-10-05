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
