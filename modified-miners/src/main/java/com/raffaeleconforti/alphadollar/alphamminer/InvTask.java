package com.raffaeleconforti.alphadollar.alphamminer;

import java.util.ArrayList;

public class InvTask {
	public final static int START = 1;
	public final static int END = 2;
	public final static int SKIP_REDO_SWITCH = 3;
	public final static String TRACE_BEGIN_TAG = "START_TRACE";
	public final static String TRACE_END_TAG = "END_TRACE";
	
	String taskName;
	int type;
	ArrayList<String> pre;
	ArrayList<String> suc;
	
	public InvTask(String _taskName, int _type)	{
		taskName = _taskName;
		pre = new ArrayList<String>();
		suc = new ArrayList<String>();
		type = _type;
	}
	
	public void addPreTask(String taskName)
	{
		pre.add(taskName);
	}
	
	public void addSucTask(String taskName)
	{
		suc.add(taskName);
	}
		
}
