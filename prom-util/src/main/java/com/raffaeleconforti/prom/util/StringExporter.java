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

package com.raffaeleconforti.prom.util;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by conforti on 23/02/15.
 */
@Plugin(name = "Export String", returnLabels = {}, returnTypes = {}, parameterLabels = { "String", "File" }, userAccessible = true)
@UIExportPlugin(description = "String file", extension = "txt")
public class StringExporter {

    @PluginVariant(variantLabel = "Export String", requiredParameterLabels = { 0, 1 })
    public void exportString(PluginContext context, String s, File file) throws IOException {
        s = s.replace("<html><table width=\"400\"><tr><td width=\"33%\"></td><td width=\"33%\"><table>", "");
        s = s.replace("</table></td><td width=\"33%\"></td></tr></table></html>", "");
        s = s.replace("<tr>", "");
        s = s.replace("<td>", "");
        s = s.replace("</td>", "\t");
        s = s.replace("</tr>", "\n");
        s = s.replace("<html><p>", "");
        s = s.replace("<br>", "\n");
        s = s.replace("</p></html>", "");
        FileWriter fw = new FileWriter(file);
        fw.write(s);
        fw.close();
    }

}
