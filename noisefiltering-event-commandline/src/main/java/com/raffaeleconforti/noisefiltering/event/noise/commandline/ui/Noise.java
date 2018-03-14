package com.raffaeleconforti.noisefiltering.event.noise.commandline.ui;


import com.raffaeleconforti.noisefiltering.event.noise.selection.NoiseResult;

import java.util.Scanner;

/**
 * Created by conforti on 26/02/15.
 */
public class Noise {

    final NoiseResult result;

    public Noise() {

        result = new NoiseResult();

        Scanner scanner = new Scanner(System.in);
        Double noise = null;
        while(noise == null || noise < 0.0 || noise > 1.0) {
            System.out.println("Select amount of noise between 0 and 1");
            String token = scanner.nextLine();
            try {
                token = scanner.nextLine();
                noise = Double.parseDouble(token);
                if (noise < 0.0 || noise > 1.0) {
                    System.out.println("Select a number between 0 and 1");
                }
            } catch (NumberFormatException nfe) {
                if (token.isEmpty()) {
                    noise = 0.05;
                    System.out.println("Value selected: " + noise);
                } else {
                    System.out.println("Select a number between 0.0 and 1.0");
                }
            }
        }
        result.setNoiseLevel(noise);

    }

    public NoiseResult getSelections() {
        return result;
    }

}
