package com.raffaeleconforti.wrappers;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.petrinet.PetriNetVisualization;

import javax.swing.*;

/**
 * Created by conforti on 23/02/15.
 */
@Plugin(name = "Visualize PetrinetWithMarking", returnLabels = {"Visualized PetrinetWithMarking"}, returnTypes = { JComponent.class }, parameterLabels = {"PetrinetWithMarking" }, userAccessible = true)
@Visualizer
public class PetrinetWithMarkingVisualizer {

    @PluginVariant(requiredParameterLabels = { 0 })
    public JComponent visualize(PluginContext context, PetrinetWithMarking petrinetWithMarking) {
        PetriNetVisualization petriNetVisualization = new PetriNetVisualization();
        return petriNetVisualization.visualize(context, petrinetWithMarking.getPetrinet(), petrinetWithMarking.getInitialMarking());
    }

}
