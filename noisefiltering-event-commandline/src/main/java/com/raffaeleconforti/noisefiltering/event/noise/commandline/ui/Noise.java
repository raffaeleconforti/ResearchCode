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
