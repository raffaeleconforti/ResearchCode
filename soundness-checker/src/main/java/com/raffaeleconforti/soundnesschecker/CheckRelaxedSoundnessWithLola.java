package com.raffaeleconforti.soundnesschecker;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 5/7/17.
 */

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.raffaeleconforti.context.FakePluginContext;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.reduction.Murata;
import org.processmining.plugins.petrinet.reduction.MurataParameters;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CheckRelaxedSoundnessWithLola {
    public static void main(String... args) throws Exception {

        AcceptingPetriNet net = AcceptingPetriNetFactory.createAcceptingPetriNet();
        net.importFromStream(
                new FakePluginContext(),
                new FileInputStream(
                        new File(
                                "D:\\svn\\00 - the beast\\experiments\\logQuality\\discoveredModels\\Receipt phase WABO CoSeLoG project.xes.gz-discovery11.xes.gz-alpha.pnml")));

        System.out.println(isRelaxedSoundAndBounded(net));
    }

    /**
     * Very sorry, this method requires a Cygwin + Lola installation. I'm afraid
     * you'll have to compile it yourself.
     *
     * @param net
     * @return
     * @throws IOException
     * @throws ConnectionCannotBeObtained
     * @throws JSONException
     */
    public static boolean isRelaxedSoundAndBounded(AcceptingPetriNet net) throws IOException,
            ConnectionCannotBeObtained, JSONException {
        AcceptingPetriNet reducedNet = reduceWorkflowNet(net);
        String lolaPetriNet = PetriNet2Lola.convert(reducedNet.getNet(), reducedNet.getInitialMarking());

        for (Place place : reducedNet.getNet().getPlaces()) {
            String lolaBoundedNessFormula = getPlaceIsBoundedFormula(reducedNet, place);
            if (!callLola(lolaPetriNet, lolaBoundedNessFormula, "--encoder=full --search=cover ")) {
                return false;
            }
        }

        String lolaFinalMarkingReachableFormula = getFinalMarkingReachableFormula(reducedNet);
        return callLola(lolaPetriNet, lolaFinalMarkingReachableFormula, "");
    }

    public static boolean callLola(String lolaPetriNet, String lolaFormula, String commandLineOption)
            throws IOException, JSONException {
        ProcessBuilder pb = new ProcessBuilder().command("/bin/bash", "-l", "-i", "-c", "/Volumes/Data/IdeaProjects/lola --json " + commandLineOption + "--markinglimit=100000000 --threads=12 --formula='" + lolaFormula + "'");

        pb.redirectErrorStream(true);

        Process process = null;
        process = pb.start();
        OutputStream os = process.getOutputStream();
        BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        pb.redirectInput();
        out2.write(lolaPetriNet);
        out2.flush();
        out2.close();

        InputStream stdout = new BufferedInputStream(process.getInputStream());

        Scanner scanner = new Scanner(stdout);
        try {
            while (scanner.hasNextLine()) {
                String result = scanner.nextLine();
//                System.out.println(result);

                if (result.startsWith("{")) {
                    JSONObject json = new JSONObject(result);
//                    System.out.println(json);

                    JSONObject array = json.getJSONObject("analysis");
                    boolean result2 = array.getBoolean("result");
                    return result2;
                }
            }
        } finally {
            scanner.close();
        }
        throw new IOException("failed to call Lola");
    }

    /**
     * Reduces the given accepting petri net. Notice that the intitial and final
     * markings are reconstructed, e.g. in the reduced net, all places without
     * incoming arcs are initial markings.
     *
     * @param net
     * @return
     * @throws ConnectionCannotBeObtained
     */
    public static AcceptingPetriNet reduceWorkflowNet(AcceptingPetriNet net) throws ConnectionCannotBeObtained {
        Petrinet reducedNet = new Murata().runWF(null, net.getNet(), new MurataParameters());

        Marking initialMarking = new Marking();
        for (Place p : reducedNet.getPlaces()) {
            if (reducedNet.getInEdges(p).isEmpty()) {
                initialMarking.add(p);
            }
        }

        Marking finalMarking = new Marking();
        for (Place p : reducedNet.getPlaces()) {
            if (reducedNet.getOutEdges(p).isEmpty()) {
                finalMarking.add(p);
            }
        }

        return AcceptingPetriNetFactory.createAcceptingPetriNet(reducedNet, initialMarking, finalMarking);
    }

    public static String getFinalMarkingReachableFormula(AcceptingPetriNet net) {

        List<String> finalMarkingsFormulae = new ArrayList<>();
        for (final Marking finalMarking : net.getFinalMarkings()) {
            finalMarkingsFormulae.add(StringUtils.join(
                    FluentIterable.from(net.getNet().getPlaces()).transform(new Function<Place, String>() {
                        public String apply(Place place) {
                            if (finalMarking.contains(place)) {
                                return PetriNet2Lola.removeNode(place.getId()) + " = "
                                        + finalMarking.occurrences(place);
                            } else {
                                return PetriNet2Lola.removeNode(place.getId()) + " = 0";
                            }
                        }
                    }), " AND "));
        }

        return "EF (" + StringUtils.join(finalMarkingsFormulae, ") OR (") + ")";

    }

    public static String getPlaceIsBoundedFormula(AcceptingPetriNet net, Place place) {
        return "AG (" + PetriNet2Lola.removeNode(place.getId()) + " < oo)";
    }
}
