package com.raffaeleconforti.alphadollar.alphamminer;

import java.util.ArrayList;

public class AlphaPPData {
	RelationMatrix rmRelation = new RelationMatrix();	//relation matrix.
	ArrayList<String> alL1L = new ArrayList<String>();	//alL1L
	ArrayList<String> alT_I = new ArrayList<String>();	//alT_I
	ArrayList<String> alT_O = new ArrayList<String>();	//alT_O
	ArrayList<String> alT_prime = new ArrayList();				//alT_prime
	ArrayList<String> alT_log = new ArrayList();				//alT_log
	int allSize;										//size of all tasks
	int allVisibleSize;									//size of all visible tasks
	L1LPlaces lpL_W;
	ArrayList<InvTask> invTasks = new ArrayList<InvTask>();			//the invisible tasks
	ArrayList<AXYB> invTaskAXYB;							//the medacious relationship found in the alpha#
}
