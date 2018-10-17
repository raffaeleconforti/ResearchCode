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

package com.raffaeleconforti.memorylog;

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
@Plugin(name = "Open XES Log File (Memory)", level = PluginLevel.PeerReviewed, parameterLabels = { "Filename" }, returnLabels = { "Log (single process)" }, returnTypes = { XLog.class })
@UIImportPlugin(description = "ProM log files (Memory)", extensions = { "mxml", "xml", "gz", "zip", "xes", "xez" })
public class OpenMemoryLogFilePlugin extends OpenLogFilePlugin {
    protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
            throws Exception {
        return importFromStream(context, input, filename, fileSizeInBytes, new XFactoryMemoryImpl());
    }
}
