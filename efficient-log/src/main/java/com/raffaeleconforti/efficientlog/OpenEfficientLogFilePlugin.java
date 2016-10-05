package com.raffaeleconforti.efficientlog;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.plugins.log.OpenLogFilePlugin;

import java.io.InputStream;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/08/2016.
 */
@Plugin(name = "Open XES Log File (Efficient)", level = PluginLevel.PeerReviewed, parameterLabels = { "Filename" }, returnLabels = { "Log (single process)" }, returnTypes = { XLog.class })
@UIImportPlugin(description = "ProM log files (Efficient)", extensions = { "mxml", "xml", "gz", "zip", "xes", "xez" })
public class OpenEfficientLogFilePlugin extends OpenLogFilePlugin {
    protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
            throws Exception {
        return importFromStream(context, input, filename, fileSizeInBytes, new XFactoryEfficientImpl());
    }
}
