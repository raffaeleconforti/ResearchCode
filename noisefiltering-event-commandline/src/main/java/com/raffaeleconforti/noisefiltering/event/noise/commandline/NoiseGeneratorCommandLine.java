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

package com.raffaeleconforti.noisefiltering.event.noise.commandline;

import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.noisefiltering.event.noise.NoiseGenerator;
import com.raffaeleconforti.noisefiltering.event.noise.commandline.ui.NoiseUI;
import com.raffaeleconforti.noisefiltering.event.noise.selection.NoiseResult;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;

import java.util.Scanner;

/**
 * Created by conforti on 26/02/15.
 */
public class NoiseGeneratorCommandLine {

    public static void main(String[] args) throws Exception {
        Scanner console = new Scanner(System.in);
        System.out.println("Input file:");
        String name = console.nextLine();
        XFactory factory = new XFactoryNaiveImpl();
        XLog log = LogImporter.importFromFile(factory, name);

        NoiseGeneratorCommandLine ngcl = new NoiseGeneratorCommandLine();
        XLog filteredlog = ngcl.generateNoise(log);

        System.out.println("Output file: ");
        String path = console.next();

        LogImporter.exportToFile(path.substring(0, path.lastIndexOf("/")) + 1, path.substring(path.lastIndexOf("/") + 1, path.length()), filteredlog);

    }

    public XLog generateNoise(XLog log) {
        NoiseGenerator noiseGenerator = new NoiseGenerator(log);
        NoiseUI noiseUI = new NoiseUI();
        NoiseResult result = noiseUI.showGUI();
        return noiseGenerator.insertNoise(result.getNoiseLevel());
    }
}
