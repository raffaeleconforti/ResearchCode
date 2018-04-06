package com.raffaeleconforti.alphadollar.alphamminer;

import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;

import java.util.Set;

class PlaceAdjacent {
	Set<Transition> pre;
	Set<Transition> succ;
	
	public PlaceAdjacent(Set<Transition> _pre, Set<Transition> _succ) {
		pre = new UnifiedSet<>(_pre);
		succ = new UnifiedSet<>(_succ);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result =1;
		result = prime * result + pre.hashCode();
		result = prime * result + succ.hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object placeAdjacent)	{
		if (!(placeAdjacent instanceof PlaceAdjacent))
			return false;

		PlaceAdjacent pa = (PlaceAdjacent) placeAdjacent;
		Set<Transition> thisPreWithoutInv = removeInv(this.pre);		// remove the invisible tasks....
		Set<Transition> thisSuccWithoutInv = removeInv(this.succ);		// remove the invisible tasks....
		Set<Transition> paPreWithoutInv = removeInv(pa.pre);		// remove the invisible tasks....
		Set<Transition> paSuccWithoutInv = removeInv(pa.succ);		// remove the invisible tasks....
		boolean result;
		result = (thisPreWithoutInv.equals(paPreWithoutInv) || (thisPreWithoutInv.size() == 0 && paPreWithoutInv.size() == 0) ) && (thisSuccWithoutInv.equals(paSuccWithoutInv) ||  (thisSuccWithoutInv.size() == 0 && paSuccWithoutInv.size() == 0));
		return result;
	}
	
	
	public static Set<Transition> removeInv(Set<Transition> original) {
		Set<Transition> result = new UnifiedSet<Transition>();
		for (Transition t	:	original) {
			if (t.getLogEvent() == null)
				continue;
			result.add(t);
		}
		return result;		
	}

	public static PlaceAdjacent convert(Place place) {
		PlaceAdjacent result;
		Set<Transition> _pre = place.getPredecessors();
		Set<Transition> _succ = place.getSuccessors();
		Set<Transition> pre = removeInv(_pre);
		Set<Transition> succ = removeInv(_succ);
		result = new PlaceAdjacent(pre, succ);
		return result;
	}
	
	
}

public class InvisibleAdjacent {

	Set<PlaceAdjacent> pred;	//the pred places.
	Set<PlaceAdjacent> succ;	//the succ places.
	
	public InvisibleAdjacent(Set<PlaceAdjacent> _pred, Set<PlaceAdjacent> _succ)
	{
		pred = _pred;
		succ = _succ;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
	    int result = 1;
		result =  prime * result + pred.hashCode() + succ.hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object invisibleAdjacent)
	{
		if (!(invisibleAdjacent instanceof InvisibleAdjacent))
			return false;
		InvisibleAdjacent ia = (InvisibleAdjacent) invisibleAdjacent;
		boolean result;
		result = ia.pred.equals(this.pred) && ia.succ.equals(this.succ);
		return result;
	}

}
