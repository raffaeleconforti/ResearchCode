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
