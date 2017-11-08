package com.raffaeleconforti.alphadollar.alphamminer;

import com.raffaeleconforti.log.util.LogReaderClassic;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.mining.logabstraction.LogRelations;

import java.util.Date;


public class AlphaMMiner {
	AlphaPPData alphaPPData = new AlphaPPData();

	public PetriNet mine(LogReaderClassic log, LogRelations relations, boolean heuristics)	{
		ModifiedAlphaPPProcessMiner alphapp = new ModifiedAlphaPPProcessMiner();
		ModifiedAlphaSharpProcessMiner alphasharp = new ModifiedAlphaSharpProcessMiner();
		Date d1 = new Date();
		alphaPPData = alphasharp.mineAlphaSharpInfo(log,relations, heuristics);
		Date d2 = new Date();
//		System.out.println("Alpha Sharp"+(d2.getTime() - d1.getTime()));
		PetriNet petriNet = alphapp.mineAlphPPInfo(log, alphaPPData);
		Date d3 = new Date();
//		System.out.println("Alpha PP"+(d3.getTime() - d2.getTime()));
		return petriNet;
	}
}


