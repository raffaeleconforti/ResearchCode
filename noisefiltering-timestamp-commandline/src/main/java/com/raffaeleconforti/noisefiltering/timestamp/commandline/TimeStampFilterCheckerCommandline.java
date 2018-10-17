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

package com.raffaeleconforti.noisefiltering.timestamp.commandline;

import com.raffaeleconforti.log.util.LogImporter;
import com.raffaeleconforti.noisefiltering.timestamp.TimeStampFilterChecker;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;

import java.util.Scanner;

/**
 * Created by conforti on 7/02/15.
 */

public class TimeStampFilterCheckerCommandline {

    public static void main(String[] args) throws Exception {
        Scanner console = new Scanner(System.in);
        XFactory factory = new XFactoryNaiveImpl();

        System.out.println("Input filtered log:");
        String name = console.nextLine();
        XLog filteredLog = LogImporter.importFromFile(factory, name);

        System.out.println("Input noisy log:");
        name = console.nextLine();
        XLog noisyLog = LogImporter.importFromFile(factory, name);

        System.out.println("Input correct log:");
        name = console.nextLine();
        XLog correctLog = LogImporter.importFromFile(factory, name);

        TimeStampFilterChecker timeStampFilterChecker = new TimeStampFilterChecker();
        String info = timeStampFilterChecker.check(filteredLog, noisyLog, correctLog);

        System.out.println(info);
    }

}
