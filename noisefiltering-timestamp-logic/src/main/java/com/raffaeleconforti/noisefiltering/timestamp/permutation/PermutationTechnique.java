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

package com.raffaeleconforti.noisefiltering.timestamp.permutation;

import org.deckfour.xes.model.XEvent;

import java.util.List;
import java.util.Set;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 14/09/2016.
 */
public interface PermutationTechnique {

    int ILP_GUROBI = 0;
    int ILP_GUROBI_ARCS = 1;
    int ILP_LPSOLVE = 2;
    int ILP_LPSOLVE_ARCS = 3;
    int HEURISTICS_BEST = 4;
    int HEURISTICS_SET = 5;

    Set<List<XEvent>> findBestStartEnd();

}
