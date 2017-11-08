package com.raffaeleconforti.alphadollar;

import com.raffaeleconforti.alphadollar.alphamminer.AlphaMMiner;
import com.raffaeleconforti.log.util.LogReaderClassic;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.mining.logabstraction.*;

public class AlphaMixMiner {

	public PetriNet mine(LogReaderClassic logReader) {
		if (logReader != null) {
			// Mine the log for a Petri net.
			AlphaMMiner miningPlugin = new AlphaMMiner();

			return miningPlugin.mine(logReader, getLogRelations(logReader), false);
		} else {
			System.err.println("No log reader could be constructed.");
			return null;
		}		
	}

	public LogRelations getLogRelations(LogReaderClassic log) {
		LogAbstraction logAbstraction = new LogAbstractionImpl(log, true);
		LogRelations relations = (new MinValueLogRelationBuilder(logAbstraction, 0,
				log.getLogSummary().getLogEvents())).getLogRelations();
		String[][] intervals = new String[0][0];

		// Third layer: Use Finite State Machine to insert causality
		if (true) {
			relations = (new FSMLogRelationBuilder(relations)).getLogRelations();
		}

		for (int i = 0; i < intervals.length; i++) {
			relations = (new TimeIntervalLogRelationBuilder(relations, log, intervals[i][0],
					intervals[i][1])).getLogRelations();
		}

		return relations;
	}

}
