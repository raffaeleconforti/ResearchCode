package com.raffaeleconforti.noisefiltering.timestamp.permutation.permutators;

import com.raffaeleconforti.kernelestimation.distribution.EventDistributionCalculator;
import com.raffaeleconforti.noisefiltering.timestamp.permutation.PermutationTechnique;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XEvent;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

import java.util.*;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/07/2016.
 */
public class HeuristicSetSolutions implements PermutationTechnique {

    private EventDistributionCalculator eventDistributionCalculator;
    private XEvent[] eventsArray;
    private double[][] likeloods;

    private Map<String, IntHashSet> skipMap = new UnifiedMap<>();
    private Map<String, Boolean> existsPathRemaining = new UnifiedMap<>();

    public HeuristicSetSolutions(Set<XEvent> events, EventDistributionCalculator eventDistributionCalculator, XEvent start, XEvent end) {
        this.eventDistributionCalculator = eventDistributionCalculator;
        this.eventsArray = events.toArray(new XEvent[events.size() + 2]);
        Arrays.sort(eventsArray, new Comparator<XEvent>() {
            XEventClassifier xEventClassifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
            @Override
            public int compare(XEvent o1, XEvent o2) {
                if(o1 != null && o2 != null) return xEventClassifier.getClassIdentity(o1).compareTo(xEventClassifier.getClassIdentity(o2));
                else if(o1 == null) return 1;
                else return -1;
            }
        });
        this.eventsArray[eventsArray.length - 2] = start;
        this.eventsArray[eventsArray.length - 1] = end;
        this.likeloods = new double[events.size() + 2][events.size() + 2];
        computeLikelihoods();
    }

    private void computeLikelihoods() {
        List<XEvent> list = new ArrayList<>(2);
        for(int i = 0; i < eventsArray.length; i++) {
            for(int j = 0; j < eventsArray.length; j++) {
                if(i != j) {
                    list.clear();
                    list.add(0, eventsArray[i]);
                    list.add(1, eventsArray[j]);
                    likeloods[i][j] = eventDistributionCalculator.computeLikelihood(list);
                }else {
                    likeloods[i][j] = 0;
                }
            }
        }
    }

    public Set<List<XEvent>> findBestStartEnd() {
        int removed = 0;
        Set<List<XEvent>> set = new UnifiedSet<>();

        List<XEvent> listEvent = new ArrayList<>();
        IntArrayList listPos = new IntArrayList();

        IntHashSet remaining = new IntHashSet();
        IntHashSet skip;

        int pos = eventsArray.length - 2;
        double likelihood;

        for (int j = 0; j < eventsArray.length - 2; j++) {
            remaining.add(j);
        }

        Set<String> keyset;
        while (true){

            if(skipMap.size() + set.size() + removed > 1000000) {
                System.out.println("Limit exceeded!");
                return (set.size() > 0) ? set : findBestStartEndWithZero();
            }
            likelihood = 0;

            listEvent.add(eventsArray[pos]);
            listPos.add(pos);
            remaining.remove(pos);

            skip = getSkipSet(listPos.toString());

            int tmpPos = -1;
            if(existsPathAmongRemaining(remaining)) {
                for (int j : remaining.toArray()) {
                    if (!skip.contains(j) && likelihood < likeloods[pos][j]) {
                        likelihood = likeloods[pos][j];
                        tmpPos = j;
                    }
                }
            }

            if (tmpPos > -1) {
                pos = tmpPos;
            }else if (remainingAndNotFound(tmpPos, remaining)) {
                if (listEvent.size() > 1) {
                    keyset = a(pos, listPos, listEvent, remaining);

                    for(String key : keyset) {
                        if(skipMap.remove(key) != null) removed++;
                    }

                    pos = b(listPos, listEvent, remaining);
                } else {
                    break;
                }
            }else if (!remainingAndNotFound(tmpPos, remaining)) {
                if (!skip.contains(eventsArray.length - 1) && likeloods[pos][eventsArray.length - 1] > 0) {
                    skip = getSkipSet(listPos.toString());
                    skip.add(eventsArray.length - 1);
                    List<XEvent> clone = new ArrayList<>(listEvent.size() - 1);
                    clone.addAll(listEvent.subList(1, listEvent.size()));
                    set.add(clone);
                }

                keyset = a(pos, listPos, listEvent, remaining);

                for(String key : keyset) {
                    if(skipMap.remove(key) != null) removed++;
                }

                pos = b(listPos, listEvent, remaining);
            }
        }

        return (set.size() > 0) ? set : findBestStartEndWithZero();
    }

    private Set<List<XEvent>> findBestStartEndWithZero() {
        skipMap.clear();

        int removed = 0;
        Set<List<XEvent>> set = new UnifiedSet<>();

        List<XEvent> listEvent = new ArrayList<>();
        IntArrayList listPos = new IntArrayList();

        IntHashSet remaining = new IntHashSet();
        IntHashSet skip;

        int pos = eventsArray.length - 2;
        double likelihood;

        for (int j = 0; j < eventsArray.length - 2; j++) {
            remaining.add(j);
        }

        Set<String> keyset;
        while (true){

            if(skipMap.size() + set.size() + removed > 1000000) {
                System.out.println("Limit exceeded!");
                return set;
            }
            likelihood = -1;

            listEvent.add(eventsArray[pos]);
            listPos.add(pos);
            remaining.remove(pos);

            skip = getSkipSet(listPos.toString());

            int tmpPos = -1;
            for (int j : remaining.toArray()) {
                if (!skip.contains(j) && likelihood < likeloods[pos][j]) {
                    likelihood = likeloods[pos][j];
                    tmpPos = j;
                }
            }

            if (tmpPos > -1) {
                pos = tmpPos;
            }else if (remainingAndNotFound(tmpPos, remaining)) {
                if (listEvent.size() > 1) {
                    keyset = a(pos, listPos, listEvent, remaining);

                    for(String key : keyset) {
                        if(skipMap.remove(key) != null) removed++;
                    }

                    pos = b(listPos, listEvent, remaining);
                } else {
                    break;
                }
            }else if (!remainingAndNotFound(tmpPos, remaining)) {
                if (!skip.contains(eventsArray.length - 1)) {
                    skip = getSkipSet(listPos.toString());
                    skip.add(eventsArray.length - 1);
                    List<XEvent> clone = new ArrayList<>(listEvent.size() - 1);
                    clone.addAll(listEvent.subList(1, listEvent.size()));
                    set.add(clone);
                }

                keyset = a(pos, listPos, listEvent, remaining);

                for(String key : keyset) {
                    if(skipMap.remove(key) != null) removed++;
                }

                pos = b(listPos, listEvent, remaining);
            }
        }

        System.out.println("findBestStartEndWithZero " + set.size());
        return set;
    }

    private boolean existsPathAmongRemaining(IntHashSet remaining) {
        int[] array = remaining.toArray();
        Arrays.sort(array);
        String remainingString = array.toString();
        Boolean exists;
        if((exists = existsPathRemaining.get(remainingString)) == null) {
            int zeros = 0;
            for (int i : remaining.toArray()) {
                double likelihood = 0;
                for (int j : remaining.toArray()) {
                    if (likeloods[i][j] > 0) {
                        likelihood = likeloods[i][j];
                        break;
                    }
                }
                if (likelihood == 0) {
                    zeros++;
                    if(zeros > 1) {
                        existsPathRemaining.put(remainingString, false);
                        return false;
                    }
                }
            }
            existsPathRemaining.put(remainingString, true);
            return true;
        }
        return exists;
    }

    private boolean remainingAndNotFound(int tmpPos, IntHashSet remaining) {
        return tmpPos == -1 && remaining.size() > 0;
    }

    private Set<String> a(int pos, IntArrayList listPos, List<XEvent> listEvent, IntHashSet remaining) {
        Set<String> keyset = getPossibleFollowingCombinations(listPos.toString());

        listPos.removeAtIndex(listPos.size() - 1);
        listEvent.remove(listEvent.size() - 1);
        remaining.add(pos);

        IntHashSet skip = getSkipSet(listPos.toString());
        skip.add(pos);

        return keyset;
    }

    private int b(IntArrayList listPos, List<XEvent> listEvent, IntHashSet remaining) {
        int pos = listPos.removeAtIndex(listPos.size() - 1);
        listEvent.remove(listEvent.size() - 1);
        remaining.add(pos);
        return pos;
    }

    private IntHashSet getSkipSet(String listPosToString) {
        IntHashSet skip;
        if ((skip = skipMap.get(listPosToString)) == null) {
            skip = new IntHashSet();
            skipMap.put(listPosToString, skip);
        }
        return skip;
    }

    private Set<String> getPossibleFollowingCombinations(String listPostToString) {
        Set<String> set = new UnifiedSet<>();
        String base = listPostToString.substring(0, listPostToString.length() - 1);
        String comma = ", ";
        String bracket = "]";
        for(int i = 0; i < eventsArray.length; i++) {
            set.add(base + comma + i + bracket);
        }
        return set;
    }

}
